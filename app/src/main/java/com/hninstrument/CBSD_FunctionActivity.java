package com.hninstrument;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.Alerts.Alert_IP;
import com.hninstrument.Alerts.Alert_Message;
import com.hninstrument.Alerts.Alert_Server;
import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Bean.DataFlow.ReUploadBean;
import com.hninstrument.Bean.DataFlow.UpCheckRecordData;
import com.hninstrument.Bean.DataFlow.UpOpenDoorData;
import com.hninstrument.Bean.DataFlow.UpPersonRecordData;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.EventBus.AlarmEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.EventBus.TemHumEvent;
import com.hninstrument.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.hninstrument.Function.Func_Camera.mvp.view.IPhotoView;
import com.hninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.hninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import com.hninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.hninstrument.Receiver.TimeCheckReceiver;
import com.hninstrument.State.LockState.Lock;
import com.hninstrument.State.LockState.State_Lockup;
import com.hninstrument.State.OperationState.No_one_OperateState;
import com.hninstrument.State.OperationState.One_man_OperateState;
import com.hninstrument.State.OperationState.Operation;
import com.hninstrument.State.OperationState.Two_man_OperateState;
import com.hninstrument.Tools.MediaHelper;
import com.hninstrument.Tools.NetInfo;
import com.hninstrument.Tools.ServerConnectionUtil;
import com.hninstrument.greendao.DaoSession;
import com.hninstrument.greendao.ReUploadBeanDao;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cbdi.drv.card.ICardInfo;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public abstract class CBSD_FunctionActivity extends RxActivity implements IPhotoView, IIDCardView, AddPersonWindow.OptionTypeListener {
    public IDCardPresenter idp = IDCardPresenter.getInstance();

    public PhotoPresenter pp = PhotoPresenter.getInstance();

    public SurfaceView surfaceView;

    UpPersonRecordData upPersonRecordData = new UpPersonRecordData();

    Operation operation;

    boolean networkState;

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
    void showMessage() {
        alert_message.showMessage();
//        msg_daid.setText("设备ID：" + config.getString("devid"));
//        if (TextUtils.isEmpty(NetworkUtils.getIPAddress(true))) {
//            msg_ip.setText("IP地址：无法获取IP地址");
//        } else {
//            msg_ip.setText("IP地址：" + NetworkUtils.getIPAddress(true));
//        }
//        msg_mac.setText("MAC地址：" + new NetInfo().getMac());
//        msg_software.setText("软件版本号：" + AppUtils.getAppVersionName());
//        if ((DHCP.equals(AppInit.getMyManager().getEthMode()))) {
//            msg_ipmode.setText("当前以太网为动态IP获取模式");
//        } else if (STATICIP.equals(AppInit.getMyManager().getEthMode())) {
//            msg_ipmode.setText("当前以太网为静态IP获取模式");
//        } else {
//            msg_ipmode.setText("当前固件版本过低，无法获取以太网设置模式");
//        }
//        if (NetworkUtils.isConnected()) {
//            msg_network.setText("网口可正常通信");
//        } else {
//            msg_network.setText("连接网络失败，请检查网线连接状态");
//        }
//        msg_iccard.setText("请放置身份证进行判断");
//
//        if (Lock.getInstance().getLockState().getClass().getName().equals(State_Lockup.class.getName())) {
//            msg_lockState.setText("仓库处于上锁状态");
//        } else {
//            msg_lockState.setText("仓库处于解锁状态");
//        }
//        messageAlert.show();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        AppInit.getMyManager().ethEnabled(true);
        operation = new Operation(new No_one_OperateState());
        if (ins_type.noise()) {
            MediaHelper.mediaOpen();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        pp.setParameter(surfaceView.getHolder());
    }


    @Override
    public void onRestart() {
        super.onRestart();
        pp.initCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        idp.IDCardPresenterSetView(this);
        idp.readCard();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());

        networkState = false;
        operation.setState(new No_one_OperateState());
    }

    @Override
    public void onPause() {
        super.onPause();
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
        pp.PhotoPresenterSetView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        idp.idCardClose();
        AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        if (ins_type.noise()) {
            MediaHelper.mediaRealese();
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
                            if (response.startsWith("true") && (int) Double.parseDouble(response.substring(5, response.length())) < 60) {//这里要改
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
                            tips.setText("开门记录服务器上传失败");
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

    Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
            if (!networkState) {
                Log.e("信息提示", "重新联网了");
                networkState = true;
                final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
                List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
//                if (list.size() > 20) {
//                    idp.stopReadCard();
//                    MediaHelper.play(MediaHelper.Text.wait_reupload);
//                }
                for (final ReUploadBean bean : list) {
                    if (bean.getContent() != null) {
                        if (bean.getType_patrol() != 0) {
                            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("devid") + "&checkType=" + bean.getType_patrol(),
                                    config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (response != null) {
                                                if (response.startsWith("true")) {
                                                    Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                                    reUploadBeanDao.delete(bean);
                                                }
                                            }
                                        }
                                    });
                        } else {
                            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("devid"),
                                    config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (response != null) {
                                                if (response.startsWith("true")) {
                                                    Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                                    reUploadBeanDao.delete(bean);
                                                }
                                            }
                                        }
                                    });
                        }
                    } else {
                        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("devid"),
                                config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (response != null) {
                                            if (response.startsWith("true")) {
                                                Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                                reUploadBeanDao.delete(bean);
                                            }
                                        }
                                    }
                                });
                    }

                }
//                idp.readCard();
                MediaHelper.play(MediaHelper.Text.waiting);
            }
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.non_wifi));
            networkState = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        tips.setText("开门报警已被触发");
        MediaHelper.play(MediaHelper.Text.alarm);
    }

    protected void takepicture(){
        if (!ins_type.isGetOneShot()) {
            pp.capture();
        } else {
            pp.getOneShut();
        }
    }
}
