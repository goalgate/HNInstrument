package com.hninstrument.State.OperationState;


/**
 * Created by zbsz on 2017/9/26.
 */

public class LockingState extends OperationState {

    @Override
    public void onHandle(Operation op) {
        op.setState(new OneUnlockState());
    }


}
