package com.hninstrument.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.AppInit;
import com.hninstrument.Bean.DataFlow.ReUploadBean;
import com.hninstrument.Builder.SocketBuilder;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Config.HeBei_Config;
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
import com.hninstrument.SplashActivity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cbdi.drv.netDa.INetDaSocketEvent;
import cbdi.drv.netDa.NetDAM0888Data;
import cbdi.drv.netDa.NetDAM0888Socket;
import cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/11/24.
 */

public class SwitchService extends Service implements ISwitchView, INetDaSocketEvent {

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

    Disposable dis_checkTime;

    Disposable dis_stateRecord;

    private int ADNum = 0;

    private NetDAM0888Socket netDa = null;

    private NetDAM0888Data daData = new NetDAM0888Data();

    int last_mTemperature = 0;

    int last_mHumidity = 0;

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
        sp.switch_Open();
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        lock = Lock.getInstance(new State_Lockup(sp));
        autoUpdate();
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

        if (type.isTemHum()) {
            dis_TemHum = Observable.interval(0, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                @Override
                public void accept(@NonNull Long aLong) throws Exception {
                    sp.readHum();
                }
            });

            dis_stateRecord = Observable.interval(10, 3600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(@NonNull Long aLong) throws Exception {
                            StateRecord();
                        }
                    });
        }
        if (type.isCheckTime()) {
            dis_checkTime = Observable.timer(30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            checkTime(false);
                            replaceCheckTime();
                        }
                    });

        }
        if (type.collectBox()) {
            netDa = new SocketBuilder()
                    .setBuilderNumber(1)
                    .setBuilderEvent(this)
                    .setBuilderDATime(1000)
                    .builder_open("192.168.12.232", 10000);
            connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=queryTdhInfo"/*&pass=" + new SafeCheck().getPass(config.getString("devid"))*/,
                    config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null && !response.equals("false")) {
                                String[] str_array = response.split("\\|\\|");
                                ADNum = str_array.length;
                                for (String s : str_array) {
                                    String[] new_s = s.split("\\,");
                                    daData.getAI(Integer.parseInt(new_s[0]))
                                            .setBuilderEnable(true)
                                            .setBuilderName((new_s[1]))
                                            .setBuilderUnit((new_s[2]))
                                            .setBuilderMinVal(Integer.parseInt(new_s[3]))
                                            .setBuilderMaxVal(Integer.parseInt(new_s[4]))
                                            .setBuilderMinRange(Integer.parseInt(new_s[5]))
                                            .setBuilderMaxRange(Integer.parseInt(new_s[6]))
                                            .setSensorAIBuilderPrecision(Integer.parseInt(new_s[7]))
                                            .setSensorAIBuilderAlarmMinVal(Integer.parseInt(new_s[8]))
                                            .setSensorAIBuilderAlarmMaxVal(Integer.parseInt(new_s[9]));
//                    通道号，名称，单位，采集最小值，采集最大值，最小量程，最大量程，精度,最小报警值(-1时不报警)，最大报警值，变化值（-1为不设置）||N
//                    0,液位:,米,4,20,0,5,2,-1,-1,-1||1,气体浓度:,%,4,20,0,100,1,-1,20,-1
                                }
                            }
                        }
                    });
        }
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
        sp.buzz(SwitchImpl.Hex.HA);
