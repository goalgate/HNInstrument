package com.hninstrument.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.AppInit;
import com.hninstrument.Bean.DataFlow.ReUploadBean;
import com.hninstrument.Builder.SocketBuilder;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Config.SHDMJ_config;
import com.hninstrument.Config.SHGJ_Config;
import com.hninstrument.EventBus.ADEvent;
import com.hninstrument.EventBus.AlarmEvent;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.ExitEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.EventBus.TemHumEvent;
import com.hninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import com.hninstrument.Receiver.TimeCheckReceiver;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.LockState.State_Unlock;
import com.hninstrument.Tools.MediaHelper;
import com.hninstrument.Tools.ServerConnectionUtil;
import com.hninstrument.greendao.DaoSession;
import com.hninstrument.greendao.ReUploadBeanDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cbdi.drv.netDa.INetDaSocketEvent;
import cbdi.drv.netDa.NetDACVCIP608Socket;
import cbdi.drv.netDa.NetDAM0888Data;
import cbdi.drv.netDa.NetDAM0888Socket;
import cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SwitchServiceByDN extends Service implements INetDaSocketEvent {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

    SimpleDateFormat check_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BaseConfig type = AppInit.getInstrumentConfig();

    private SPUtils config = SPUtils.getInstance("config");

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    SwitchPresenter sp = SwitchPresenter.getInstance();

    String Last_Value;

    Lock lock;

    Disposable dis_testNet;

    Disposable dis_checkOnline;

    Disposable dis_TemHum;

    Disposable dis_stateRecord;


    private NetDACVCIP608Socket daIP=null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    boolean network_State = false;

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        lock = Lock.getInstance(new State_Lockup(sp));
        autoUpdate();
        daIP=new NetDACVCIP608Socket();
        daIP.open(5000);
        daIP.setEvent(this);
        dis_testNet = Observable.interval(5, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=test"/*&pass=" + new SafeCheck().getPass(config.getString("devid"))*/
                                , config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (response != null) {
                                            if (response.startsWith("true")) {
                                                if (!network_State) {
                                                    updata();
                                                }
                                                network_State = true;
                                                EventBus.getDefault().post(new NetworkEvent(true, "服务器连接正常"));
                                            } else {
                                                network_State = false;
                                                EventBus.getDefault().post(new NetworkEvent(false, "设备出错"));
                                            }
                                        } else {
                                            network_State = false;
                                            EventBus.getDefault().post(new NetworkEvent(false, "服务器连接出错"));
                                        }
                                    }
                                });
                    }
                });

        dis_checkOnline = Observable.interval(0, type.getCheckOnlineTime(), TimeUnit.MINUTES)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        if (network_State) {
                            connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=checkOnline"/*&pass=" + new SafeCheck().getPass(config.getString("devid"))*/,
                                    config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                                        @Override
                                        public void onResponse(String response) {

                                        }
                                    });
                        }
                    }
                });
        reboot();
        reUpload();
    }

    public void reUpload() {
        final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list) {
            if (bean.getContent() != null) {
                if (bean.getType_patrol() != 0) {
                    connectionUtil.post_SingleThread(config.getString("ServerId") + type.getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("devid") + "&checkType=" + bean.getType_patrol(),
                            config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                            reUploadBeanDao.delete(bean);
                                        }
                                    }
                                }
                            });
                } else {
                    connectionUtil.post_SingleThread(config.getString("ServerId") + type.getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("devid"),
                            config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                            reUploadBeanDao.delete(bean);
                                        }
                                    }
                                }
                            });
                }
            } else {
                connectionUtil.post_SingleThread(config.getString("ServerId") + type.getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("devid"),
                        config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                            @Override
                            public void onResponse(String response) {
                                if (response != null) {
                                    if (response.startsWith("true")) {
                                        Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                        reUploadBeanDao.delete(bean);
                                    }
                                }
                            }
                        });
            }
        }
    }

    private void autoUpdate() {
        connectionUtil.download("http://124.172.232.89:8050/daServer/updateADA.do?ver=" + AppUtils.getAppVersionName() + "&url=" + config.getString("ServerId") + "&daid=" + config.getString("devid"), config.getString("ServerId"), new ServerConnectionUtil.Callback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if (response.equals("true")) {
                        AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"), "application/vnd.android.package-archive");
                    }
                }
            }
        });
    }

    private void updata() {
        connectionUtil.post(config.getString("ServerId") + type.getPersonInfoPrefix() + "dataType=updatePersion&daid=" + config.getString("devid") /*+ "&pass=" + new SafeCheck().getPass(config.getString("devid"))*/ + "&persionType=2",
                config.getString("ServerId"),
                new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            SPUtils.getInstance("personData").clear();
                            String[] idList = response.split("\\|");
                            if (idList.length > 0) {
                                for (String id : idList) {
                                    SPUtils.getInstance("personData").put(id, "2");
                                }
                                connectionUtil.post(SPUtils.getInstance("config").getString("ServerId") + type.getPersonInfoPrefix() + "dataType=updatePersion&daid=" + config.getString("devid")/* + "&pass=" + new SafeCheck().getPass(config.getString("devid")) */ + "&persionType=1",
                                        config.getString("ServerId"), new ServerConnectionUtil.Callback() {

                                            @Override
                                            public void onResponse(String response) {
                                                if (response != null) {
                                                    String[] idList = response.split("\\|");
                                                    if (idList.length > 0) {
                                                        for (String id : idList) {
                                                            SPUtils.getInstance("personData").put(id, "1");
                                                        }
                                                    } else {
                                                        ToastUtils.showLong("没有相应仓管员信息");
                                                    }
                                                } else {
                                                    ToastUtils.showLong("连接服务器错误");
                                                }

                                            }
                                        });
                            } else {
                                ToastUtils.showLong("没有相应巡检员信息");
                            }
                        } else {
                            ToastUtils.showLong("连接服务器错误");
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        lock.setLockState(new State_Unlock(sp));
        lock.doNext();
        if (type.getClass().getName().equals(SHGJ_Config.class.getName())
                || type.getClass().getName().equals(SHDMJ_config.class.getName())) {
            sp.doorOpen();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetCloseEvent(CloseDoorEvent event) {
        lock.setLockState(new State_Lockup(sp));
        CloseDoorRecord();
        sp.buzz(SwitchImpl.Hex.H2);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetExitEvent(ExitEvent event) {
        lock.setLockState(new State_Lockup(sp));
//        Intent dialogIntent = new Intent(getBaseContext(), SplashActivity.class);
//        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplication().startActivity(dialogIntent);
    }


    public void onDestroy() {
        super.onDestroy();
//        sp.SwitchPresenterSetView(null);
        try {
            if (dis_testNet != null) {
                dis_testNet.dispose();
            }
            if (dis_checkOnline != null) {
                dis_checkOnline.dispose();
            }
            if (dis_stateRecord != null) {
                dis_stateRecord.dispose();
            }
            if (dis_TemHum != null) {
                dis_TemHum.dispose();
            }
        }catch (NullPointerException e){
            Lg.e("Exception",e.getMessage());
        }

        EventBus.getDefault().unregister(this);
    }

    private void alarmRecord() {
        EventBus.getDefault().post(new AlarmEvent());
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=alarm&alarmType=1" + "&time=" + formatter.format(new Date(System.currentTimeMillis())),
                config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            mdaoSession.insert(new ReUploadBean(null, "dataType=alarm&alarmType=1" + "&time=" + formatter.format(new Date(System.currentTimeMillis())), null, 0));
                        }
                    }
                });
    }

    private void CloseDoorRecord() {
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=closeDoor" + "&time=" + formatter.format(new Date(System.currentTimeMillis())),
                config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            mdaoSession.insert(new ReUploadBean(null, "dataType=closeDoor" + "&time=" + formatter.format(new Date(System.currentTimeMillis())), null, 0));
                            MediaHelper.play(MediaHelper.Text.err_connect_relock);
                        } else {
                            MediaHelper.play(MediaHelper.Text.relock_opt);
                        }
                    }
                });
    }

    private Boolean getLockState(Class stateClass) {
        if (lock.getLockState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }

    private void reboot() {
        long daySpan = 24 * 60 * 60 * 1000 * 1;
        // 规定的每天时间，某时刻运行
        int randomTime = new Random().nextInt(50) + 10;
        String pattern = "yyyy-MM-dd '03:" + randomTime + ":00'";
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Log.e("rebootTime", pattern);
        // 首次运行时间
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime()) {
                startTime = new Date(startTime.getTime() + daySpan);
            } else if (startTime.getHours() == new Date().getHours()) {
                startTime = new Date(startTime.getTime() + daySpan);
            }
            Log.e("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));
            Timer t = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 要执行的代码
                    AppInit.getMyManager().reboot();
                    Log.e("信息提示", "关机了");
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(int num, int state)  //打开状态 num 连接编号  state状态  1为连接 0为断开
    {
        Lg.e("采集盒", "open:" + num + "_" + state);
        if (state == 0) {
            ToastUtils.showLong("采集断网");
        }
    }

    @Override
    public void onCmd(int num, int cmdType, byte value)  //命令回应  num 连接编号 cmdType命令类型   value返回值
    {
        Lg.e("采集盒", "onCmd:" + num + "_" + cmdType + "_" + value);
        if (cmdType == 2) {
            //Lg.v("onDI:",""+cmdType+"_"+value);
            //daData.setDI(value);
            //tcatd.setText(daData.getDI(0).getName()+daData.getDI(0).getVal()); //第一路
            Lg.e("开关量变化",String.valueOf(value));
            if (value == 0) {
                if (getLockState(State_Lockup.class)) {
                    lock.doNext();
                    alarmRecord();
                }
            }
            //tcatd.setText("模块号"+(num/1000)+"   通道号："+(num%1000)+"   开关量："+value); //
        }
    }

    @Override
    public void onAI(int num, int cmdType, int[] value)  //命令回应  num 连接编号 cmdType命令类型   value返回值
    {
//        Lg.v("onAI:",""+cmdType+"_"+value[1]);
//        if (netDa.getCmd().cmdType_ai == cmdType) {
//            int i=0;
//            daData.setAI(value);
//            //info.setText(daData.getAI(i).getName() +daData.getAI(i).getVal()+ daData.getAI(i).getUnit());//第一路
//        }
    }

}
