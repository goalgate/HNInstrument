package com.hninstrument;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.hninstrument.Bean.DataFlow.ReUploadBean;
import com.hninstrument.Bean.DataFlow.UpCheckRecordData;
import com.hninstrument.Config.HN_Config;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.ExitEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Receiver.TimeCheckReceiver;
import com.hninstrument.Service.SwitchService;
import com.hninstrument.State.OperationState.LockingState;
import com.hninstrument.State.OperationState.OneUnlockState;
import com.hninstrument.State.OperationState.TwoUnlockState;
import com.hninstrument.Tools.MediaHelper;
import com.hninstrument.Tools.ServerConnectionUtil;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cbdi.drv.card.ICardInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CBSD_CommonActivity extends CBSD_FunctionActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Disposable disposableTips;

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    Intent intent;
    GestureLibrary mGestureLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if(config.getString("ServerId").equals(new HN_Config().getServerId())){
//            config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
//        }
       // config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        if (ins_type.isTemHum()) {
            iv_temperature.setVisibility(View.VISIBLE);
            iv_humidity.setVisibility(View.VISIBLE);
        } else {
            iv_temperature.setVisibility(View.INVISIBLE);
            iv_humidity.setVisibility(View.INVISIBLE);
        }
        openService();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        tv_time.setText(formatter.format(new Date(System.currentTimeMillis())));
                    }
                });

        disposableTips = RxTextView.textChanges(tips)
                .debounce(20, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws
                            Exception {
                        return Observable.just(config.getString("devid") + "号机器等待用户操作");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tips.setText(s);
                    }
                });

        setGesture();
    }



    private void setGesture() {
        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        gestures.setGestureVisible(false);
        gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay,
                                           Gesture gesture) {
                ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = (Prediction) predictions.get(0);
                    // 匹配的手势
                    if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
                        if (prediction.name.equals("setting")) {
                            NetworkUtils.openWirelessSettings();
                        }
                    }
                }
            }
        });
        if (mGestureLib == null) {
            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            mGestureLib.load();
        }
    }

    @Override
    public void onOptionType(Button view, int type) {
        personWindow.dismiss();
        if (type == 1) {
            alert_server.show();
        } else if (type == 2) {
            alert_ip.show();
        }
    }

    void openService() {
        intent = new Intent(this, SwitchService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        tips.setText(config.getString("devid") + "号机器等待用户操作");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new ExitEvent());
        //stopService(intent);
        disposableTips.dispose();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCaremaText(String s) {

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.cardId());
        } else {
            this.cardInfo = cardInfo;
            tips.setText(cardInfo.name() + "刷卡中，请稍后");
            if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("1")) {
                if (getState(LockingState.class)) {
                    person1.setCardId(cardInfo.cardId());
                    person1.setName(cardInfo.name());
                } else if (getState(OneUnlockState.class)) {
                    person2.setCardId(cardInfo.cardId());
                    person2.setName(cardInfo.name());
                    if (person1.getCardId().equals(person2.getCardId())) {
                        tips.setText("请不要连续输入同一个管理员的信息");
                        MediaHelper.play(MediaHelper.Text.err_samePerson);
                        return;
                    } else {
                        if (checkChange != null) {
                            checkChange.dispose();
                        }
                    }
                } else if (getState(TwoUnlockState.class)) {
                    EventBus.getDefault().post(new CloseDoorEvent());
                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lockup));
                    tips.setText("已进入设防状态");
                    // MediaHelper.play(MediaHelper.Text.relock_opt);
                }
                if (!ins_type.isGetOneShot()) {
                    pp.capture();
                } else {
                    pp.getOneShut();
                }
                idp.stopReadCard();
            } else if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("2")) {
                if (!ins_type.isGetOneShot()) {
                    pp.capture();
                } else {
                    pp.getOneShut();
                }
                idp.stopReadCard();
            } else {
                connectionUtil.post(config.getString("ServerId") + ins_type.getPersonInfoPrefix() + "dataType=queryPersion" + "&daid=" + config.getString("devid") + "&id=" + cardInfo.cardId(), config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                if (response.split("\\|").length > 1) {
                                    persontype = response.split("\\|")[1];
                                    SPUtils.getInstance("personData").put(cardInfo.cardId(), persontype);
                                    if (persontype.equals("1")) {
                                        if (getState(LockingState.class)) {
                                            person1.setCardId(cardInfo.cardId());
                                            person1.setName(cardInfo.name());
                                        } else if (getState(OneUnlockState.class)) {
                                            person2.setCardId(cardInfo.cardId());
                                            person2.setName(cardInfo.name());
                                        }
                                    }
                                    if (!ins_type.isGetOneShot()) {
                                        pp.capture();
                                    } else {
                                        pp.getOneShut();
                                    }
                                    idp.stopReadCard();
                                }
                            } else {
                                persontype = "0";
                                if (!ins_type.isGetOneShot()) {
                                    pp.capture();
                                } else {
                                    pp.getOneShut();
                                }
                                idp.stopReadCard();
                            }
                        } else {
                            tips.setText("人员身份查询：服务器上传出错");
//                            MediaHelper.play(MediaHelper.Text.err_connect);
                            persontype = "0";
                            if (!ins_type.isGetOneShot()) {
                                pp.capture();
                            } else {
                                pp.getOneShut();
                            }
                            idp.stopReadCard();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
        photo = compressImage(bmp);
        if (persontype.equals("1")) {
            // if (!persontype.equals("5")) {
            if (getState(LockingState.class) || getState(OneUnlockState.class)) {
                if (ins_type.isFace()) {
                    face_upData();
                } else {
                    noface_upData();
                }
            } else if (getState(TwoUnlockState.class)) {
                operation.doNext();
                pp.setDisplay(surfaceView.getHolder());
                idp.readCard();
            }
        } else if (persontype.equals("2")) {
            if (checkChange != null) {
                checkChange.dispose();
            }
            checkRecord(2);
        } else if (persontype.equals("3")) {
            if (checkChange != null) {
                checkChange.dispose();
            }
            checkRecord(3);
        } else if (persontype.equals("0")) {
            unknownPersonData();
        }
    }

    void noface_upData() {
        operation.doNext();
        if (getState(OneUnlockState.class)) {
            person1.setPhoto(photo);
            captured1.setImageBitmap(photo);
            tips.setText("仓管员" + cardInfo.name() + "刷卡成功");
            MediaHelper.play(MediaHelper.Text.first_opt);
            pp.setDisplay(surfaceView.getHolder());
            idp.readCard();
            Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .compose(CBSD_CommonActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            checkChange = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            checkRecord(2);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else if ((getState(TwoUnlockState.class))) {
            person2.setPhoto(photo);
            captured1.setImageBitmap(null);
            EventBus.getDefault().post(new PassEvent());
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_unlock));
            tips.setText("仓管员" + cardInfo.name() + "刷卡成功");
            MediaHelper.play(MediaHelper.Text.second_opt);
            noface_openDoorUpData();
        }
    }

    void face_upData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=faceRecognition" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true") && (int) Double.parseDouble(response.substring(5, response.length())) > 60) {
                                operation.doNext();
                                if (getState(OneUnlockState.class)) {
                                    person1.setPhoto(photo);
                                    person1.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    captured1.setImageBitmap(photo);
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person1.getFaceReconition());
                                    MediaHelper.play(MediaHelper.Text.first_opt);
                                    pp.setDisplay(surfaceView.getHolder());
                                    idp.readCard();
                                    Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                            .compose(CBSD_CommonActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<Long>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {
                                                    checkChange = d;
                                                }

                                                @Override
                                                public void onNext(Long aLong) {
                                                    checkRecord(2);
                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                }

                                                @Override
                                                public void onComplete() {

                                                }
                                            });
                                } else if ((getState(TwoUnlockState.class))) {
                                    person2.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    captured1.setImageBitmap(null);
                                    EventBus.getDefault().post(new PassEvent());
                                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_unlock));
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person2.getFaceReconition());
                                    MediaHelper.play(MediaHelper.Text.second_opt);
                                    face_openDoorUpData();
                                }
                            } else {
                                try{
                                    tips.setText("人脸比对失败，分数为"+String.valueOf(Double.parseDouble(response.substring(5, response.length()))));
                                    pp.setDisplay(surfaceView.getHolder());
                                    idp.readCard();
                                }catch (Exception e){
                                    tips.setText("人脸比对失败");

                                }

                            }
                        } else {
                            tips.setText("仓管员数据：无法连接服务器");
                            MediaHelper.play(MediaHelper.Text.err_connect_ns);
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
                        }
                    }
                });
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void checkRecord(final int type) {
        SwitchPresenter.getInstance().OutD9(false);
        Intent checked = new Intent(this, TimeCheckReceiver.class);
        checked.setAction("checked");
        sendBroadcast(checked);

        if (checkChange != null && !checkChange.isDisposed()) {
            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=" + type,
                    config.getString("ServerId"),
                    new UpCheckRecordData().toCheckRecordData(person1.getCardId(),/*cardInfo.cardId(),*/ person1.getPhoto(), person1.getName()).toByteArray(),
                    new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {
                            if (!getState(TwoUnlockState.class)) {
                                operation.setState(new LockingState());
                            }
                            captured1.setImageBitmap(null);
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
                            if (response != null) {
                                if (response.startsWith("true")) {
                                    tips.setText("巡检数据：巡检成功");
                                    MediaHelper.play(MediaHelper.Text.msg_patrol);
                                } else {
                                    tips.setText("巡检数据：上传失败");
                                    MediaHelper.play(MediaHelper.Text.err_upload);
                                }
                            } else {
                                tips.setText("巡检数据：无法连接到服务器");
                                MediaHelper.play(MediaHelper.Text.err_connect);
                                mdaoSession.insert(new ReUploadBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(person1.getCardId(),/*cardInfo.cardId(),*/ person1.getPhoto(), person1.getName()).toByteArray(), type));
                            }
                        }
                    });
        } else {
            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=" + type,
                    config.getString("ServerId"),
                    new UpCheckRecordData().toCheckRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(),
                    new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {
                            if (!getState(TwoUnlockState.class)) {
                                operation.setState(new LockingState());
                            }
                            captured1.setImageBitmap(null);
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
                            if (response != null) {
                                if (response.startsWith("true")) {
                                    tips.setText("巡检数据：巡检成功");
                                    MediaHelper.play(MediaHelper.Text.msg_patrol);
                                } else {
                                    tips.setText("巡检数据：上传失败");
                                    MediaHelper.play(MediaHelper.Text.err_upload);
                                }
                            } else {
                                tips.setText("巡检数据：无法连接到服务器");
                                MediaHelper.play(MediaHelper.Text.err_connect);
                                mdaoSession.insert(new ReUploadBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), type));
                            }
                        }
                    });

        }
    }

    void unknownPersonData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=persionRecord" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                tips.setText("来访人员信息已上传");
                                MediaHelper.play(MediaHelper.Text.msg_visit);
                            } else {
                                tips.setText("设备尚未登记,请前往系统进行登记操作");
                                MediaHelper.play(MediaHelper.Text.no_registration);
                            }
                        } else {
                            tips.setText("来访人员信息：无法连接到服务器");
                            MediaHelper.play(MediaHelper.Text.err_connect);
                            mdaoSession.insert(new ReUploadBean(null, "dataType=persionRecord", upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), 0));
                        }
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                    }
                });
    }
}
