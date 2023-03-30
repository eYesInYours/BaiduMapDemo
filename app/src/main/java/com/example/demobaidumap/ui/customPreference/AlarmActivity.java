package com.example.demobaidumap.ui.customPreference;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class AlarmActivity extends Activity {

//    public void handlePermissionResult(int requestCode,int[] grantResults) {
//        switch (requestCode) {
//            case 2:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission has been granted
//                    // Open the dialog
//                    // 弹出表单
//                    Log.e("request","动态申请闹钟权限成功");
//                    MyDialogFragment dialogFragment = new MyDialogFragment();
//                    dialogFragment.setOnCompleteListener(new MyDialogFragment.OnCompleteListener() {
//                        @Override
//                        public void onComplete(String title, int hour, int minute) {
//                            // 创建Intent，用于在闹钟时间到达时启动BroadcastReceiver
//                            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
//                            intent.putExtra("title", title);
//
//                            // 创建PendingIntent
//                            PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                                    getApplicationContext(),
//                                    0,
//                                    intent,
//                                    PendingIntent.FLAG_IMMUTABLE);
//
//                            // 获取AlarmManager实例
//                            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getApplicationContext().getSystemService(ALARM_SERVICE);
//
//                            // 获取当前时间的Calendar实例
//                            Calendar calendar = Calendar.getInstance();
//                            calendar.set(Calendar.HOUR_OF_DAY, hour);
//                            calendar.set(Calendar.MINUTE, minute);
//                            calendar.set(Calendar.SECOND, 0);
//
//                            // 设置闹钟
//                            alarmManager.setExact(
//                                    AlarmManager.RTC_WAKEUP,
//                                    calendar.getTimeInMillis(),
//                                    pendingIntent);
//                            Log.e("alarm","set alarm");
//                            Toast.makeText(getApplicationContext(), "已设置闹钟", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    dialogFragment.show(((AppCompatActivity)getApplicationContext()).getSupportFragmentManager(), "MyDialogFragment");
//
//                } else {
//                    // Permission has been denied
//                    // Show a message or something
//                    Toast.makeText(this, "You need to grant permission to use this feature",
//                            Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//    }
}
