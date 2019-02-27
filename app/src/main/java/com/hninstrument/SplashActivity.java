package com.hninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.Config.HeBeiDanNing_Config;
import com.hninstrument.Config.HeBei_Config;
import com.hninstrument.Config.HuBeiWeiHua_Config;
import com.hninstrument.Config.SHDMJ_config;
import com.hninstrument.Config.SHGJ_Config;
import com.hninstrument.Config.SH_Config;
import com.hninstrument.R;
import com.hninstrument.Tools.NetInfo;

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
            if(AppInit.getInstrumentConfig().getClass().getName().equals(HeBeiDanNing_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivityByDN");
                this.finish();
            }else{
                ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivity");
                this.finish();
            }

        }else {
            if(SP_Config.getString("devid").substring(6,7).equals(String.valueOf(1))){
                SP_Config.put("devid",SP_Config.getString("devid").substring(0,6)+"3"+SP_Config.getString("devid").substring(7,10));
                ToastUtils.showLong("设备号已成功转换");
            }
            if("http://115.159.241.118:8009/".equals(SP_Config.getString("ServerId"))){
                SP_Config.put("ServerId","https://gdmb.wxhxp.cn:8009/");
            }
            if(AppInit.getInstrumentConfig().getClass().getName().equals(HuBeiWeiHua_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HuBeiWeiHuaActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SH_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_ShangHaiActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SHDMJ_config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_ShangHaiActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SHGJ_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_SHGJActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(HeBeiDanNing_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HeibeiDNActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(HeBei_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HeBeiActivity");
            }  else{
                //ActivityUtils.startActivity(getPackageName(), getPackageName() + ".MainActivity");
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_CommonActivity");
            }
            this.finish();
        }
    }
}
