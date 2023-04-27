package com.example.demobaidumap.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.demobaidumap.R;

/**
 * 守护进程 双进程通讯
 */
public class GuardService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMyOwnForeground();
        //绑定建立链接
        bindService(new Intent(this,BackgroundLocationService.class),
                mServiceConnection, this.BIND_IMPORTANT);
        return START_STICKY;
    }

    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("Service", "channel_service", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("安全定位服务")
                    .setContentText("正在后台定位")
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

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d("test","GuardService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            startService(new Intent(GuardService.this,BackgroundLocationService.class));
            //重新绑定
            bindService(new Intent(GuardService.this,BackgroundLocationService.class),
                    mServiceConnection, getApplicationContext().BIND_IMPORTANT);
        }
    };

}
