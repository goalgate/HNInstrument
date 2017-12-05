package com.hninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Function.Func_Switch.mvp.view.ISwitchView;

import com.hninstrument.R;
import com.hninstrument.Retrofit.RetrofitGenerator;
import com.hninstrument.State.DoorState.Door;
import com.hninstrument.State.DoorState.State_Close;
import com.hninstrument.State.DoorState.State_Open;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.LockState.State_Unlock;
import com.hninstrument.State.OperationState.No_one_OperateState;
import com.hninstrument.Tools.SafeCheck;
import com.trello.rxlifecycle2.android.ActivityEvent;

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
    SwitchPresenter sp = SwitchPresenter.getInstance();

    String Last_Value;

    Disposable dis_rx_delay;

    Disposable dis_unlock_noOpen;



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
    /*    door = new Door(new State_Close(lock));*/

        dis_testNet = Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (NetworkUtils.isConnected()) {
                            Map<String,Object> map = new HashMap<String, Object>() ;
                            map.put("daid","1234567890");
                            map.put("dataType","test");
                            map.put("pass",new SafeCheck().getPass("1234567890"));
                            RetrofitGenerator.getCommonApi().CommonRequest(map).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onNext(String s) {
                                    if(s.equals("true")){
                                        network_State = true;
                                        EventBus.getDefault().post(new NetworkEvent(true, "服务器连接正常"));
                                    }else{
                                        network_State = false;
                                        EventBus.getDefault().post(new NetworkEvent(false, "设备出错"));
                                    }
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    network_State = false;
                                    EventBus.getDefault().post(new NetworkEvent(false, "服务器连接出错"));
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                        }else{
                            network_State = false;
                            EventBus.getDefault().post(new NetworkEvent(false, "请检查网络是否已连接"));

                        }
                    }
                });

        dis_checkOnline = Observable.interval(1, 1, TimeUnit.HOURS)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        if (network_State) {
                            Map<String,Object> map = new HashMap<String, Object>() ;
                            map.put("daid","1234567890");
                            map.put("dataType","checkOnline");
                            map.put("pass",new SafeCheck().getPass("1234567890"));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        lock.setLockState(new State_Unlock(sp));
       /* if(lock.isAlarming()){
            AlarmCease();
        }*/
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

        if (dis_unlock_noOpen != null) {
            dis_unlock_noOpen.dispose();
        }
        if (dis_rx_delay != null) {
            dis_rx_delay.dispose();
        }
        if (dis_testNet != null) {
            dis_testNet.dispose();
        }
        if(dis_checkOnline!= null){
            dis_checkOnline.dispose();
        }
        EventBus.getDefault().unregister(this);
        Intent localIntent = new Intent();
        localIntent.setClass(this, SwitchService.class); //销毁时重新启动Service
        this.startService(localIntent);
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
                   /* door.setDoorState(new State_Open(lock));
                    door.doNext();*/
                    lock.doNext();
                    alarmRecord();
                }
            }

        } else {
            if (value.startsWith("AAAAAA")) {
                if (!value.equals(Last_Value)) {
                    Last_Value = value;
                    if (Last_Value.equals("AAAAAA000000000000")) {
                        /*if (getDoorState(State_Close.class)) {
                            *//*door.setDoorState(new State_Open(lock));
                            door.doNext();*//*
                            if (getLockState(State_Lockup.class)) {
                                alarmRecord();
                            }
                        }*/
                        if (getLockState(State_Lockup.class)) {
                            lock.doNext();
                            alarmRecord();
                        }
                        if (dis_unlock_noOpen != null) {
                            dis_unlock_noOpen.dispose();
                        }
                        if (dis_rx_delay != null) {
                            dis_rx_delay.dispose();
                        }
                    } /*else if (Last_Value.equals("AAAAAA000001000000")) {
                        final String closeDoorTime = TimeUtils.getNowString();
                        Observable.timer(20, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        dis_rx_delay = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        lock.setLockState(new State_Lockup(sp));
                                 *//*       door.setDoorState(new State_Close(lock));*//*
                                        CloseDoorRecord(closeDoorTime);
                                        sp.buzz(SwitchImpl.Hex.H2);

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }*/
                }

                /*if (getLockState(State_Unlock.class) && value.equals("AAAAAA000001000000")) {
                    Observable.timer(120, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    dis_unlock_noOpen = d;
                                }

                                @Override
                                public void onNext(Long aLong) {
                                    lock.setLockState(new State_Lockup(sp));
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                }*/
            }
        }
    }


    private void alarmRecord(){
   /*     Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });*/

        if(network_State){
            Map<String,Object> map = new HashMap<String, Object>() ;
            map.put("daid","1234567890");
            map.put("dataType","alarm");
            map.put("pass",new SafeCheck().getPass("1234567890"));
            map.put("alarmType","1");
            map.put("time",TimeUtils.getNowString());
            RetrofitGenerator.getCommonApi().CommonRequest(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {

                        }
                    });
        }else {

        }
    }

    private void CloseDoorRecord(String time) {
        if(network_State){
            Map<String,Object> map = new HashMap<String, Object>() ;
            map.put("daid","1234567890");
            map.put("dataType","closeDoor");
            map.put("pass",new SafeCheck().getPass("1234567890"));
            map.put("time",TimeUtils.getNowString());
            RetrofitGenerator.getCommonApi().CommonRequest(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {

                        }
                    });
        }else {

        }
    }







   /* private Boolean getDoorState(Class stateClass) {
        if (door.getDoorState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }*/
    private Boolean getLockState(Class stateClass) {
        if (lock.getLockState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }
}

