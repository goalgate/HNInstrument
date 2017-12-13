package com.hninstrument;

import android.app.Application;
import android.content.Context;
import com.blankj.utilcode.util.Utils;
import com.hninstrument.Tools.DaoMaster;
import com.hninstrument.Tools.DaoSession;
import com.squareup.leakcanary.LeakCanary;
import org.greenrobot.greendao.database.Database;
import cbdi.log.Lg;

/**
 * Created by zbsz on 2017/11/25.
 */

public class AppInit extends Application {
    protected static AppInit instance;

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

        Utils.init(getContext());

        initDatabase();

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
