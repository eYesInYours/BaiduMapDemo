package com.example.demobaidumap.ui.home;

import static android.content.Context.MODE_PRIVATE;

import static com.example.demobaidumap.util.StepUtil.isSupportStep;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mshield.x6.recv.MyReceiver;
import com.example.demobaidumap.MainActivity;
import com.example.demobaidumap.NotificationUtils;
import com.example.demobaidumap.R;
import com.example.demobaidumap.databinding.FragmentHomeBinding;
import com.example.demobaidumap.service.ZeroTimeClearStepsService;
import com.example.demobaidumap.ui.gallery.GalleryFragment;
import com.example.demobaidumap.util.StepService;
import com.example.demobaidumap.util.StepUtil;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private FragmentHomeBinding binding;
    SensorManager mSensorManager;
    public Sensor stepCounter;//步伐总数传感器
    public SensorEventListener stepCounterListener;//步伐总数传感器事件监听器

        TextView stepText;

//    private int mStep;
//    private int mStepCount;
//    private float StepCounter_Open=0;
//    private float StepCounter_Close=0;
//    private Boolean stepFlag = true;

    public static final String PREFS_NAME = "StepCounterPrefs";
    public static final String KEY_DATE = "date";
    public static final String KEY_STEP_COUNT = "stepCount";
    public static final String LEFT_COUNT = "left_count";

    public String nowadays;
    String yesterdayString;

    private LocationClient mLocationClient = null;
    private Notification mNotification;
    private boolean isEnableLocInForeground = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity().getApplicationContext();

        nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();
        yesterdayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday);


//        // 当前日期和存储的日期不一致，需要清零步数
//        if (!all[0].equals(nowadays)) {
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(KEY_STEP_COUNT, nowadays+"@"+0);
//            editor.apply();
//        }


    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        stepText = view.findViewById(R.id.textView2);

        Log.e("allow step--",""+isSupportStep(getActivity()));

        stepText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String steps = StepUtil.getTodayStep(getActivity()) + "步";
                Log.e("steps",""+steps  );
//                stepText.setText(steps);
            }
        });

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        /*
         * 获取设备支持的传感器
         * */
        List<Sensor> sensorsList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : sensorsList){
            Log.d("支持的传感器，",sensor.getName().toString());
        }

        // 获取计步传感器服务：返回从开机到目前为止的步数。虚拟机没有counter传感器，真机运行
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        registerStepCounter();

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void registerZeroClearAlarm(){
        // 获取AlarmManager实例
        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        // 创建Intent，指定执行操作的组件
        Intent intent = new Intent(getActivity(), ZeroTimeClearStepsService.class);

        // 设置Action
        intent.setAction("com.example.myapp.ACTION_MY_TASK");

        // 创建PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 首先取消闹钟，避免重复设置多个
        alarmMgr.cancel(pendingIntent);

        // 设置执行时间为零点
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 设置每天执行一次
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    // 计步传感器方法
    public void registerStepCounter(){
        stepCounterListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
                    Context context = getActivity().getApplicationContext();

                    // 赋值步数（COUNTER获取的是开机的总步数），并将步数保存到SharedPreferences中
                    int stepCount = (int)sensorEvent.values[0];
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    String savedStepCount = prefs.getString(KEY_STEP_COUNT+nowadays, "");
                    String[] all = savedStepCount.split("[@]");

                    Log.e("init all",all[0]+"");

                    Log.e("nowadays",""+nowadays);
                    Log.e("yesterdayString",""+yesterdayString);

                    // 当前日期和存储的日期不一致，需要清零步数
                    if (!all[0].equals(nowadays)) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_STEP_COUNT + nowadays, nowadays+"@"+0);
                        editor.apply();
                    } else {
                        // 获取昨天的运动步数
                        SharedPreferences prefs2 = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        int todayStepCount;
                        if(prefs2.contains(KEY_STEP_COUNT + yesterdayString)){
                            String savedStepCount2 = prefs2.getString(KEY_STEP_COUNT+yesterdayString, "");
                            String[] all_yesterday = savedStepCount2.split("[@]");

                            Log.e("all_yesterday",""+all_yesterday);

                            int lastStepCount = Integer.parseInt(all_yesterday[1]);
                            todayStepCount = stepCount - lastStepCount;
                            stepText.setText("您今日已运动：" + todayStepCount + " 步");

                            Log.e("todayStepCount",""+todayStepCount);
                        }else{
                            // 没有，表示第一天使用
                            todayStepCount = stepCount;
                            stepText.setText("您今日已运动：" + todayStepCount + " 步");
                        }

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_STEP_COUNT + nowadays, nowadays+"@"+todayStepCount);
                        editor.apply();



                    }


//                    SharedPreferences prefs2 = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
////                        String savedStepCount = prefs.getString(KEY_STEP_COUNT, "");
//                    SharedPreferences.Editor editor = prefs2.edit();
//                    editor.putString(KEY_STEP_COUNT,  nowadays+"@"+stepCount);
//                    editor.apply();
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        mSensorManager.registerListener(stepCounterListener, stepCounter, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}