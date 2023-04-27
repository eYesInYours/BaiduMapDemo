package com.example.demobaidumap.fall;

import static android.content.Context.SENSOR_SERVICE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.demobaidumap.MyNavigation;
import com.example.demobaidumap.R;
import com.example.demobaidumap.ui.home.HomeFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LiuWeixiang on 2017/3/13.
 */

public class FallSensorManager extends Service {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Context context;
    private float accX, accY, accZ;
    private float svm;
    public Fall fall;
    private final String TAG = "liuweixiang";

    double accelerometerValue;

    public FallSensorManager(Context context){
        this.context = context;
        fall = new Fall();
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        context = getApplicationContext();
//    }

    /*
            加载传感器
             */
    public void initSensor(){
        //获取SensorManager，系统的传感器管理服务
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        //获取accelerometer加速度传感器
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d(TAG, "FallSensorManager.initSensor()");
    }

    /*
    注册传感器
     */
    public void registerSensor(){
        sensorManager.registerListener(sensorEventListener,
                accelerometer, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "FallSensorManager.registerSensor()");
    }
    /*
    取消注册传感器
     */
    public void unregisterSensor(){
        sensorManager.unregisterListener(sensorEventListener);
        Log.d(TAG, "FallSensorManager.unregisterSensor");
    }

    public SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];
                    svm = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);

                    SharedPreferences pref = context.getSharedPreferences("SVM",MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("svm",""+svm);
                    editor.apply();

                    // 采集
                    Fall.svmCollector(svm);
                    // 滤波
                    Fall.setSvmFilteringData();
                    break;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };






    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
