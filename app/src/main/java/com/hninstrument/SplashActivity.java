package com.hninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
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
            SP_Config.put("firstStart", false);
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivity");
            this.finish();
        }else{
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".MainActivity");
            this.finish();

        }
    }
}
