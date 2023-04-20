package com.example.demobaidumap.ui.customPreference;

import static android.content.Context.ALARM_SERVICE;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mshield.x6.recv.MyReceiver;
import com.example.demobaidumap.R;
import com.example.demobaidumap.ScrollingActivity;
import com.example.demobaidumap.alarmlist.AlarmData;
//import com.example.demobaidumap.alarmlist.MyAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyDialogFragment extends DialogFragment {

    // 定义回调接口
    public interface OnCompleteListener {
        void onComplete(String title, int hour, int minute);
    }

    private OnCompleteListener mListener;
    private RecyclerView recyclerView;

    // 设置回调接口
    public void setOnCompleteListener(OnCompleteListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 使用LayoutInflater加载自定义布局文件
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.custom_preference, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        EditText editTextTitle = view.findViewById(R.id.edit_text);
        TimePicker timePicker = view.findViewById(R.id.time_picker);

        builder.setView(view)
                .setTitle("添加日程")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do something
                        if (mListener != null) {
                            String title = editTextTitle.getText().toString();
                            int hour = timePicker.getCurrentHour();
                            int minute = timePicker.getCurrentMinute();
                            mListener.onComplete(title, hour, minute);

                            String timeStr = hour+":"+minute;
                            Log.e("alarm time",hour+"@"+minute);
                            // 添加闹钟列表
                            addAlarmTemplateView(title, timeStr);

                        }

                    }

//                    private Object getSystemService(String alarmService) {
//                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do something

                    }
                });

        return builder.create();
    }

    private void addAlarmTemplateView(String title, String time) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        NestedScrollView nestedScrollView = (NestedScrollView) inflater.inflate(R.layout.content_scrolling, null);

        View alarmTemplateView = getLayoutInflater().inflate(R.layout.alarm_template, null);

        LinearLayout linearLayout = nestedScrollView.findViewById(R.id.linear_layout_alarm_list);

        TextView titleView = alarmTemplateView.findViewById(R.id.alarm_title);
        TextView timeView = alarmTemplateView.findViewById(R.id.alarm_time);

        titleView.setText(title);
        timeView.setText(time);

//        alarmTemplateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(alarmTemplateView);

//        Toast.makeText(getContext(),"可前往列表查看已设置闹钟",Toast.LENGTH_SHORT).show();
        Log.e("Alarm List","Ok");
    }

}
