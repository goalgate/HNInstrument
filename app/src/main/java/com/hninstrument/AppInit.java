package com.hninstrument;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.blankj.utilcode.util.Utils;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Config.GDMB_Config;
import com.hninstrument.Config.HNTest_Config;
import com.hninstrument.Config.HeBeiDanNing_Config;
import com.hninstrument.Config.HeBei_Config;
import com.hninstrument.Config.SHDMJ_config;
import com.hninstrument.Config.SH_Config;
import com.hninstrument.Config.WZ_Config;
import com.hninstrument.Config.XA_Config;
import com.hninstrument.greendao.DaoMaster;
import com.hninstrument.greendao.DaoSession;
import com.squareup.leakcanary.LeakCanary;
import com.ys.myapi.MyManager;
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

        InstrumentConfig = new WZ_Config();

        manager = MyManager.getInstance(this);

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
