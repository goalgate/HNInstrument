package com.hninstrument.Alerts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.AppInit;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.R;
import com.hninstrument.Tools.DAInfo;
import com.hninstrument.Tools.ServerConnectionUtil;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Alert_Server {


    private Context context;

    private SPUtils config = SPUtils.getInstance("config");

    BaseConfig ins_type = AppInit.getInstrumentConfig();

    String url;
    private AlertView inputServerView;
    private EditText etName;
    private ImageView QRview;

    public Alert_Server(Context context) {
        this.context = context;
    }

    public void serverInit(final Server_Callback callback) {
        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputserver_form, null);
        etName = (EditText) extView1.findViewById(R.id.server_input);
        QRview = (ImageView) extView1.findViewById(R.id.QRimage);
        inputServerView = new AlertView("服务器设置", null, "取消", new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (!etName.getText().toString().replaceAll(" ", "").endsWith("/")) {
                        url = etName.getText().toString() + "/";
                    } else {
                        url = etName.getText().toString();
                    }
                    new ServerConnectionUtil().post(url + AppInit.getInstrumentConfig().getUpDataPrefix() + "daid=" + config.getString("devid") + "&dataType=test", url
                            , new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            config.put("ServerId", url);
                                            ToastUtils.showLong("连接服务器成功");
                                            callback.setNetworkBmp();
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



    public void show() {
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
    }

    public interface Server_Callback {
        void setNetworkBmp();
    }

    ;
}
