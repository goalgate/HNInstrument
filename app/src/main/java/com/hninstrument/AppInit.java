package com.hninstrument;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.blankj.utilcode.util.Utils;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Config.GDMB_Config;
import com.hninstrument.Config.HNFB_Config;
import com.hninstrument.Config.HeNanYZB_Config;
import com.hninstrument.Config.HuBeiWeiHua_Config;
import com.hninstrument.Config.LN_Config;
import com.hninstrument.Config.XAYZB_Config;
import com.hninstrument.Config.XA_Config;
import com.hninstrument.Tools.WZWManager;
import com.hninstrument.greendao.DaoMaster;
import com.hninstrument.greendao.DaoSession;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

import cbdi.log.Lg;

/**
 * Created by zbsz on 2017/11/25.
 */

public class AppInit extends Application {

    private DaoMaster.DevOpenHelper mHelper;

    private SQLiteDatabase db;

    private DaoMaster mDaoMaster;

    private DaoSession mDaoSession;

    protected static AppInit instance;

    protected static BaseConfig InstrumentConfig;

    protected static WZWManager manager;

    public static WZWManager getMyManager() {
        return manager;
    }

    public static BaseConfig getInstrumentConfig() {
        return InstrumentConfig;
    }

    public static AppInit getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    @Override
    public void onCreate() {

        super.onCreate();

        Lg.setIsSave(false);

        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Exception/");

        if(!file.exists()){
            file.mkdir();
        }

        Lg.setLogPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "Exception/");

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        instance = this;

        InstrumentConfig = new GDMB_Config();

        manager = WZWManager.getInstance(this);

        manager.bindAIDLService(this);

        Utils.init(getContext());

        setDatabase();

    }

    private void setDatabase() {
        mHelper = new DaoMaster.DevOpenHelper(this, "reUpload-db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
