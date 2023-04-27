package com.example.demobaidumap.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.demobaidumap.service.BackgroundLocationService;

/**
 * 开机完成广播
 */
public class mReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent mIntent = new Intent(context, BackgroundLocationService.class);
        context.startService(mIntent);
    }
}
