package com.hninstrument;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.Alerts.Alert_IP;
import com.hninstrument.Alerts.Alert_Message;
import com.hninstrument.Alerts.Alert_Server;
import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Bean.DataFlow.ReUploadBean;
import com.hninstrument.Bean.DataFlow.UpOpenDoorData;
import com.hninstrument.Bean.DataFlow.UpPersonRecordData;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.Config.HeBeiDanNing_Config;
import com.hninstrument.Config.HeBei_Config;
import com.hninstrument.Config.SHDMJ_config;
import com.hninstrument.EventBus.AlarmEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.TemHumEvent;
import com.hninstrument.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.hninstrument.Function.Func_Camera.mvp.view.IPhotoView;
import com.hninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.hninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Unlock;
import com.hninstrument.State.OperationState.LockingState;
import com.hninstrument.State.OperationState.Operation;
import com.hninstrument.Tools.MediaHelper;
import com.hninstrument.Tools.ServerConnectionUtil;
import com.hninstrument.greendao.DaoSession;
import com.hninstrument.greendao.ReUploadBeanDao;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cbdi.drv.card.ICardInfo;
import cbdi.log.Lg;
import io.reactivex.disposables.Disposable;

public abstract class CBSD_FunctionActivity extends RxActivity implements IPhotoView, IIDCardView, AddPersonWindow.OptionTypeListener {
    public IDCardPresenter idp = IDCardPresenter.getInstance();

    public PhotoPresenter pp = PhotoPresenter.getInstance();

    public SurfaceView surfaceView;

    UpPersonRecordData upPersonRecordData = new UpPersonRecordData();

    Operation operation;

    Bitmap headphoto;

    Bitmap photo;

    String persontype;

    Disposable checkChange;

    SPUtils config = SPUtils.getInstance("config");

    BaseConfig ins_type = AppInit.getInstrumentConfig();

    AddPersonWindow personWindow;

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    ICardInfo cardInfo;

    PersonBean person1 = new PersonBean();

    PersonBean person2 = new PersonBean();

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

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

    Alert_Message alert_message = new Alert_Message(this);

    Alert_Server alert_server = new Alert_Server(this);

    Alert_IP alert_ip = new Alert_IP(this);

