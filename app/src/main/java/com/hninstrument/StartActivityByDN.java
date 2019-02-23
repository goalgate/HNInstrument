package com.hninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.Tools.AssetsUtils;
import com.hninstrument.Tools.NetInfo;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivityByDN extends Activity {

//    private String regEx = "^\\d{1,3}$";
//
//
//
//    Pattern pattern = Pattern.compile(regEx);

    private SPUtils config = SPUtils.getInstance("config");

    @BindView(R.id.module_input)
    EditText et_module;

    @OnClick(R.id.next)
    void next() {
        try {
            if (Integer.parseInt(et_module.getText().toString()) < 255) {
                int moduleNum = Integer.parseInt(et_module.getText().toString()) * 1000 + 1;
                config.put("firstStart", false);
                config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
                config.put("devid", new NetInfo().getMacId());
                config.put("moduleID", moduleNum);
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_CommonActivity");
                ToastUtils.showLong("网络模块ID设置成功");
                AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib","wltlib");
                this.finish();
            } else {
                ToastUtils.showLong("网络模块ID输入数值过大，请重试");
            }
        } catch (Exception e) {
            ToastUtils.showLong("输入内容无法成功转换为有效值，请重试");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_form);
        ButterKnife.bind(this);
    }

}
