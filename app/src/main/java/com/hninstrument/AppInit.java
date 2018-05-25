package com.hninstrument;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Config.GDMB_Config;
import com.hninstrument.Config.GZ_Config;
import com.hninstrument.Config.HLJ_Config;
import com.hninstrument.Config.HN_Config;
import com.hninstrument.Config.HeiBei_Config;
import com.hninstrument.Config.HuBeiFB_Config;
import com.hninstrument.Config.HuBeiWeiHua_Config;
import com.hninstrument.Config.SH_Config;
import com.hninstrument.Config.XA_Config;
import com.hninstrument.Tools.AssetsUtils;
import com.hninstrument.Tools.DaoMaster;
import com.hninstrument.Tools.DaoSession;
import com.squareup.leakcanary.LeakCanary;

import com.ys.myapi.MyManager;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/11/25.
 */

public class AppInit extends Application {
    protected static AppInit instance;

    protected static BaseConfig InstrumentConfig;

    protected static MyManager manager;

    public static MyManager getMyManager() {
        return manager;
    }

    public static BaseConfig getInstrumentConfig() {
        return InstrumentConfig;
    }

    public static AppInit getInstance() {
        return instance;
    }

    private DaoSession daoSession;

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    @Override
    public void onCreate() {

        super.onCreate();

        Lg.setIsSave(true);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        instance = this;

        InstrumentConfig = new SH_Config();

        manager = MyManager.getInstance(this);

        manager.bindAIDLService(this);
        Utils.init(getContext());



        //initDatabase();

    }





    public DaoSession getDaoSession() {
        return daoSession;
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "HN_unUploadPackage-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

}
