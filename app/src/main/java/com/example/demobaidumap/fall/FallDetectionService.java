package com.example.demobaidumap.fall;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.demobaidumap.MyNavigation;
import com.example.demobaidumap.R;

import java.util.Calendar;

public class FallDetectionService extends Service {

    private FallSensorManager fallSensorManager;
    public Fall fall;
    private final int FELL = 0;
//    private final int TIME = 1;
    private boolean running = false;
//    private TextView countingView;
//    private Dialog dialog;
//    private Timer timer;
    private final String TAG = "liuweixiang";
    private DetectThread detectThread;
    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private FallLocalReceiver fallLocalReceiver;

    public FallDetectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("FALL", "FallDetectionService.onCreate()");


        fallSensorManager = new FallSensorManager(this);
        fallSensorManager.initSensor();
        fallSensorManager.registerSensor();
        fall = new Fall();
        fall.setThresholdValue(70,30);
        running = true;
        //在通知栏上显示服务运行
//        showInNotification();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.broadcast.FALL_LOCAL_BROADCAST");
        fallLocalReceiver = new FallLocalReceiver();
        localBroadcastManager.registerReceiver(fallLocalReceiver, intentFilter);

        startCountDownTimer();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FALL", "FallDetectionService.onStartCommand");
        detectThread = new DetectThread();
        detectThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
//        fallSensorManager.unregisterSensor();
//        localBroadcastManager.unregisterReceiver(fallLocalReceiver);
        super.onDestroy();
    }

    // 开始定时器
    private CountDownTimer mCountDownTimer;
    private final long COUNT_DOWN_INTERVAL = 3 * 60 * 60 * 1000;  // 定时器时间 3小时
//    private final long COUNT_DOWN_INTERVAL = 10 * 1000;  // 定时器时间 测试 10s
    private void startCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(COUNT_DOWN_INTERVAL, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                Boolean isNightTime = hour >= 22 || hour < 6; // 晚上10点到早上6点是夜间时段
                Log.e("isNightTime",""+isNightTime);
                if(isNightTime){
                    mCountDownTimer.cancel();
                    return;
                }

                // 在这里进行加速度传感器的数值检测
                // 如果数值超过阈值，则结束当前定时器，开启下一个定时器进行检测
                // 否则继续等待下一次onTick()方法调用
                // 获取加速度传感器的数值
                SharedPreferences pref = getSharedPreferences("SVM",MODE_PRIVATE);
                Float f = Float.parseFloat(pref.getString("svm",""));
                Log.e("countdowm",""+f);
                if (f > 10.0) {
                    mCountDownTimer.cancel();
                    startCountDownTimer(); // 开启下一个定时器进行检测
                }
            }

            @Override
            public void onFinish() {
                startMyOwnForeground();
                startCountDownTimer();
            }
        }.start();
    }


    //开一个线程用于检测跌倒
    class DetectThread extends Thread{
        @Override
        public void run() {
            fall.fallDetection();
            Log.d("FALL", "DetectThread.start()");
            while (running) {
                if (fall.isFell()) {
                    Log.e("FALL", "跌倒了");
                    running = false;
                    Message msg = handler.obtainMessage();
                    msg.what = FELL;
                    handler.sendMessage(msg);
                    fall.setFell(false);
                    fall.cleanData();
                    stopSelf();

                }
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case FELL:
                    Log.e("FALL", "FELL");
                    //报警
//                    showAlertDialog();
                    Intent intent = new Intent("com.broadcast.FALL_LOCAL_BROADCAST");
                    localBroadcastManager.sendBroadcast(intent);

                    break;

            }

        }
    };


    /*
    在通知栏上显示服务运行
     */
    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("suggest", "channel_suggest", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("您太久时间没有运动了")
                    .setContentText("出门走走吧")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setChannelId("suggest");

            Notification notification = builder.build();
            startForeground(22, notification);

        }else{
            startForeground(22, new Notification());
        }

    }

}
