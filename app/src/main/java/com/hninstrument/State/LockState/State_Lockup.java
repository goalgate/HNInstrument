package com.hninstrument.State.LockState;


import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;

/**
 * Created by zbsz on 2017/9/28.
 */

public class State_Lockup extends LockState {

    public boolean alarming;

    SwitchPresenter sp;

    public State_Lockup(SwitchPresenter sp) {
        this.sp = sp;
    }
    @Override
    public void onHandle(Lock lock) {
        sp.OutD9(true);
        alarming = true;
    }

    @Override
    public boolean isAlarming() {
        return alarming;
    }
}