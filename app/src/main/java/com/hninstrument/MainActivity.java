package com.hninstrument;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Bean.DataFlow.UpOpenDoorData;
import com.hninstrument.Bean.DataFlow.UpPersonRecordData;
import com.hninstrument.EventBus.CloseDoorEvent;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.hninstrument.Function.Func_Camera.mvp.view.IPhotoView;
import com.hninstrument.Retrofit.Request.RequestModule.CompareRequestBean;
import com.hninstrument.Retrofit.Response.ResponseModule.CompareResponseBean;
import com.hninstrument.Retrofit.RetrofitGenerator;
import com.hninstrument.Retrofit.ServerConnectionUtil;
import com.hninstrument.Service.SwitchService;
import com.hninstrument.State.OperationState.No_one_OperateState;
import com.hninstrument.State.OperationState.One_man_OperateState;
import com.hninstrument.State.OperationState.Operation;
import com.hninstrument.State.OperationState.Two_man_OperateState;
import com.hninstrument.Tools.FileUtils;
import com.hninstrument.Tools.SafeCheck;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends FunctionActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    UpPersonRecordData upPersonRecordData = new UpPersonRecordData();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Disposable disposableTips;

    CardInfoRk123x cardInfo ;

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

    Operation operation;

    Intent intent;

    Bitmap headphoto;

    Bitmap photo;

    String Msg_network;
    private static String personRecordUri = "http://192.168.12.165:7001/daServer/da_gzmb_updata?dataType=persionRecord&daid=1234567890&pass=" + new SafeCheck().getPass("1234567890");

    private static String faceRecognitionUri = "http://192.168.12.165:7001/daServer/da_gzmb_updata?dataType=faceRecognition&daid=1234567890&pass=" + new SafeCheck().getPass("1234567890");

    private static String samePsonFaceRecognitionUri = "http://192.168.12.165:7001/daServer/da_gzmb_updata?dataType=samePsonFaceRecognition&daid=1234567890&pass=" + new SafeCheck().getPass("1234567890");
    private static String OpenDoorUri = "http://192.168.12.165:7001/daServer/da_gzmb_updata?dataType=openDoor&daid=1234567890&pass=" + new SafeCheck().getPass("1234567890");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
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
                .debounce(60, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws Exception {
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
        stopService(intent);

        disposableTips.dispose();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if(event.getNetwork_state()){
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.wifi));
        }else{
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.non_wifi));
        }
        Msg_network = event.getMsg();
    }


    @Override
    public void onCaremaText(String s) {

    }


    @Override
    public void onGetPhoto(Bitmap bmp) {
        photo = bitmapChange(bmp, 0.5f, 0.5f);
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        if(getState(No_one_OperateState.class)||getState(One_man_OperateState.class)){
            upData();
        }else if(getState(Two_man_OperateState.class)){
            operation.doNext();
            pp.setDisplay(surfaceView.getHolder());
            idp.readCard();

        }
    }


    private void upData() {
        ServerConnectionUtil.post(faceRecognitionUri,
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                operation.doNext();
                                if(getState(No_one_OperateState.class)){
                                    pp.setDisplay(surfaceView.getHolder());
                                    idp.readCard();
                                }
                                else if (getState(One_man_OperateState.class)) {
                                    person1.setPhoto(photo);
                                    person1.setFaceReconition(response.substring(5, response.length()));
                                    photo = bitmapChange(photo, 2f, 3f);
                                    captured1.setImageBitmap(photo);
                                    tips.setText(cardInfo.name() + "刷卡成功");
                                    pp.setDisplay(surfaceView.getHolder());
                                    idp.readCard();

                                } else if ((getState(Two_man_OperateState.class))) {
                                    person2.setFaceReconition(response.substring(5, response.length()));
                                    openDoorUpData();
                                    captured1.setImageBitmap(null);
                                    EventBus.getDefault().post(new PassEvent());
                                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_lock_unlock));
                                    tips.setText(cardInfo.name() + "刷卡成功，门禁已解锁");
                                }
                            } else {
                                tips.setText("数据上传失败");
                                pp.setDisplay(surfaceView.getHolder());
                                idp.readCard();
                            }
                        } else {
                            tips.setText("无法连接服务器");
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
                        }

                    }
                });
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    Disposable checkChange;
    private void openDoorUpData() {
        person1.getPhoto().compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        ServerConnectionUtil.post(samePsonFaceRecognitionUri,
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                ServerConnectionUtil.post(OpenDoorUri + "&faceRecognition1=" + person1.getFaceReconition() + "faceRecognition2" + person2.getFaceReconition() + "faceRecognition3" + response.substring(5, response.length()),
                                        new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), photo).toByteArray(),
                                        new ServerConnectionUtil.Callback() {
                                            @Override
                                            public void onResponse(String response) {
                                                pp.setDisplay(surfaceView.getHolder());
                                                idp.readCard();
                                            }
                                        });
                            } else {
                                tips.setText("数据上传失败");
                            }
                        } else {
                            tips.setText("无法连接到服务器");
                        }

                    }
                });
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }

    @Override
    public void onsetCardInfo(CardInfoRk123x cardInfo) {
        if (getState(No_one_OperateState.class)) {
            tips.setText(cardInfo.name() + "刷卡中");
            person1.setCardId(cardInfo.cardId());
            person1.setName(cardInfo.name());
            Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .compose(this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            checkChange = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            operation.setState(new No_one_OperateState());
                            captured1.setImageBitmap(null);
                            tips.setText("等待用户操作");
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else if (getState(One_man_OperateState.class)) {
          /*  this.cardInfo2 = cardInfo;
            tips.setText(cardInfo.name() + "刷卡中");
            if(checkChange!= null){
                checkChange.dispose();
            }*/
            person2.setCardId(cardInfo.cardId());
            person2.setName(cardInfo.name());
            if (person1 .getCardId().equals(person2.getCardId())) {
                tips.setText("请不要连续输入同一个管理员的信息");
                return;
            }else{
                tips.setText(cardInfo.name() + "刷卡中");
                if(checkChange!= null){
                    checkChange.dispose();
                }
            }
        } else if(getState(Two_man_OperateState.class)){
            EventBus.getDefault().post(new CloseDoorEvent());
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_lockup));
            tips.setText("已进入设防状态");
        }

        pp.capture();
        this.cardInfo = cardInfo;
        idp.stopReadCard();
    }


    private Boolean getState(Class stateClass) {
        if (operation.getState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }

    /*private void Verify(final CompareRequestBean requestBean) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(requestBean));
        RetrofitGenerator.getFacetofaceApi().facetoface(body).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CompareResponseBean>() {
            @Override
            public void onSubscribe(Disposable d) {
                pd = new ProgressDialog(MainActivity.this);
                pd.setTitle("提示");
                pd.setMessage("数据上传中，请稍后");
                pd.setCancelable(true);
                pd.show();
                idp.stopReadCard();
            }

            @Override
            public void onNext(CompareResponseBean responseBean) {
                pd.dismiss();
                if (responseBean.isResult() && responseBean.getSimilar() > 0.50) {
                    operation.doNext();
                    if ((getState(One_man_OperateState.class))) {
                        Matrix matrix = new Matrix();
                        matrix.postScale(1f, 1.5f);
                        photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
                        captured1.setImageBitmap(photo);
                        tips.setText(cardInfo.name() + "刷卡成功");
                    } else if ((getState(Two_man_OperateState.class))) {
                        captured1.setImageBitmap(null);
                        EventBus.getDefault().post(new PassEvent());
                        tips.setText(cardInfo.name() + "刷卡成功，门禁已解锁");
                        operation.doNext();
                    }
                } else {
                    tips.setText("人脸比对不符，请重试");
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showLong("与人脸比对服务器连接异常");

            }

            @Override
            public void onComplete() {
                pd.dismiss();
                pp.setDisplay(surfaceView.getHolder());
                idp.readCard();
            }
        });
    }*/

    private Bitmap bitmapChange(Bitmap bmp, float width, float height) {
        Matrix matrix = new Matrix();
        matrix.postScale(width, height);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }


}
