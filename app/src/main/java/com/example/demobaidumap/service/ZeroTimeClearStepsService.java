package com.example.demobaidumap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ZeroTimeClearStepsService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        if (currentHour == 23 && currentMinute >= 55) {
            // 考虑到时间可能有一定误差，这里可以额外判断23:55到23:59的时间范围
            // 在此处执行任务
        }
    }
}
