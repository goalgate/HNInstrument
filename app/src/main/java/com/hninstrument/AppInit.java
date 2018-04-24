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
import com.hninstrument.Config.HuBeiFB_Config;
import com.hninstrument.Config.HuBeiWeiHua_Config;
import com.hninstrument.Config.SH_Config;
import com.hninstrument.Config.XA_Config;
import com.hninstrument.Tools.AssetsUtils;
import com.hninstrument.Tools.DaoMaster;
import com.hninstrument.Tools.DaoSession;
import com.squareup.leakcanary.LeakCanary;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;
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

    private static SpeechSynthesizer mTTSPlayer;

 /*   public static SpeechSynthesizer getSpeaker(){
        return mTTSPlayer;
    }*/

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

        InstrumentConfig = new HuBeiWeiHua_Config();

        manager = MyManager.getInstance(this);

        Utils.init(getContext());

        //initTts();

        //initDatabase();

    }



    private final String mFrontendModel= "/sdcard/sound/frontend_model";
    private final String mBackendModel = "/sdcard/sound/backend_lzl";
    private static final String appKey = "ykx33znzklrnnad3hiod64tiw6humkocn2c44dak";
    private static final String secret = "048a0805f96f3054c0f59d28ff323f56";
    private void initTts() {

        if(SPUtils.getInstance("config").getBoolean("soundInit",true)){
            AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("sound","sound" );
            SPUtils.getInstance("config").put("soundInit",false);
        }

        // 初始化语音合成对象
        mTTSPlayer = new SpeechSynthesizer(this, appKey, secret);
        // 设置本地合成
        mTTSPlayer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_LOCAL);/*
        File _FrontendModelFile = new File(mFrontendModel);
        if (!_FrontendModelFile.exists()) {
            ToastUtils.showLong("文件：" + mFrontendModel + "不存在，请将assets下相关文件拷贝到SD卡指定目录！");
        }
        File _BackendModelFile = new File(mBackendModel);
        if (!_BackendModelFile.exists()) {
            ToastUtils.showLong("文件：" + mBackendModel + "不存在，请将assets下相关文件拷贝到SD卡指定目录！");
        }*/
        // 设置前端模型
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_FRONTEND_MODEL_PATH, mFrontendModel);
        // 设置后端模型
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_BACKEND_MODEL_PATH, mBackendModel);
        // 设置回调监听
        mTTSPlayer.setTTSListener(new SpeechSynthesizerListener() {
            @Override
            public void onEvent(int type) {
                switch (type) {
                    case SpeechConstants.TTS_EVENT_INIT:
                        // 初始化成功回调
                        //log_i("onInitFinish");
                        // mTTSPlayBtn.setEnabled(true);
                        break;
                    case SpeechConstants.TTS_EVENT_SYNTHESIZER_START:
                        // 开始合成回调
                        //log_i("beginSynthesizer");
                        break;
                    case SpeechConstants.TTS_EVENT_SYNTHESIZER_END:
                        // 合成结束回调
                        //log_i("endSynthesizer");
                        break;
                    case SpeechConstants.TTS_EVENT_BUFFER_BEGIN:
                        // 开始缓存回调
                        //log_i("beginBuffer");
                        break;
                    case SpeechConstants.TTS_EVENT_BUFFER_READY:
                        // 缓存完毕回调
                        //log_i("bufferReady");
                        break;
                    case SpeechConstants.TTS_EVENT_PLAYING_START:
                        // 开始播放回调
                        // log_i("onPlayBegin");
                        break;
                    case SpeechConstants.TTS_EVENT_PLAYING_END:
                        // 播放完成回调
                        // log_i("onPlayEnd");
                        //setTTSButtonReady();
                        break;
                    case SpeechConstants.TTS_EVENT_PAUSE:
                        // 暂停回调
                        //log_i("pause");
                        break;
                    case SpeechConstants.TTS_EVENT_RESUME:
                        // 恢复回调
                        //log_i("resume");
                        break;
                    case SpeechConstants.TTS_EVENT_STOP:
                        // 停止回调
                        //log_i("stop");
                        break;
                    case SpeechConstants.TTS_EVENT_RELEASE:
                        // 释放资源回调
                        //log_i("release");
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onError(int type, String errorMSG) {
                // 语音合成错误回调
               /* log_i("onError");
                toastMessage(errorMSG);
                setTTSButtonReady();*/
            }
        });
        // 初始化合成引擎
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        mTTSPlayer.init("");

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

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
