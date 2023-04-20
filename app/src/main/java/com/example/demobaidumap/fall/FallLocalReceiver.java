package com.example.demobaidumap.fall;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.demobaidumap.MainActivity;
import com.example.demobaidumap.R;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FallLocalReceiver extends BroadcastReceiver {

    private TextView countingView;
    private Dialog dialog;
    private Timer timer;
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private boolean isVibrate;
    private MediaPlayer mediaPlayer;

    private LocationClient mLocationClient = null;
    public String locationAddress;
    public String locationTime;
    private Context context;
    private final String TAG = "HELLO WORLD";

    private volatile int showCountTime = 15;

    public FallLocalReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "FallLocalReceiver.onReceive()");
        this.context = context;
        showAlertDialog();

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        isVibrate = sharedPreferences.getBoolean("pre_key_vibrate", true);
        if(isVibrate){
            startVibrate();
        }

//        startAlarm();
        startLocation();

//        callPhone();
    }

    // 拨打电话 Context context
    public void callPhone(){
        Log.e("call phone","ok");
        String phoneNumber = sharedPreferences.getString("pre_key_phone", null);
        Log.e("phoneNumber",""+phoneNumber);
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));

        // 添加拨打电话的权限
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Log.e("call phone","have permission");

            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (telephonyManager.getPhoneCount() > 1) {
                        // 获取第一个电话卡的subId
                        final int subIdForSlot;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            subIdForSlot = SubscriptionManager.getDefaultSubscriptionId();
                            if (subIdForSlot != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                                ComponentName componentName = new ComponentName("com.android.phone", "com.android.services.telephony.TelephonyConnectionService");
                                PhoneAccountHandle phoneAccountHandle = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    phoneAccountHandle = new PhoneAccountHandle(componentName, String.valueOf(subIdForSlot));
                                }
                                dialIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle);
                            }
                        }

                    }
                }
                dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(dialIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 发起拨打电话
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dialIntent);
        }
    }

    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation Location) {
            if (Location != null) {

                if (Location.getLocType() == BDLocation.TypeGpsLocation ||
                        Location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 定位成功回调信息，设置相关消息
                    locationAddress = Location.getAddrStr();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // 将当前时间转换为上一行格式
                    locationTime = df.format(new Date(System.currentTimeMillis()));
                    // locationTime = df.format(new Date(Location.getTime()));

                } else {
                    // 显示错误信息
                    Log.e("BaiduError","location Error, ErrCode:"
                            + Location.getLocType() + ", errInfo:"
                            + Location.getLocTypeDescription());
                }

            }


        }

    }


    /*
    弹窗报警
     */
private void showAlertDialog() {
        Log.e("showDialog","show");
        AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext());

        countDown();

        builder.setTitle("跌倒警报");
        builder.setMessage("检测到跌倒发生，是否发出警报？\n"+showCountTime+"秒后预警结束，将通知预置联系人");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //点击取消按钮后的操作
                timer.cancel();
                dialog.dismiss();
                if(isVibrate){
                    stopVibrate();
                }
    //            stopAlarm();
                Intent startIntent = new Intent(context.getApplicationContext(), FallDetectionService.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // add this flag
                context.startService(startIntent);

                Log.e("Dialog","Dialog cancel");
            }
        });

        AlertDialog alertDialog = builder.create();
        if (context instanceof Activity) {
            Log.e("1","1");
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
                wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else {
                wmParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }

            Log.e("2","2");

            alertDialog.getWindow().setType(wmParams.type);
            alertDialog.show();

        }

        Log.d("Dialog", "dialog.create()");
    }





    /*
    倒计时
     */
    private void countDown() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int countTime = 10;
            @Override
            public void run() {
                if(showCountTime > 0){
                    showCountTime --;
                }
                synchronized (handler){
                    Message msgTime = handler.obtainMessage();
                    msgTime.arg1 = showCountTime;
                    handler.sendMessage(msgTime);
                }
            }
        };
        timer.schedule(timerTask, 50, 1000);
    }

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
             Log.e("if msg",""+msg.arg1);
            if(msg.arg1 > 0){
                Log.e("msg",""+msg.arg1);
                Log.e("in if",""+dialog);
            }else{
                Log.e("in else",""+msg.arg1);
                Log.e("dialog",""+dialog);
                //倒计时结束自动关闭
                if(dialog != null){
                    dialog.dismiss();
                    if(isVibrate){
                        stopVibrate();
                    }
                    Log.e("countdown finish","ok");
//                    stopAlarm();
                    callPhone();
                    sendSMS(locationAddress, locationTime);
                }else{
//                    dialog.dismiss();
                    if(isVibrate){
                        stopVibrate();
                    }
                    Log.e("location info",locationAddress+"@"+locationTime);
                    sendSMS(locationAddress, locationTime);
                    callPhone();
                }
                timer.cancel();
            }
        }
    };

    /*
    开始震动
     */
    private void startVibrate(){
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500};
        vibrator.vibrate(pattern, 2);
    }
    /*
    停止震动
     */
    private void stopVibrate(){
        vibrator.cancel();
    }

    /*
    开始播放铃声
     */
    private void startAlarm(){
        String ringtone = sharedPreferences.getString("pre_key_alarm" , null);
        Log.d(TAG, ringtone + "");
        Uri ringtoneUri = Uri.parse(ringtone);

        mediaPlayer = MediaPlayer.create(context, ringtoneUri);
        mediaPlayer.setLooping(true);//设置循环
        mediaPlayer.start();
    }
    /*
    停止播放铃声
     */
    private void stopAlarm(){
        mediaPlayer.stop();
    }

    private void sendSMS(String address, String time){
        //获取短信管理器
        SmsManager smsManager = SmsManager.getDefault();

        String name = sharedPreferences.getString("pre_key_name", null);
//        String phoneNum = sharedPreferences.getString("pre_key_phone", null);
        String phoneNum = "16673854459";
        String smsContent = "预警提醒：您的被监护人可能发生跌倒，45秒内未取消预警！\n"
                            + "请前往 ["+ address +"] 查看，" + time + "监听";
        try {
            smsManager.sendTextMessage(phoneNum, null, smsContent ,null, null);
            Toast.makeText(context, "短信已经发出", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "短信发送失败，请检查您的短信设置", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    private void startLocation(){
        Log.d(TAG, "FallLocalReceiver.startLocation()");
        // 创建定位客户端实例
        LocationClient.setAgreePrivacy(true);
        try {
            mLocationClient = new LocationClient(context.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationClient.registerLocationListener(new MyLocationListener());


        // 创建定位参数实例
        LocationClientOption option = new LocationClientOption();
        // 设置定位模式为高精度模式
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 获取最近3s内精度最高的一次定位结果
        option.setScanSpan(3000);
        // 设置是否需要地址信息，默认为无地址信息
        option.setIsNeedAddress(true);
        // 设置定位参数
        mLocationClient.setLocOption(option);

        // 启动定位
        mLocationClient.start();
    }


}
