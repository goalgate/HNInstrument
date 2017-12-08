package com.hninstrument;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.hninstrument.Tools.SafeCheck;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.greendao.database.Database;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cbdi.log.Lg;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/11/25.
 */

public class AppInit extends Application {
    protected static AppInit instance;


    public static AppInit getInstance() {
        return instance;
    }



    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    SPUtils SP_Config;

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

 /*   public DaoSession getDaoSession() {
        return daoSession;
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "HN_unUploadPackage-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }*/
    }
}