//        Intent dialogIntent = new Intent(getBaseContext(), SplashActivity.class);
//        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplication().startActivity(dialogIntent);
    }


    public void onDestroy() {
        super.onDestroy();
        sp.SwitchPresenterSetView(null);
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onTemHum(int temperature, int humidity) {
        EventBus.getDefault().post(new TemHumEvent(temperature, humidity));
        if ((Math.abs(temperature - last_mTemperature) > 3 || Math.abs(temperature - last_mTemperature) > 10)) {
            StateRecord();
        }
        last_mTemperature = temperature;
        last_mHumidity = humidity;
    }

    @Override
    public void onSwitchingText(String value) {
        if (value.startsWith("AAAAAA")) {
            if ((Last_Value == null || Last_Value.equals(""))) {
                Last_Value = value;
            }
            if (!value.equals(Last_Value)) {
                Last_Value = value;
                if (Last_Value.equals("AAAAAA000000000000")) {
                    if (getLockState(State_Lockup.class)) {
                        lock.doNext();
                        alarmRecord();
                    }
                }
            }
        }

    }

    boolean socket_connect = false;

    private void StateRecord() {
        if (type.collectBox()) {
            String extra = "";
            if (socket_connect) {
                for (int i = 0; i < ADNum; i++) {
                    extra += i + "," + daData.getAI(i).getName() + "," + daData.getAI(i).getVal() + ","
                            + daData.getAI(i).getUnit() + "," + daData.getAI(i).getMinVal() + ","
                            + daData.getAI(i).getMaxVal() + "||";
                    //extra += "&"+daData.getAI(i).getEnglish_name()+"="+daData.getAI(i).getVal();
                }
            }
            extra = EncodeUtils.urlEncode(extra);
            connectionUtil.post(config.getString("ServerId")
                            + type.getUpDataPrefix() + "daid=" + config.getString("devid")
                            + "&dataType=temHum&tem=" + last_mTemperature + "&hum=" + last_mHumidity
                            + "&data=" + extra
                            + "&time=" + formatter.format(new Date(System.currentTimeMillis())),
                    config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {

                        }
                    });
        } else {
            if (network_State) {
                connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=temHum&tem=" + last_mTemperature + "&hum=" + last_mHumidity + "&time=" + formatter.format(new Date(System.currentTimeMillis())),
                        config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                            @Override
                            public void onResponse(String response) {

                            }
                        });
            }
        }
    }

    private void alarmRecord() {
        EventBus.getDefault().post(new AlarmEvent());
        if (!AppInit.getInstrumentConfig().getClass().getName().equals(HeBei_Config.class.getName())) {
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

    Calendar c = Calendar.getInstance();
    PendingIntent checkTime_pi;

    private void checkTime(final boolean add) {
        Lg.e("提示", "获取时间开始");
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=checkTime" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        Intent check_intent = new Intent(SwitchService.this, TimeCheckReceiver.class);
                        check_intent.setAction("checkTime");
                        if (response != null) {
                            String[] timeArray = response.split(",");
                            for (String time : timeArray) {
                                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]), 00); //当前时间
                                if (add) {
                                    c.add(Calendar.DATE, 1);
                                }
                                c.add(Calendar.MINUTE, -5);
                                if (c.getTimeInMillis() > System.currentTimeMillis()) {
                                    check_intent.putExtra("time", check_formatter.format(c.getTimeInMillis()));
                                    checkTime_pi = PendingIntent.getBroadcast(SwitchService.this, new Random().nextInt(), check_intent, 0);
                                    Lg.e("时间", check_formatter.format(c.getTimeInMillis()));
                                    am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), checkTime_pi);
                                }
                                if (add) {
                                    c.add(Calendar.DATE, -1);
                                }
                            }
                        }
                    }
                });
    }

    private void replaceCheckTime() {
        long daySpan = 24 * 60 * 60 * 1000;
        // 规定的每天时间，某时刻运行
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '23:55:00'");
        // 首次运行时间
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime())
                startTime = new Date(startTime.getTime() + daySpan);
            Timer t = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 要执行的代码
                    checkTime(true);
                    Lg.e("信息提示：", "重置巡检时间");
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
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
    public void onAI(int num, int cmdType, int[] value) {
        if (netDa.getCmd().cmdType_ai == cmdType) {
            daData.setAI(value);
            EventBus.getDefault().post(new ADEvent(daData, String.valueOf(ADNum)));
            socket_connect = true;
        }
    }

    @Override
    public void onOpen(int num, int state) {
        if (state == 0) {
            EventBus.getDefault().post(new ADEvent(null, "数据采集断开"));
            socket_connect = false;
        }
    }

    @Override
    public void onCmd(int num, int cmdType, byte value) {
        if (cmdType == netDa.getCmd().cmdType_di) {
            //Lg.v("onDI:",""+cmdType+"_"+value);
            daData.setDI(value);
        }
    }
}

