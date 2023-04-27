package com.example.demobaidumap.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.demobaidumap.R;

import java.util.List;

/**
 * 用于判断Service是否被杀死
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)//5.0以后可用
public class JobWakeUpService extends JobService {
    private int JobWakeUpId = 1;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMyOwnForeground();

        //开启轮寻
        JobInfo.Builder mJobBulider = new JobInfo.Builder(
                JobWakeUpId,new ComponentName(this,JobWakeUpService.class));
        //设置轮寻时间
        mJobBulider.setPeriodic(2000);
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(mJobBulider.build());
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //开启定时任务 定时轮寻 判断应用Service是否被杀死
        //如果被杀死则重启Service
        boolean messageServiceAlive = serviceAlive(BackgroundLocationService.class.getName());
        if(!messageServiceAlive){
            startService(new Intent(this,BackgroundLocationService.class));
        }

        return false;
    }

    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("Service", "channel_service", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("安全定位服务")
                    .setContentText("已为你开启摔倒检测与地理围栏功能")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setChannelId("Service");

            Notification notification = builder.build();
            startForeground(1, notification);

        }else{
            startForeground(1, new Notification());
        }

    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    private boolean serviceAlive(String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }


}
