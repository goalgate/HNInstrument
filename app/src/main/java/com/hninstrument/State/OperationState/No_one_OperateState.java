package com.hninstrument.State.OperationState;


/**
 * Created by zbsz on 2017/9/26.
 */

public class No_one_OperateState extends OperationState {


    @Override
    public void onHandle(Operation op) {
        op.setState(new One_man_OperateState());
    }

/*    @Override
    public void setMessage(UnUploadPackageDao unUploadPackageDao, IRequestModule module, Boolean network_state) {

    }*/
}
