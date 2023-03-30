package com.example.demobaidumap.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import com.example.demobaidumap.R;
import com.example.demobaidumap.databinding.FragmentHomeBinding;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity().getApplicationContext();

        nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 读取SharedPreferences中的数据，判断是否需要清零步数
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedDate = prefs.getString(KEY_STEP_COUNT, "");
        String[] all = savedDate.split("[@]");

        // 当前日期和存储的日期不一致，需要清零步数
        if (!all[0].equals(nowadays)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_STEP_COUNT, nowadays+"@"+0);
            editor.apply();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        stepText = view.findViewById(R.id.textView2);

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerStepCounter();
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
//                        String savedStepCount = prefs.getString(KEY_STEP_COUNT, "");
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_STEP_COUNT,  nowadays+"@"+stepCount);
                        editor.apply();
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