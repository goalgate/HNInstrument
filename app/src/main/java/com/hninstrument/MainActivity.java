package com.hninstrument;

import android.content.Intent;
import android.content.res.AssetManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import com.google.zxing.qrcode.encoder.QRCode;
import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Bean.DataFlow.UpOpenDoorData;
import com.hninstrument.Bean.DataFlow.UpPersonRecordData;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.ExitEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.EventBus.TemHumEvent;
import com.hninstrument.Service.SwitchService;
import com.hninstrument.State.OperationState.No_one_OperateState;
import com.hninstrument.State.OperationState.One_man_OperateState;
import com.hninstrument.State.OperationState.Operation;
import com.hninstrument.State.OperationState.Two_man_OperateState;
import com.hninstrument.Tools.DAInfo;
import com.hninstrument.Tools.ServerConnectionUtil;
import com.jakewharton.rxbinding2.widget.RxTextView;

import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cbdi.drv.card.CardInfoRk123x;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends FunctionActivity {

    private SPUtils config = SPUtils.getInstance("config");

    private BaseConfig type = AppInit.getInstrumentConfig();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    UpPersonRecordData upPersonRecordData = new UpPersonRecordData();

    Disposable disposableTips;

    CardInfoRk123x cardInfo;

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    PersonBean person1 = new PersonBean();

    PersonBean person2 = new PersonBean();

    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.img_captured1)
    ImageView captured1;

    @BindView(R.id.tv_info)
    TextView tips;

    @BindView(R.id.iv_network)
    ImageView iv_network;

    @BindView(R.id.iv_lock)
    ImageView iv_lock;

    @BindView(R.id.tv_temp)
    TextView tv_temperature;

    @BindView(R.id.tv_humid)
    TextView tv_humidity;

    @BindView(R.id.iv_humid)
    ImageView iv_humidity;

    @BindView(R.id.iv_temp)
    ImageView iv_temperature;

    @OnClick(R.id.iv_network)
    void network() {
        Bitmap mBitmap = null;
        etName.setText(config.getString("ServerId"));
        dev_name.setText(config.getString("devid"));
        ip_name.setText(NetworkUtils.getIPAddress(true));
        DAInfo di=new DAInfo();
        try {
            di.setId(config.getString("devid"));
            di.setName("数据采集器");
            di.setModel("CBDI-ID");
            di.setSoftwareVer("1.0");
            di.setProject("HNJD");
            mBitmap = di.daInfoBmp();
        }catch (Exception ex){}
        if(mBitmap!=null)
        {
            QRview.setImageBitmap(mBitmap);
        }
        inputServerView.show();
    }

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    Operation operation;

    Intent intent;

    Bitmap headphoto;

    Bitmap photo;

    String persontype;

    GestureLibrary mGestureLib;

    private TextView dev_name;

    private TextView ip_name;

    private EditText etName;

    private ImageView QRview;

    private AlertView inputServerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        if(type.isTemHum()){
            iv_temperature.setVisibility(View.VISIBLE);
            iv_humidity.setVisibility(View.VISIBLE);
        }else{
            iv_temperature.setVisibility(View.INVISIBLE);
            iv_humidity.setVisibility(View.INVISIBLE);
        }

        openService();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        inputServerView = new AlertView("服务器设置,软件版本号为" + AppUtils.getAppVersionName(), null, "取消", new String[]{"确定"}, null, MainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    final String url = etName.getText().toString().replaceAll(" ", "");
                    connectionUtil.post(url + type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=test", url
                            , new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            config.put("ServerId", url);
                                            ToastUtils.showLong("连接服务器成功");
                                        } else {
                                            ToastUtils.showLong("设备验证错误");
                                        }

                                    } else {
                                        ToastUtils.showLong("服务器连接失败");
                                    }
                                }
                            });
                }
            }
        });

        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.inputserver_form, null);
        dev_name = (TextView) extView1.findViewById(R.id.dev_id);
        ip_name = (TextView) extView1.findViewById(R.id.dev_ip);
        etName = (EditText) extView1.findViewById(R.id.server_input);
        QRview = (ImageView) extView1.findViewById(R.id.QRimage) ;
        inputServerView.addExtView(extView1);

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
                .debounce(60, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws
                            Exception {
                        return Observable.just("等待用户操作");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tips.setText(s);
                    }
                });
        operation = new Operation(new No_one_OperateState());

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



    void openService() {
        intent = new Intent(MainActivity.this, SwitchService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        operation.setState(new No_one_OperateState());
        tips.setText("等待用户操作");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new ExitEvent());
        stopService(intent);
        disposableTips.dispose();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.non_wifi));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }


    @Override
    public void onCaremaText(String s) {

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }

    Disposable checkChange;

    @Override
    public void onsetCardInfo(final CardInfoRk123x cardInfo) {
        this.cardInfo = cardInfo;
        if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("1")) {
            if (getState(No_one_OperateState.class)) {
                tips.setText(cardInfo.name() + "刷卡中");
                person1.setCardId(cardInfo.cardId());
                person1.setName(cardInfo.name());
            } else if (getState(One_man_OperateState.class)) {
                person2.setCardId(cardInfo.cardId());
                person2.setName(cardInfo.name());
                if (person1.getCardId().equals(person2.getCardId())) {
                    tips.setText("请不要连续输入同一个管理员的信息");
                    return;
                } else {
                    tips.setText(cardInfo.name() + "刷卡中");
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                }
            } else if (getState(Two_man_OperateState.class)) {
                EventBus.getDefault().post(new CloseDoorEvent());
                iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lockup));
                tips.setText("已进入设防状态");
            }
            pp.capture();
            idp.stopReadCard();
        } else if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("2")) {
            pp.capture();
            idp.stopReadCard();
        } else {
            connectionUtil.post(config.getString("ServerId") + type.getPersonInfoPrefix() + "dataType=queryPersion" + "&daid=" + config.getString("devid") + "&id=" + cardInfo.cardId(), config.getString("ServerId"), new ServerConnectionUtil.Callback() {

                @Override
                public void onResponse(String response) {
                    if (response != null) {
                        if (response.startsWith("true")) {
                            if (response.split("\\|").length > 1) {
                                persontype = response.split("\\|")[1];
                                SPUtils.getInstance("personData").put(cardInfo.cardId(), persontype);
                                if (persontype.equals("1")) {
                                    if (getState(No_one_OperateState.class)) {
                                        person1.setCardId(cardInfo.cardId());
                                        person1.setName(cardInfo.name());
                                    } else if (getState(One_man_OperateState.class)) {
                                        person2.setCardId(cardInfo.cardId());
                                        person2.setName(cardInfo.name());
                                    }
                                }
                                pp.capture();
                                idp.stopReadCard();
                            }
                        } else {
                            persontype = "0";
                            pp.capture();
                            idp.stopReadCard();
                        }
                    } else {
                        tips.setText("人员身份查询：服务器上传出错");
                    }
                }
            });
        }
    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
        photo = bitmapChange(bmp, 0.3f, 0.3f);
        if (persontype.equals("1")) {
            if (getState(No_one_OperateState.class) || getState(One_man_OperateState.class)) {
                if (type.isFace()) {
                    face_upData();
                } else {
                    noface_upData();
                }
            } else if (getState(Two_man_OperateState.class)) {
                operation.doNext();
                pp.setDisplay(surfaceView.getHolder());
                idp.readCard();
            }
        } else if (persontype.equals("2")) {
            checkRecord();
        } else if (persontype.equals("0")) {
            unknownPersonData();
        }
    }

    private void checkRecord() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=2",
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(),
                new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (!getState(Two_man_OperateState.class)) {
                            operation.setState(new No_one_OperateState());
                        }
                        captured1.setImageBitmap(null);
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                        if (response != null) {
                            if (response.startsWith("true")) {
                                tips.setText("巡检数据：巡检成功");
                            } else {
                                tips.setText("巡检数据：上传失败");
                            }
                        } else {
                            tips.setText("巡检数据：无法连接到服务器");
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

    private void face_upData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=faceRecognition" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true") && (int) Double.parseDouble(response.substring(5, response.length())) > 60) {
                                operation.doNext();
                                if (getState(One_man_OperateState.class)) {
                                    person1.setPhoto(photo);
                                    person1.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    photo = bitmapChange(photo, 3.3f, 5f);
                                    captured1.setImageBitmap(photo);
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person1.getFaceReconition());
                                    pp.setDisplay(surfaceView.getHolder());
                                    idp.readCard();
                                    Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                            .compose(MainActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<Long>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {
                                                    checkChange = d;
                                                }

                                                @Override
                                                public void onNext(Long aLong) {
                                                    checkRecord();
                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                }

                                                @Override
                                                public void onComplete() {

                                                }
                                            });
                                } else if ((getState(Two_man_OperateState.class))) {
                                    person2.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    captured1.setImageBitmap(null);
                                    EventBus.getDefault().post(new PassEvent());
                                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_unlock));
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person2.getFaceReconition());
                                    face_openDoorUpData();
                                }
                            } else {
                                tips.setText("仓管员数据：人脸识别结果" + response.substring(5, response.length()) + "，请重试");
                                pp.setDisplay(surfaceView.getHolder());
                                idp.readCard();
                            }
                        } else {
                            tips.setText("仓管员数据：无法连接服务器");
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

    private void noface_upData() {
        operation.doNext();
        if (getState(One_man_OperateState.class)) {
            person1.setPhoto(photo);
            photo = bitmapChange(photo, 3.3f, 5f);
            captured1.setImageBitmap(photo);
            tips.setText("仓管员" + cardInfo.name() + "刷卡成功");
            pp.setDisplay(surfaceView.getHolder());
            idp.readCard();
            Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .compose(MainActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            checkChange = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            checkRecord();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else if ((getState(Two_man_OperateState.class))) {
            person2.setPhoto(photo);
            captured1.setImageBitmap(null);
            EventBus.getDefault().post(new PassEvent());
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_unlock));
            tips.setText("仓管员" + cardInfo.name() + "刷卡成功");
            noface_openDoorUpData();
        }
    }

    private void unknownPersonData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=persionRecord"+ "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                        tips.setText("未知人员信息已上传");
                    }
                });

    }

    private void face_openDoorUpData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        person1.getPhoto().compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=samePsonFaceRecognition"+ "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                        if (response != null) {
                            if (response.startsWith("true")) {
                                connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid") + "&faceRecognition1=" + (person1.getFaceReconition() + 100) + "&faceRecognition2=" + (person2.getFaceReconition() + 100) + "&faceRecognition3=" + ((int) Double.parseDouble(response.substring(5, response.length())) + 100),
                                        config.getString("ServerId"),
                                        new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), photo).toByteArray(),
                                        new ServerConnectionUtil.Callback() {
                                            @Override
                                            public void onResponse(String response) {
                                                if(response!=null){
                                                    tips.setText("开门记录已上传到服务器");
                                                }
                                            }
                                        });
                            } else {
                                tips.setText("开门记录数据：上传失败");
                            }
                        } else {
                            tips.setText("开门记录数据：无法连接到服务器");
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


    private void noface_openDoorUpData() {
        connectionUtil.post(config.getString("ServerId") + type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), person2.getPhoto()).toByteArray(),
                new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if(response!=null){
                            tips.setText("开门记录已上传到服务器");
                        }
                    }
                });


    }

    private Boolean getState(Class stateClass) {
        if (operation.getState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }

    private Bitmap bitmapChange(Bitmap bmp, float width, float height) {
        Matrix matrix = new Matrix();
        matrix.postScale(width, height);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }
}
