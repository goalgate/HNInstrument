package com.hninstrument.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.AppInit;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Tools.Checked;
import com.hninstrument.Tools.ServerConnectionUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zbsz on 2018/3/7.
 */

public class TimeCheckReceiver extends BroadcastReceiver {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

    private volatile Checked checked = Checked.getInstance();

    Intent final_intent;

    private BaseConfig type = AppInit.getInstrumentConfig();

    private SPUtils config = SPUtils.getInstance("config");

    public void onReceive(Context context, Intent intent) {
        final_intent = intent;
        // TODO Auto-generated method stub
        if (intent.getAction().equals("checkTime")) {
            checked.setFlag(false);
            ToastUtils.showLong("巡检前5分钟");
            Lg.e(final_intent.getStringExtra("time"), "巡检前5分钟");
            Observable.timer(5, TimeUnit.MINUTES)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(Long aLong) {
                            if(checked.isFlag()){
                                Lg.e(final_intent.getStringExtra("time"), "已巡检");
                            }else{
                                ToastUtils.showLong("巡检到点");
                                Lg.e(final_intent.getStringExtra("time"), "巡检到点");
                                Observable.timer(5, TimeUnit.MINUTES)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Observer<Long>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                            }

                                            @Override
                                            public void onNext(Long aLong) {
                                                if(checked.isFlag()){
                                                    Lg.e(final_intent.getStringExtra("time"), "已巡检");
                                                }else{
                                                    ToastUtils.showLong("巡检预警超5分钟，数据已上传");
                                                    Lg.e(final_intent.getStringExtra("time"), "巡检后5分钟");
                                                    new ServerConnectionUtil().post(config.getString("ServerId")+ type.getUpDataPrefix()+"daid=" + config.getString("devid") + "&dataType=alarm&alarmType=11"+ "&time=" +formatter.format(new Date(System.currentTimeMillis())),
                                                            config.getString("ServerId"),new ServerConnectionUtil.Callback() {
                                                                @Override
                                                                public void onResponse(String response) {

                                                                }
                                                            });
                                                }

                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else if (intent.getAction().equals("checked")) {
            Lg.e("信息提示：", "已巡检");
            checked.setFlag(true);
        }
    }


}