    @OnClick(R.id.iv_network)
    void show() {
        personWindow = new AddPersonWindow(this);
        personWindow.setOptionTypeListener(this);
        personWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    @OnClick(R.id.iv_lock)
    public void showMessage() {
        if (AppInit.getInstrumentConfig().getClass().getName().equals(SHDMJ_config.class.getName())
                && Lock.getInstance().getLockState().getClass().getName().equals(State_Unlock.class.getName())) {
            SwitchPresenter.getInstance().doorOpen();
        } else {
            alert_message.showMessage();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Lg.e("生命周期", "onCreate");
        BarUtils.hideStatusBar(this);
        idp.idCardOpen();
        pp.initCamera();
        alert_message.messageInit();
        alert_ip.IpviewInit();
        alert_server.serverInit(new Alert_Server.Server_Callback() {
            @Override
            public void setNetworkBmp() {
                iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
            }
        });
        if (!AppInit.getInstrumentConfig().getClass().getName().equals(HeBeiDanNing_Config.class.getName())) {
            AppInit.getMyManager().ethEnabled(true);
        }
        operation = new Operation(new LockingState());
        if (ins_type.noise()) {
            MediaHelper.mediaOpen();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Lg.e("生命周期", "onStart");
        pp.setParameter(surfaceView.getHolder());
    }


    @Override
    public void onRestart() {
        super.onRestart();
        Lg.e("生命周期", "onRestart");
        if (!AppInit.getInstrumentConfig().getClass().getName().equals(HeBeiDanNing_Config.class.getName())) {
            pp.initCamera();
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        Lg.e("生命周期", "onResume");
        idp.IDCardPresenterSetView(this);
        idp.readCard();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
        //operation.setState(new LockingState());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Lg.e("生命周期", "onStop");

    }

    @Override
    public void onPause() {
        super.onPause();
        Lg.e("生命周期", "onPause");
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
        pp.PhotoPresenterSetView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Lg.e("生命周期", "onDestroy");
        idp.idCardClose();
        if (!AppInit.getInstrumentConfig().getClass().getName().equals(HeBeiDanNing_Config.class.getName())) {
            AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        }
        if (ins_type.noise()) {
            MediaHelper.mediaRealese();
        }
    }

    @Override
    public void onSetText(String Msg) {
        if (alert_message.Showing()) {
            ToastUtils.showLong(Msg);
        }
    }

    void face_openDoorUpData() {
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
                            if (AppInit.getInstrumentConfig().doubleCheck()) {
                                if (response.startsWith("true") && (int) Double.parseDouble(response.substring(5, response.length())) < 85) {//这里要改
                                    connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid") + "&faceRecognition1=" + (person1.getFaceReconition() + 100) + "&faceRecognition2=" + (person2.getFaceReconition() + 100) + "&faceRecognition3=" + ((int) Double.parseDouble(response.substring(5, response.length())) + 100),
                                            config.getString("ServerId"),
                                            new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), photo).toByteArray(),
                                            new ServerConnectionUtil.Callback() {
                                                @Override
                                                public void onResponse(String response) {
                                                    if (response != null) {
                                                        tips.setText("开门记录已上传到服务器");
                                                    } else {
                                                        tips.setText("无法连接到服务器");
                                                        MediaHelper.play(MediaHelper.Text.err_connect_ns);
                                                    }
                                                }
                                            });
                                } else {
                                    tips.setText("上传失败，请注意是否单人双卡操作");
                                    MediaHelper.play(MediaHelper.Text.err_omtk);
                                }
                            } else {
                                connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid") + "&faceRecognition1=" + (person1.getFaceReconition() + 100) + "&faceRecognition2=" + (person2.getFaceReconition() + 100) + "&faceRecognition3=" + ((int) Double.parseDouble(response.substring(5, response.length())) + 100),
                                        config.getString("ServerId"),
                                        new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), photo).toByteArray(),
                                        new ServerConnectionUtil.Callback() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (response != null) {
                                                    tips.setText("开门记录已上传到服务器");
                                                } else {
                                                    tips.setText("无法连接到服务器");
                                                    MediaHelper.play(MediaHelper.Text.err_connect_ns);
                                                }
                                            }
                                        });
                            }

                        } else {
                            tips.setText("开门记录数据：无法连接到服务器");
                            MediaHelper.play(MediaHelper.Text.err_connect_ns);
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

    void noface_openDoorUpData() {
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), person2.getPhoto()).toByteArray(),
                new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            tips.setText("开门记录已上传到服务器");
                        } else {
                            tips.setText("开门记录上传失败，已保存到本地");
                            MediaHelper.play(MediaHelper.Text.second_err);
                            mdaoSession.insert(new ReUploadBean(null, "dataType=openDoor", new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), person2.getPhoto()).toByteArray(), 0));
                        }
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                    }
                });


    }

    Boolean getState(Class stateClass) {
        if (operation.getState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 70;

        image.compress(Bitmap.CompressFormat.JPEG, options, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        while (baos.toByteArray().length / 1024>100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
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

    boolean alarmPic = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        tips.setText("开门报警已被触发");
        MediaHelper.play(MediaHelper.Text.alarm);
        if (AppInit.getInstrumentConfig().getClass().getName().equals(HeBei_Config.class.getName())) {
            if (!alarmPic) {
                alarmPic = true;
                takepicture();
            }
        }
    }

    protected void takepicture() {
        if (!ins_type.isGetOneShot()) {
            pp.capture();
        } else {
            pp.getOneShut();
        }
    }
}
