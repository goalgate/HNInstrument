package com.hninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

import com.blankj.utilcode.util.BarUtils;
import com.hninstrument.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.hninstrument.Function.Func_Camera.mvp.view.IPhotoView;
import com.hninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.hninstrument.Function.Func_IDCard.mvp.view.IIDCardView;

/**
 * Created by zbsz on 2017/11/27.
 */

public abstract class FunctionActivity  extends Activity implements IPhotoView, IIDCardView {
    public IDCardPresenter idp = IDCardPresenter.getInstance();

    public PhotoPresenter pp = PhotoPresenter.getInstance();

    public SurfaceView surfaceView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        idp.idCardOpen();
        pp.initCamera();
    }

    @Override
    public void onStart() {
        super.onStart();
        pp.setParameter(surfaceView.getHolder());
    }



    @Override
    public void onRestart() {
        super.onRestart();
        pp.initCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        idp.IDCardPresenterSetView(this);
        idp.readCard();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
    }

    @Override
    public void onPause() {
        super.onPause();
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
        pp.PhotoPresenterSetView(null);
    }

}
