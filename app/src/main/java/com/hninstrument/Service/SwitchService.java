package com.hninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.AppInit;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.ExitEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.EventBus.TemHumEvent;
import com.hninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.LockState.State_Unlock;
import com.hninstrument.Tools.ServerConnectionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/11/24.
 */

public class SwitchService extends Service implements ISwitchView {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

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

    int last_mTemperature = 0;

    int last_mHumidity = 0;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    boolean network_State = false;


    @Override
    public void onCreate() {
        super.onCreate();
        sp.switch_Open();
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        lock = new Lock(new State_Lockup(sp));
        dis_testNet = Observable.interval(5, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix()+"daid=" + config.getString("devid") + "&dataType=test"/*&pass=" + new SafeCheck().getPass(config.getString("devid"))*/
                                    ,config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (response != null) {
                                                if (response.startsWith("true")) {
                                                    if(!network_State){
                                                        updata();
                                                        autoUpdate();
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
                            connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix()+"daid=" + config.getString("devid") + "&dataType=checkOnline"/*&pass=" + new SafeCheck().getPass(config.getString("devid"))*/,
                                    config.getString("ServerId"),new ServerConnectionUtil.Callback() {
                                        @Override
                                        public void onResponse(String response) {

                                        }
                                    });
                        }
                    }
                });

        if(type.isTemHum()){
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
        reboot();
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

    private void updata(){
        connectionUtil.post(config.getString("ServerId") + type.getPersonInfoPrefix()+"dataType=updatePersion&daid=" + config.getString("devid") /*+ "&pass=" + new SafeCheck().getPass(config.getString("devid"))*/ + "&persionType=1",
                config.getString("ServerId"),
                new ServerConnectionUtil.Callback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    SPUtils.getInstance("personData").clear();
                    String[] idList = response.split("\\|");
                    if (idList[0].length() == 18) {
                        for (String id : idList) {
                            SPUtils.getInstance("personData").put(id, "1");
                        }
                        connectionUtil.post(SPUtils.getInstance("config").getString("ServerId") + type.getPersonInfoPrefix()+"dataType=updatePersion&daid=" + config.getString("devid")/* + "&pass=" + new SafeCheck().getPass(config.getString("devid")) */+ "&persionType=2",
                                config.getString("ServerId"),new ServerConnectionUtil.Callback() {

                            @Override
                            public void onResponse(String response) {
                                String[] idList = response.split("\\|");
                                if (idList[0].length() == 18) {
                                    for (String id : idList) {
                                        SPUtils.getInstance("personData").put(id, "2");
                                    }
                                } else {
                                    ToastUtils.showLong("巡检员更新错误或并无巡检员");
                                }
                            }
                        });
                    } else {
                        ToastUtils.showLong("仓管员更新错误或并无仓管员");
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
        if(dis_stateRecord != null){
            dis_stateRecord.dispose();
        }
        if (dis_TemHum != null){
            dis_TemHum.dispose();
        }
        EventBus.getDefault().unregister(this);
   /*     Intent localIntent = new Intent();
        localIntent.setClass(this, SwitchService.class); //销毁时重新启动Service
        this.startService(localIntent);*/
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
        if(value.startsWith("AAAAAA")){
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

    private void StateRecord() {
        if (network_State) {
            connectionUtil.post(config.getString("ServerId")+ type.getUpDataPrefix()+"daid=" + config.getString("devid") + "&dataType=temHum&tem="+last_mTemperature+"&hum="+last_mHumidity+ "&time=" +formatter.format(new Date(System.currentTimeMillis())),
                    config.getString("ServerId"),new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {

                        }
                    });
        }

    }


    private void alarmRecord() {
        if (network_State) {
            connectionUtil.post(config.getString("ServerId")+ type.getUpDataPrefix()+"daid=" + config.getString("devid") + "&dataType=alarm&alarmType=1"+ "&time=" +formatter.format(new Date(System.currentTimeMillis())),
                    config.getString("ServerId"),new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {

                        }
                    });
        } else {

        }
    }

    private void CloseDoorRecord() {
        if (network_State) {
            connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix()+"daid=" + config.getString("devid") + "&dataType=closeDoor"+"&time=" + formatter.format(new Date(System.currentTimeMillis())),
                    config.getString("ServerId"),new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {

                        }
                    });
        } else {

        }
    }
    private Boolean getLockState(Class stateClass) {
        if (lock.getLockState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }
    private void reboot(){
        long daySpan = 24 * 60 * 60 * 1000 * 2;
        // 规定的每天时间，某时刻运行
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '3:00:00'");
        // 首次运行时间
        try{
            Date startTime= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if(System.currentTimeMillis() > startTime.getTime())
                startTime = new Date(startTime.getTime() + daySpan);
            Timer t = new Timer();
            TimerTask task = new TimerTask(){
                @Override
                public void run() {
                    // 要执行的代码
                    AppInit.getMyManager().reboot();
                }
            };
            t.scheduleAtFixedRate(task, startTime,daySpan);
        }catch (ParseException e){
            e.printStackTrace();
        }
    }
}

