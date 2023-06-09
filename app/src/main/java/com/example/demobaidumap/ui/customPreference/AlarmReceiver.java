package com.example.demobaidumap.ui.customPreference;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.demobaidumap.R;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        Log.i("Alarm","闹钟调用");

        Context context1 = context.getApplicationContext();

        // 创建一个Intent，用于启动AlarmActivity
        Intent alarmIntent = new Intent(context1, AlarmShow.class);
        alarmIntent.putExtra("title", title);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context1.startActivity(alarmIntent);

        // 设置重复闹钟
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // calendar.add(Calendar.SECOND, 10);
        calendar.add(Calendar.MINUTE, 1); // 设置1分钟后触发闹钟
        Intent intent1 = new Intent(context1, AlarmReceiver.class);
        intent1.putExtra("title", title);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context1, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context1.getSystemService(ALARM_SERVICE);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
//            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道，必需
            CharSequence name = "My Notification Channel";
            String description = "My Notification Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = context1.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        // 弹出通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText("时间到了！")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }
}
