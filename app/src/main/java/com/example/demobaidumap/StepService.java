package com.example.demobaidumap;

import android.provider.ContactsContract;
import android.util.Log;
import android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StepService {

    private int currentTime;

    /*
    * 用户授予权限后，获取第一天开始计数的时间
    * */
    public static String showTest(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        // 当前时间戳
        long currentTimeStamp = System.currentTimeMillis();
        Date curDate = new Date(currentTimeStamp);
        String currentDateFormat = formatter.format(curDate);

        // 当日零点时间戳
        Date currentDayZero = new Date(getZeroClockTimestamp(currentTimeStamp));
        // 当日零点时间
        String currentDayZeroFormat = formatter.format(currentDayZero);

        // 当前13位时间戳
        Log.e("TimeMillis",System.currentTimeMillis()+"<= TimeMillis:");
        Log.e("zone",currentDayZeroFormat+"+零点时间");
        Log.e("dataStr",currentDateFormat);

        return currentDayZeroFormat;
    }

    //获取当天0点的时间戳
    public static long getZeroClockTimestamp(long time){
        long zeroTimestamp = time - (time + TimeZone.getDefault().getRawOffset()) % (24 * 60 * 60 * 1000);
        return zeroTimestamp;
    }

}
