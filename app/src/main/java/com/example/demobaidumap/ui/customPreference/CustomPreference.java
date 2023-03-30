package com.example.demobaidumap.ui.customPreference;

import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.example.demobaidumap.MainActivity;
import com.example.demobaidumap.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomPreference extends Preference {

    private Context mContext;

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        Activity activity = (Activity) getContext();


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()){
                    Log.e("permission","ok");
                    // 弹出表单
                    MyDialogFragment dialogFragment = new MyDialogFragment();
                    dialogFragment.setOnCompleteListener(new MyDialogFragment.OnCompleteListener() {
                        @Override
                        public void onComplete(String title, int hour, int minute) {
                            // 创建Intent，用于在闹钟时间到达时启动BroadcastReceiver
                            Log.e("receiver","doing");
                            Intent intent = new Intent(getContext(), AlarmReceiver.class);
                            Log.e("receiver","done");
                            intent.putExtra("title", title);

                            // 创建PendingIntent
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                    getContext(),
                                    0,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE);

                            // 获取AlarmManager实例
                            AlarmManager alarmManager = (AlarmManager) getContext().getApplicationContext().getSystemService(ALARM_SERVICE);

                            // 获取当前时间的Calendar实例
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);

                            // 设置闹钟
                            alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.getTimeInMillis(),
                                    pendingIntent);
                            Log.e("alarm","set alarm");
                            Toast.makeText(getContext(), "已设置闹钟", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialogFragment.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "MyDialogFragment");
                }else{
                    // 动态申请权限
//                    if(shouldRequestPermission()){
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.SET_ALARM},
//                                2);
//                    }


//                    // 无权限，弹出提示框
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("无法使用该功能");
                    builder.setMessage("请在应用设置中授予闹钟权限以使用该功能");
                    builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                            intent.setData(uri);
                            getContext().startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }


            }
        });
    }


    private boolean shouldRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Android 6.0以上才需要申请权限
            return false;
        }
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.SET_ALARM)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.SET_ALARM) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

}
