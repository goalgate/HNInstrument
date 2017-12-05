package com.hninstrument;

import android.app.Application;
import android.content.Context;

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

    private static final String PREFS_NAME = "UserInfo";




    public static AppInit getInstance() {
        return instance;
    }



    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    SPUtils User_SP;

    SPUtils data;



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

     /*   User_SP = SPUtils.getInstance(PREFS_NAME);

        if (User_SP.getBoolean("firstStart", true)) {
            JSONObject jsonKey = new JSONObject();
            try {
                jsonKey.put("daid", new NetInfo().getMacId());
                jsonKey.put("check", DESX.encrypt(new NetInfo().getMacId()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            User_SP.put("firstStart", false);
            User_SP.put("dev_id", new NetInfo().getMacId());
            User_SP.put("jsonKey", DESX.encrypt(jsonKey.toString()));
            User_SP.put("server", "http://192.168.11.165:8080/");
        }

        initDatabase();

        RetrofitGenerator.getCommonApi().commonFunction(RequestEnvelope.GetRequestEnvelope(
                new OnlyPutKeyModule(downPersonInfo)))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseEnvelope>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseEnvelope responseEnvelope) {
                        List<String> dataList = responseEnvelope.body.downPersonInfoResponse.Listinfo;
                        String result = dataList.get(0);
                        Map<String, String> infoMap = new Gson().fromJson(result,
                                new TypeToken<HashMap<String, String>>() {
                                }.getType());
                        if (infoMap.get("result").equals("checkErr")) {
                            ToastUtils.showLong("设备出错");
                            return;
                        } else if (infoMap.get("result").equals("noData")) {
                            ToastUtils.showLong("找不到相应的数据");
                            return;
                        } else if (infoMap.get("result").equals("true")) {
                            dataList.remove(0);
                            for (String siminfo : dataList) {
                                Map<String, String> simInfoMap = new Gson().fromJson(siminfo,
                                        new TypeToken<HashMap<String, String>>() {
                                        }.getType());
                                data = SPUtils.getInstance(simInfoMap.get("fp_id"));
                                data.put("id", simInfoMap.get("id"));
                                data.put("name", simInfoMap.get("name"));
                                data.put("type", simInfoMap.get("personType"));
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }*/

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
