package com.hninstrument.State.OperationState;



import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/9/26.
 */

public class Check_OperateState extends OperationState {


    @Override
    public void onHandle(Operation op) {
        op.setState(new No_one_OperateState());
    }
  /*  IRequestModule checkModule;

    UnUploadPackageDao unUploadPackageDao;

    Boolean network_state;

    @Override
    public void setMessage(UnUploadPackageDao unUploadPackageDao, IRequestModule module, Boolean network_state){
        this.unUploadPackageDao = unUploadPackageDao;
        this.checkModule = module;
        this.network_state = network_state;
    }

    @Override
    public void onHandle(Operation op) {
        if (network_state) {
            RetrofitGenerator.getCommonApi().commonFunction(RequestEnvelope.GetRequestEnvelope(checkModule))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SaveObserver(unUploadPackageDao,checkModule));
        } else {

            UnUploadPackage un = new UnUploadPackage();
            un.setMethod(checkModule.getMethod());
            un.setJsonData(checkModule.getJSON());
            un.setUpload(false);
            unUploadPackageDao.insert(un);
        }


        op.setState(new No_one_OperateState());
    }*/
}
