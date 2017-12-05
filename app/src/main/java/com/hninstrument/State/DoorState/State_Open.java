package com.hninstrument.State.DoorState;



import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.LockState.State_Unlock;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zbsz on 2017/9/27.
 */

public class State_Open extends DoorState {

    Lock lock;

    public State_Open(Lock lock) {
        this.lock = lock;
    }

    @Override
    public void onHandle(Door door) {
        if (lock.getLockState().getClass().getName().equals(State_Lockup.class.getName())) {
            lock.doNext();
        }
    }
}
