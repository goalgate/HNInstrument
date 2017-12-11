package com.hninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import com.hninstrument.Retrofit.RetrofitGenerator;
import com.hninstrument.Retrofit.ServerConnectionUtil;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.LockState.State_Unlock;
import com.hninstrument.Tools.SafeCheck;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/11/24.
 */

public class SwitchService extends Service implements ISwitchView {

    private SPUtils config = SPUtils.getInstance("config");

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    SwitchPresenter sp = SwitchPresenter.getInstance();

    String Last_Value;

    Lock lock;

    Disposable dis_testNet;

    Disposable dis_checkOnline;

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
                        if (NetworkUtils.isConnected()) {
                            connectionUtil.https(config.getString("ServerId") + "daServer/da_gzmb_updata?daid=" + config.getString("devid") + "&dataType=test&pass=" + new SafeCheck().getPass(config.getString("devid"))
                                    , new ServerConnectionUtil.Callback() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (response != null) {
                                                if (response.startsWith("true")) {
                                                    if(!network_State){
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
                    }

                });

        dis_checkOnline = Observable.interval(1, 1, TimeUnit.HOURS)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        if (network_State) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("daid", config.getString("devid"));
                            map.put("dataType", "checkOnline");
                            map.put("pass", new SafeCheck().getPass(config.getString("devid")));
                            RetrofitGenerator.getCommonApi().CommonRequest(map)
                                    .subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String s) throws Exception {
                                }
                            });
                        }
                    }
                });


    }

    private void updata(){
        connectionUtil.https(config.getString("ServerId") + "daServer/da_gzmb_persionInfo?dataType=updatePersion&daid=" + config.getString("devid") + "&pass=" + new SafeCheck().getPass(config.getString("devid")) + "&persionType=1", new ServerConnectionUtil.Callback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    SPUtils.getInstance("personData").clear();
                    String[] idList = response.split("\\|");
                    if (idList[0].length() == 18) {
                        for (String id : idList) {
                            SPUtils.getInstance("personData").put(id, "1");
                        }
                        connectionUtil.https(SPUtils.getInstance("config").getString("ServerId") + "daServer/da_gzmb_persionInfo?dataType=updatePersion&daid=" + config.getString("devid") + "&pass=" + new SafeCheck().getPass(config.getString("devid")) + "&persionType=2", new ServerConnectionUtil.Callback() {

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
        CloseDoorRecord(TimeUtils.getNowString());
        sp.buzz(SwitchImpl.Hex.H2);
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
        EventBus.getDefault().unregister(this);
/*        Intent localIntent = new Intent();
        localIntent.setClass(this, SwitchService.class); //销毁时重新启动Service
        this.startService(localIntent);*/
    }

    @Override
    public void onTemHum(int temperature, int humidity) {

    }

    @Override
    public void onSwitchingText(String value) {
        if ((Last_Value == null || Last_Value.equals(""))) {
            if (value.startsWith("AAAAAA")) {
                Last_Value = value;
                if (value.equals("AAAAAA000000000000")) {
                    lock.doNext();
                    alarmRecord();
                }
            }

        } else {
            if (value.startsWith("AAAAAA")) {
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
    }


    private void alarmRecord() {
        if (network_State) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("daid", config.getString("devid"));
            map.put("dataType", "alarm");
            map.put("pass", new SafeCheck().getPass(config.getString("devid")));
            map.put("alarmType", "1");
            map.put("time", TimeUtils.getNowString());
            RetrofitGenerator.getCommonApi().CommonRequest(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {

                        }
                    });
        } else {

        }
    }

    private void CloseDoorRecord(String time) {
        if (network_State) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("daid", config.getString("devid"));
            map.put("dataType", "closeDoor");
            map.put("pass", new SafeCheck().getPass(config.getString("devid")));
            map.put("time", TimeUtils.getNowString());
            RetrofitGenerator.getCommonApi().CommonRequest(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {

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
}

