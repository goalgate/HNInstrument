package com.hninstrument.State.LockState;

/**
 * Created by zbsz on 2017/9/28.
 */

public abstract class LockState {

    public abstract void onHandle(Lock lock);

    public abstract boolean isAlarming() ;
}
