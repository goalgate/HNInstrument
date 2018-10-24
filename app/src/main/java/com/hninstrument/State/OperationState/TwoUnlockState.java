package com.hninstrument.State.OperationState;


import com.hninstrument.AppInit;
import com.hninstrument.Config.SHGJ_Config;
import com.hninstrument.Config.SH_Config;

/**
 * Created by zbsz on 2017/9/26.
 */

public class TwoUnlockState extends OperationState {


    @Override
    public void onHandle(Operation op) {
        if(AppInit.getInstrumentConfig().getClass().getName().equals(SHGJ_Config.class.getName())){
            op.setState(new OneLockState());
        }else{
            op.setState(new LockingState());
        }

    }

}
