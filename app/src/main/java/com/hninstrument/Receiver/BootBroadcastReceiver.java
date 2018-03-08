package com.hninstrument.Receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.SPUtils;
import com.hninstrument.AppInit;
import com.hninstrument.Config.BaseConfig;
import com.hninstrument.MainActivity;
import com.hninstrument.Service.SwitchService;
import com.hninstrument.SplashActivity;
import com.hninstrument.Tools.ServerConnectionUtil;

import java.util.Calendar;

import cbdi.log.Lg;

import static android.content.Context.ALARM_SERVICE;


/**
 * Created by zbsz on 2017/7/28.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {// boot;
            Intent intent2 = new Intent(context, SplashActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
    }
}
