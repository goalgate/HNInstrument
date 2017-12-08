package com.hninstrument.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hninstrument.Service.SwitchService;


/**
 * Created by zbsz on 2017/7/28.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {// boot;

        }


    }


}
