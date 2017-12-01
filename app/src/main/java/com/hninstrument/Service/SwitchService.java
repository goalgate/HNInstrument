package com.hninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import com.hninstrument.State.DoorState.Door;
import com.hninstrument.State.DoorState.State_Close;
import com.hninstrument.State.DoorState.State_Open;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.LockState.State_Unlock;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/11/24.
 */

public class SwitchService extends Service implements ISwitchView {
    SwitchPresenter sp = SwitchPresenter.getInstance();

    String Last_Value;

    Disposable rx_delay;

    Disposable unlock_noOpen;

    Door door;

    Lock lock;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        sp.switch_Open();
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        lock = new Lock(new State_Lockup(sp));
        door = new Door(new State_Close(lock));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        lock.setLockState(new State_Unlock(sp));
       /* if(lock.isAlarming()){
            AlarmCease();
        }*/
        lock.doNext();

    }



    public void onDestroy() {
        super.onDestroy();
        sp.SwitchPresenterSetView(null);
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
                    door.setDoorState(new State_Open(lock));
                    door.doNext();

                    //alarmRecord();
                }
            }

        } else {
            if (value.startsWith("AAAAAA")) {
                if (!value.equals(Last_Value)) {
                    Last_Value = value;
                    if (Last_Value.equals("AAAAAA000000000000")) {
                        if (getDoorState(State_Close.class)) {
                            door.setDoorState(new State_Open(lock));
                            door.doNext();
                           /* if (getLockState(State_Lockup.class)) {
                                alarmRecord();
                            }*/
                        }


                        if (unlock_noOpen != null) {
                            unlock_noOpen.dispose();
                        }
                        if (rx_delay != null) {
                            rx_delay.dispose();
                        }
                    } else if (Last_Value.equals("AAAAAA000001000000")) {
                        final String closeDoorTime = TimeUtils.getNowString();
                        Observable.timer(20, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        rx_delay = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        lock.setLockState(new State_Lockup(sp));
                                        door.setDoorState(new State_Close(lock));
                                       /* CloseDoorRecord(closeDoorTime);*/
                                        sp.buzz(SwitchImpl.Hex.H2);

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }
                }

                if (getLockState(State_Unlock.class) && value.equals("AAAAAA000001000000")) {
                    Observable.timer(120, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    unlock_noOpen = d;
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
                }
            }
        }
    }





    private Boolean getDoorState(Class stateClass) {
        if (door.getDoorState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
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

