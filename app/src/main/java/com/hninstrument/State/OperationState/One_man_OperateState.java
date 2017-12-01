package com.hninstrument.State.OperationState;



/**
 * Created by zbsz on 2017/9/26.
 */

public class One_man_OperateState extends OperationState {



    @Override
    public void onHandle(Operation op) {
        op.setState(new Two_man_OperateState());
    }
/*
    @Override
    public void setMessage(UnUploadPackageDao unUploadPackageDao, IRequestModule module, Boolean network_state) {

    }*/
}
