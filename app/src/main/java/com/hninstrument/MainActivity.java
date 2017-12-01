package com.hninstrument;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.hninstrument.Bean.DataFlow.UpPersonRecordData;
import com.hninstrument.EventBus.NetworkEvent;
import com.hninstrument.EventBus.PassEvent;
import com.hninstrument.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.hninstrument.Function.Func_Camera.mvp.view.IPhotoView;
import com.hninstrument.Retrofit.Request.RequestModule.CompareRequestBean;
import com.hninstrument.Retrofit.Response.ResponseModule.CompareResponseBean;
import com.hninstrument.Retrofit.RetrofitGenerator;
import com.hninstrument.Service.SwitchService;
import com.hninstrument.State.OperationState.No_one_OperateState;
import com.hninstrument.State.OperationState.One_man_OperateState;
import com.hninstrument.State.OperationState.Operation;
import com.hninstrument.State.OperationState.Two_man_OperateState;
import com.hninstrument.Tools.FileUtils;
import com.hninstrument.Tools.SafeCheck;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cbdi.drv.card.CardInfo;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends FunctionActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    UpPersonRecordData upPersonRecordData = new UpPersonRecordData();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


    Disposable disposableTips;

    CardInfoRk123x cardInfo;

    Disposable disposableTime;

    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.img_captured1)
    ImageView captured1;

    @BindView(R.id.tv_info)
    TextView tips;

    Operation operation;
    Intent intent;

    ProgressDialog pd;

    Bitmap headphoto;

    Bitmap photo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        openService();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        disposableTime = Observable.interval(0, 1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
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
        disposableTime.dispose();
        disposableTips.dispose();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {

    }


    @Override
    public void onCaremaText(String s) {

    }


    @Override
    public void onGetPhoto(Bitmap bmp) {
        photo = bitmapChange(bmp, 0.5f, 0.5f);
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        new Thread()
        {
            @Override
            public void run()
            {
                sendPost();
            }
        }.start();
/*        Map<String,Object> map = new HashMap<String,Object>();
        map.put("dataType","persionRecord");
        map.put("daid","1234567890");
        map.put("pass",new SafeCheck().getPass("1234567890"));
        RequestBody requestFile= RequestBody.create(MediaType.parse("multipart/form-data"),upPersonRecordData.toPersonRecordData(cardInfo.cardId(),bmp,cardInfo.name()).toByteArray());
        MultipartBody.Part body = MultipartBody.Part.create(requestFile);
        RetrofitGenerator.getpersonRecordApi().personRecord(map,body)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ByteArrayOutputStream>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ByteArrayOutputStream byteArrayOutputStream) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

                try {
                    outputStream.flush();
                    outputStream.close();

                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        });*/
        operation.doNext()  ;
        if ((getState(One_man_OperateState.class))) {
            bmp = bitmapChange(bmp, 1f, 1.5f);
            captured1.setImageBitmap(bmp);
            tips.setText(cardInfo.name() + "刷卡成功");
        } else if ((getState(Two_man_OperateState.class))) {
            captured1.setImageBitmap(null);
            EventBus.getDefault().post(new PassEvent());
            tips.setText(cardInfo.name() + "刷卡成功，门禁已解锁");
            operation.doNext();
        }
        pp.setDisplay(surfaceView.getHolder());
        //Verify(new CompareRequestBean(FileUtils.bitmapToBase64(headphoto), FileUtils.bitmapToBase64(bmp)));
    }


    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }


    String Last_CardID;

    @Override
    public void onsetCardInfo(CardInfoRk123x cardInfo) {
        if (getState(No_one_OperateState.class)) {
            Last_CardID = cardInfo.cardId();
            Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Long aLong) {
                            operation.setState(new No_one_OperateState());
                            captured1.setImageBitmap(null);
                            Last_CardID = null;
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
            if (Last_CardID.equals(cardInfo.cardId())) {
                tips.setText("请不要连续输入同一个管理员的信息");
                return;
            }
        }
        pp.capture();
        this.cardInfo = cardInfo;
        tips.setText(cardInfo.name() + "刷卡中");
    }


    private Boolean getState(Class stateClass) {
        if (operation.getState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }

    private void Verify(final CompareRequestBean requestBean) {

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
    }

    private Bitmap bitmapChange(Bitmap bmp, float width, float height) {
        Matrix matrix = new Matrix();
        matrix.postScale(width, height);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }
    public String sendPost()
    {

        int TIME_OUT = 10*1000;   //超时时间
        String CHARSET = "utf-8";
        BufferedReader in = null;
        String result = null;
        String  BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";
        String RequestURL =  "http://192.168.12.165:7001/daServer/da_gzmb_updata?dataType=persionRecord&daid=1234567890&pass="+new SafeCheck().getPass("1234567890");
        try
        {
            int bufferSize = 2048;
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
            ByteArrayInputStream bin = new ByteArrayInputStream(upPersonRecordData.toPersonRecordData(cardInfo.cardId(),photo,cardInfo.name()).toByteArray());
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            while((length = bin.read(buffer)) != -1)
            {
                ds.write(buffer, 0, length);
            }

            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
            {
                result =  line;
            }
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
        finally
        {

        }
        return result;
    }

}
