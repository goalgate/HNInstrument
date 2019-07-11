package com.hninstrument;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hninstrument.HeibeiDNHelper.MyAccessibilityService;
import com.hninstrument.Tools.AssetsUtils;
import com.hninstrument.Tools.NetInfo;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivityByDN extends Activity {

    private static final String TAG = StartActivityByDN.class.getSimpleName() + ">>>>>";

    private SPUtils config = SPUtils.getInstance("config");

    @BindView(R.id.module_input)
    EditText et_module;

    @OnClick(R.id.next)
    void next() {
        if(!isAccessibilitySettingsOn(this)){
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 1);
        }else {
            try {
                if (Integer.parseInt(et_module.getText().toString()) < 255) {
                    int moduleNum = Integer.parseInt(et_module.getText().toString()) * 1000 + 1;
                    config.put("firstStart", false);
                    config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
//                    config.put("devid", new NetInfo().getMacId());
                    config.put("devid", "140020-125208-228190");
                    config.put("moduleID", moduleNum);
                    ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HeibeiDNActivity");
                    ToastUtils.showLong("网络模块ID设置成功");
                    AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib", "wltlib");
                    this.finish();
                } else {
                    ToastUtils.showLong("网络模块ID输入数值过大，请重试");
                }
            } catch (Exception e) {
                ToastUtils.showLong("输入内容无法成功转换为有效值，请重试");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_form);
        ButterKnife.bind(this);
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
}
