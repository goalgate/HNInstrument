package com.hninstrument;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import com.google.zxing.qrcode.encoder.QRCode;
import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Bean.DataFlow.UpCheckRecordData;
import com.hninstrument.Bean.DataFlow.UpOpenDoorData;
import com.hninstrument.Bean.DataFlow.UpPersonRecordData;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.EventBus.AlarmEvent;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.ExitEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.EventBus.TemHumEvent;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Receiver.TimeCheckReceiver;
import com.hninstrument.Service.SwitchService;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.OperationState.No_one_OperateState;
import com.hninstrument.State.OperationState.One_man_OperateState;
import com.hninstrument.State.OperationState.Operation;
import com.hninstrument.State.OperationState.Two_man_OperateState;
import com.hninstrument.Tools.DAInfo;
import com.hninstrument.Tools.NetInfo;
import com.hninstrument.Tools.ServerConnectionUtil;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cbdi.drv.card.CardInfoRk123x;
import cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends FunctionActivity implements AddPersonWindow.OptionTypeListener {

    private SPUtils config = SPUtils.getInstance("config");

    private BaseConfig ins_type = AppInit.getInstrumentConfig();

    private SPUtils staticIP = SPUtils.getInstance("staticIP");

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

    private AddPersonWindow personWindow;

    @OnClick(R.id.iv_network)
    void show() {
        personWindow = new AddPersonWindow(this);
        personWindow.setOptionTypeListener(this);
        personWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private AlertView messageAlert;
    private TextView msg_daid;
    private TextView msg_ip;
    private TextView msg_mac;
    private TextView msg_software;
    private TextView msg_ipmode;
    private TextView msg_network;
    private TextView msg_iccard;
    private TextView msg_lockState;

    private void messageInit() {
        ViewGroup messageView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.message_form, null);
        msg_daid = (TextView) messageView.findViewById(R.id.msg_daid);
        msg_ip = (TextView) messageView.findViewById(R.id.msg_ip);
        msg_mac = (TextView) messageView.findViewById(R.id.msg_mac);
        msg_software = (TextView) messageView.findViewById(R.id.msg_software);
        msg_ipmode = (TextView) messageView.findViewById(R.id.msg_ipmode);
        msg_network = (TextView) messageView.findViewById(R.id.msg_network);
        msg_iccard = (TextView) messageView.findViewById(R.id.msg_iccard);
        msg_lockState = (TextView) messageView.findViewById(R.id.msg_lockState);
        messageAlert = new AlertView("信息显示", null, null, new String[]{"确定"}, null, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {

            }
        });
        messageAlert.addExtView(messageView);
    }


    @OnClick(R.id.iv_lock)
    void showMessage() {
        msg_daid.setText("设备ID：" + config.getString("devid"));
        msg_ip.setText("IP地址：" + NetworkUtils.getIPAddress(true));
        msg_mac.setText("MAC地址：" + new NetInfo().getMac());
        msg_software.setText("软件版本号：" + AppUtils.getAppVersionName());
        if (staticIP.getBoolean("state")) {
            msg_ipmode.setText("当前以太网为静态IP模式");
        } else {
            msg_ipmode.setText("当前以太网为动态IP获取模式");
        }
        if (NetworkUtils.isConnected()) {
            msg_network.setText("连接网络成功");
        } else {
            msg_network.setText("连接网络失败，请检查网线连接状态");
        }
        msg_iccard.setText("请放置身份证进行判断");

        if (Lock.getInstance().getLockState().getClass().getName().equals(State_Lockup.class.getName())) {
            msg_lockState.setText("仓库处于上锁状态");
        } else {
            msg_lockState.setText("仓库处于解锁状态");
        }
        messageAlert.show();
    }

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    Operation operation;

    Intent intent;

    Bitmap headphoto;

    Bitmap photo;

    String persontype;

    GestureLibrary mGestureLib;

    private EditText etName;
    private ImageView QRview;
    private AlertView inputServerView;
    String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Lg.e("mainactivity","oncreate");
        setContentView(R.layout.activity_main);
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
                .debounce(120, TimeUnit.SECONDS)
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

        setGesture();
        ServerInput();
        IpviewInit();
        messageInit();
        /*reboot();*/
    }

    private void ServerInput() {
        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.inputserver_form, null);
        etName = (EditText) extView1.findViewById(R.id.server_input);
        QRview = (ImageView) extView1.findViewById(R.id.QRimage);
        inputServerView = new AlertView("服务器设置,软件版本号为" + AppUtils.getAppVersionName(), null, "取消", new String[]{"确定"}, null, MainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (!etName.getText().toString().replaceAll(" ", "").endsWith("/")) {
                        url = etName.getText().toString() + "/";
                    } else {
                        url = etName.getText().toString();
                    }
                    connectionUtil.post(url + ins_type.getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=test", url
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
        inputServerView.addExtView(extView1);
    }

    long count = 5;
    private AlertView inputStaticIPView;
    EditText et_Static_ip;
    EditText et_Static_mask;
    EditText et_Static_gateway;
    EditText et_Static_dns1;
    EditText et_Static_dns2;
    CheckBox ipCheckBox;

    private void IpviewInit() {
        ViewGroup ipview = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.inputstaticip_form, null);
        ipCheckBox = (CheckBox) ipview.findViewById(R.id.ip_checkBox);
        et_Static_ip = (EditText) ipview.findViewById(R.id.static_ip);
        et_Static_mask = (EditText) ipview.findViewById(R.id.static_mask);
        et_Static_gateway = (EditText) ipview.findViewById(R.id.static_gateway);
        et_Static_dns1 = (EditText) ipview.findViewById(R.id.static_DNS1);
        et_Static_dns2 = (EditText) ipview.findViewById(R.id.static_DNS2);
        ipCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_Static_ip.setEnabled(true);
                    et_Static_mask.setEnabled(true);
                    et_Static_gateway.setEnabled(true);
                    et_Static_dns1.setEnabled(true);
                    et_Static_dns2.setEnabled(true);
                } else {
                    et_Static_ip.setEnabled(false);
                    et_Static_mask.setEnabled(false);
                    et_Static_gateway.setEnabled(false);
                    et_Static_dns1.setEnabled(false);
                    et_Static_dns2.setEnabled(false);
                }
            }
        });
        inputStaticIPView = new AlertView("设置静态IP", null, "取消", new String[]{"确定"}, null, MainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (ipCheckBox.isChecked()) {
                        if (RegexUtils.isIP(et_Static_ip.getText().toString()) ||
                                RegexUtils.isIP(et_Static_mask.getText().toString()) ||
                                RegexUtils.isIP(et_Static_gateway.getText().toString()) ||
                                RegexUtils.isIP(et_Static_dns1.getText().toString()) ||
                                RegexUtils.isIP(et_Static_dns2.getText().toString())) {
                            staticIP.put("Static_ip", et_Static_ip.getText().toString());
                            staticIP.put("Static_mask", et_Static_mask.getText().toString());
                            staticIP.put("Static_gateway", et_Static_gateway.getText().toString());
                            staticIP.put("Static_dns1", et_Static_dns1.getText().toString());
                            staticIP.put("Static_dns2", et_Static_dns2.getText().toString());
                            staticIP.put("state", true);
                            AppInit.getMyManager().setStaticEthIPAddress(et_Static_ip.getText().toString(),
                                    et_Static_gateway.getText().toString(), et_Static_mask.getText().toString(),
                                    et_Static_dns1.getText().toString(), et_Static_dns2.getText().toString());
                            ToastUtils.showLong("静态IP已设置");
                            Observable.interval(0, 1, TimeUnit.SECONDS)
                                    .take(count + 1)
                                    .map(new Function<Long, Long>() {
                                        @Override
                                        public Long apply(@NonNull Long aLong) throws Exception {
                                            return count - aLong;
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Long>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onNext(@NonNull Long aLong) {
                                            ToastUtils.showLong(aLong + "秒后重新开机保存设置");
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            pp.close_Camera();
                                            AppInit.getMyManager().reboot();
                                        }
                                    });
                        } else {
                            ToastUtils.showLong("IP地址输入格式有误，请重试");
                        }
                    } else {
                        if (Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(32, 40)) >= 20171212) {
                            AppInit.getMyManager().setDhcpIpAddress(AppInit.getContext());
                            ToastUtils.showLong("已设置为动态IP获取模式");
                            staticIP.put("state", false);
                            Observable.interval(0, 1, TimeUnit.SECONDS)
                                    .take(count + 1)
                                    .map(new Function<Long, Long>() {
                                        @Override
                                        public Long apply(@NonNull Long aLong) throws Exception {
                                            return count - aLong;
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Long>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onNext(@NonNull Long aLong) {
                                            ToastUtils.showLong(aLong + "秒后重新开机保存设置");
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            pp.close_Camera();
                                            AppInit.getMyManager().reboot();
                                        }
                                    });
                        } else {
                            ToastUtils.showLong("该固件版本过低，无法完成到动态获取以太网的转变");
                        }
                    }
                }
            }
        });
        inputStaticIPView.addExtView(ipview);
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
            Bitmap mBitmap = null;
            etName.setText(config.getString("ServerId"));
            DAInfo di = new DAInfo();
            try {
                di.setId(config.getString("devid"));
                di.setName(ins_type.getName());
                di.setModel(ins_type.getModel());
                di.setPower(ins_type.getPower());
                di.setSoftwareVer(AppUtils.getAppVersionName());
                di.setProject(ins_type.getProject());
                mBitmap = di.daInfoBmp();
            } catch (Exception ex) {
            }
            if (mBitmap != null) {
                QRview.setImageBitmap(mBitmap);
            }
            inputServerView.show();
        } else if (type == 2) {
            if (staticIP.getBoolean("state")) {
                ipCheckBox.setChecked(true);
            } else {
                ipCheckBox.setChecked(false);
            }
            if (!TextUtils.isEmpty(staticIP.getString("Static_ip"))) {
                et_Static_ip.setText(staticIP.getString("Static_ip"));
                et_Static_gateway.setText(staticIP.getString("Static_gateway"));
                et_Static_mask.setText(staticIP.getString("Static_mask"));
                et_Static_dns1.setText(staticIP.getString("Static_dns1"));
                et_Static_dns2.setText(staticIP.getString("Static_dns2"));
            }
            if (ipCheckBox.isChecked()) {
                et_Static_ip.setEnabled(true);
                et_Static_mask.setEnabled(true);
                et_Static_gateway.setEnabled(true);
                et_Static_dns1.setEnabled(true);
                et_Static_dns2.setEnabled(true);
            }
            inputStaticIPView.show();
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
        //stopService(intent);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event){
        tips.setText("开门报警已被触发");
        //AppInit.getSpeaker().playText("开门报警已被触发");
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
        if (messageAlert.isShowing()) {
            msg_iccard.setText("身份证号为："+cardInfo.cardId());
        } else {
            this.cardInfo = cardInfo;
            tips.setText(cardInfo.name() + "刷卡中");
            if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("1")) {
                if (getState(No_one_OperateState.class)) {
                    person1.setCardId(cardInfo.cardId());
                    person1.setName(cardInfo.name());
                } else if (getState(One_man_OperateState.class)) {
                    person2.setCardId(cardInfo.cardId());
                    person2.setName(cardInfo.name());
                    if (person1.getCardId().equals(person2.getCardId())) {
                        tips.setText("请不要连续输入同一个管理员的信息");
                        //AppInit.getSpeaker().playText("请不要连续输入同一个管理员的信息");
                        return;
                    } else {
                        if (checkChange != null) {
                            checkChange.dispose();
                        }
                    }
                } else if (getState(Two_man_OperateState.class)) {
                    EventBus.getDefault().post(new CloseDoorEvent());
                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lockup));
                    tips.setText("已进入设防状态");
                    //AppInit.getSpeaker().playText("已进入设防状态");
                }
                pp.capture();
                idp.stopReadCard();
            } else if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("2")) {
                pp.capture();
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
                          //  AppInit.getSpeaker().playText("服务器上传出错");
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
        photo = bitmapChange(bmp, 0.3f, 0.3f);
        if (persontype.equals("1")) {
            if (getState(No_one_OperateState.class) || getState(One_man_OperateState.class)) {
                if (ins_type.isFace()) {
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
        SwitchPresenter.getInstance().OutD9(false);
        Intent checked = new Intent(MainActivity.this,TimeCheckReceiver.class);
        checked.setAction("checked");
        sendBroadcast(checked);
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=2",
                config.getString("ServerId"),
                new UpCheckRecordData().toCheckRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(),
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
                              //  AppInit.getSpeaker().playText("巡检成功");
                            } else {
                                tips.setText("巡检数据：上传失败");
                              //  AppInit.getSpeaker().playText("巡检数据上传失败");
                            }
                        } else {
                            tips.setText("巡检数据：无法连接到服务器");
                           // AppInit.getSpeaker().playText("无法连接到服务器");
                        }
                    }
                });
    }

    private void face_upData() {
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
                                if (getState(One_man_OperateState.class)) {
                                    person1.setPhoto(photo);
                                    person1.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    captured1.setImageBitmap(photo);
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person1.getFaceReconition());
                                   // AppInit.getSpeaker().playText("打卡成功");
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
                                    //AppInit.getSpeaker().playText("打卡成功");
                                    face_openDoorUpData();
                                }
                            } else {
                                tips.setText("仓管员数据：人脸识别结果" + response.substring(5, response.length()) + "，请重试");
                                //AppInit.getSpeaker().playText("人脸识别不通过");
                                pp.setDisplay(surfaceView.getHolder());
                                idp.readCard();
                            }
                        } else {
                            tips.setText("仓管员数据：无法连接服务器");
                           // AppInit.getSpeaker().playText("无法连接服务器");
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
          //  AppInit.getSpeaker().playText("打卡成功");
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
           // AppInit.getSpeaker().playText("打卡成功");
            noface_openDoorUpData();
        }
    }

    private void unknownPersonData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=persionRecord" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                        tips.setText("未知人员信息已上传");
                      //  AppInit.getSpeaker().playText("未知人员信息已上传");
                    }
                });

    }

    private void face_openDoorUpData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        person1.getPhoto().compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=samePsonFaceRecognition" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                        if (response != null) {
                            if (response.startsWith("true") && (int) Double.parseDouble(response.substring(5, response.length())) < 60) {//这里要改
                                connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid") + "&faceRecognition1=" + (person1.getFaceReconition() + 100) + "&faceRecognition2=" + (person2.getFaceReconition() + 100) + "&faceRecognition3=" + ((int) Double.parseDouble(response.substring(5, response.length())) + 100),
                                        config.getString("ServerId"),
                                        new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), photo).toByteArray(),
                                        new ServerConnectionUtil.Callback() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (response != null) {
                                                    tips.setText("开门记录已上传到服务器");
                                                }else{
                                                    tips.setText("无法连接到服务器");
                                                   //AppInit.getSpeaker().playText("无法连接到服务器");
                                                }
                                            }
                                        });
                            } else {
                                tips.setText("上传失败，请注意是否单人双卡操作");
                                //AppInit.getSpeaker().playText("数据上传失败，请注意是否单人双卡操作");
                            }
                        } else {
                            tips.setText("开门记录数据：无法连接到服务器");
                            //AppInit.getSpeaker().playText("无法连接到服务器");
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
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), person2.getPhoto()).toByteArray(),
                new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            tips.setText("开门记录已上传到服务器");
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
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
