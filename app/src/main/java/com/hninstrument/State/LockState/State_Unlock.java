package com.hninstrument.State.LockState;


import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;

/**
 * Created by zbsz on 2017/9/28.
 */

public class State_Unlock extends LockState {

    public boolean alarming;

    SwitchPresenter sp;

    public State_Unlock(SwitchPresenter sp) {
        this.sp = sp;
    }

    @Override
    public void onHandle(Lock lock) {
        sp.OutD9(false);
        alarming = false;
    }

    @Override
    public boolean isAlarming() {
        return alarming;
    }
}
