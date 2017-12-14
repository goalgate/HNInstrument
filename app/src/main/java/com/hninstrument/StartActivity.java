package com.hninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zbsz on 2017/12/8.
 */

public class StartActivity extends Activity {

    private String regEx = "^\\d{4}$";

    private SPUtils config = SPUtils.getInstance("config");

    Pattern pattern = Pattern.compile(regEx);


    @BindView(R.id.dev_prefix)
    TextView dev_prefix;
    @BindView(R.id.devid_input)
    EditText dev_suffix;

    @OnClick(R.id.next)
    void next() {
        if (pattern.matcher(dev_suffix.getText().toString()).matches()) {
            config.put("firstStart", false);
            config.put("ServerId","http://192.168.12.168:7001/");
            config.put("devid", dev_prefix.getText().toString() + dev_suffix.getText().toString());
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".MainActivity");
            StartActivity.this.finish();
            ToastUtils.showLong("设备ID设置成功");
        } else {
            ToastUtils.showLong("设备ID输入错误，请重试");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_form);
        ButterKnife.bind(this);
    }
}
