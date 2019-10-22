package com.hninstrument;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.Config.HeBei_Config;
import com.hninstrument.Config.HuBeiWeiHua_Config;
import com.hninstrument.Config.SHDMJ_config;
import com.hninstrument.Config.SHGJ_Config;
import com.hninstrument.Config.SH_Config;
import com.hninstrument.HeibeiDNHelper.MyAccessibilityService;
import com.hninstrument.Tools.AssetsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cbdi.log.Lg;

/**
 * Created by zbsz on 2017/12/8.
 */

public class StartActivity extends Activity {

    private String regEx = "^\\d{4}$";

    private SPUtils config = SPUtils.getInstance("config");

    Pattern pattern = Pattern.compile(regEx);

    private static final String TAG = StartActivity.class.getSimpleName() + ">>>>>";


    @BindView(R.id.dev_prefix)
    TextView dev_prefix;

    @BindView(R.id.devid_input)
    EditText dev_suffix;

    @BindView(R.id.btn_chooseCam)
    Button btn_chooseCam;

    @OnClick(R.id.btn_chooseCam) void chooseCam(){
        if(config.getBoolean("chooseCam",true)){
            config.put("chooseCam",false);
        }
    }

    @OnClick(R.id.next)
    void next() {
        if(AppInit.getMyManager().getAndroidDisplay().startsWith("rk3288")){
            if(!isAccessibilitySettingsOn(this)){
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, 1);
                return;
            }
        }
        if (pattern.matcher(dev_suffix.getText().toString()).matches()) {
            config.put("firstStart", false);
            config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
            config.put("devid", AppInit.getInstrumentConfig().getDev_prefix() + dev_suffix.getText().toString());
            //ActivityUtils.startActivity(getPackageName(),getPackageName()+".MainActivity");
            if(AppInit.getInstrumentConfig().getClass().getName().equals(HuBeiWeiHua_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HuBeiWeiHuaActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SH_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_ShangHaiActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SHDMJ_config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_ShangHaiActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SHGJ_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_SHGJActivity");
            }else if(AppInit.getInstrumentConfig().getClass().getName().equals(HeBei_Config.class.getName())){
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HeBeiActivity");
            } else{
                //ActivityUtils.startActivity(getPackageName(), getPackageName() + ".MainActivity");
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_CommonActivity");
            }
            StartActivity.this.finish();
            ToastUtils.showLong("设备ID设置成功");
            //copyFilesToSdCard();
            AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib","wltlib");
        } else {
            ToastUtils.showLong("设备ID输入错误，请重试");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_form);
        ButterKnife.bind(this);
        dev_prefix.setText(AppInit.getInstrumentConfig().getDev_prefix());
        if(AppInit.getMyManager().getAndroidDisplay().startsWith("rk3288")){
            btn_chooseCam.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        // MyAccessibilityService为对应的服务
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        Log.e(TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.e(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.e(TAG, "***ACCESSIBILITY IS ENABLED***");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.e(TAG, "accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.e(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.e(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

   /* String SDCardPath = Environment.getExternalStorageDirectory() +"/";
    private void copyFilesToSdCard() {
        copyFileOrDir(""); // copy all files in assets folder in my project
    }

    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            Lg.i("tag", "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = SDCardPath+ path;
                Lg.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Lg.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir( p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Lg.e("tag", "I/O Exception");
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Lg.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName =SDCardPath+filename.substring(0, filename.length()-4);
            else
                newFileName =SDCardPath+filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Lg.e("tag", "Exception in copyFile() of "+newFileName);
            Lg.e("tag", "Exception in copyFile() "+e.toString());
        }

    }*/
}
