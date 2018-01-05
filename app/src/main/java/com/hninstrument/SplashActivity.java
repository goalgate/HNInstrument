package com.hninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.R;

/**
 * Created by zbsz on 2017/12/8.
 */

public class SplashActivity extends Activity {

    private static final String PREFS_NAME = "config";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SPUtils SP_Config = SPUtils.getInstance(PREFS_NAME);

        if (SP_Config.getBoolean("firstStart", true)) {
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivity");
            this.finish();
        }else {
            if(SP_Config.getString("devid").substring(6,7).equals(String.valueOf(1))){
                SP_Config.put("devid",SP_Config.getString("devid").substring(0,6)+"3"+SP_Config.getString("devid").substring(7,10));
                ToastUtils.showLong("设备号已成功转换");
            }
            ActivityUtils.startActivity(getPackageName(), getPackageName() + ".MainActivity");
            this.finish();
        }
    }
}
