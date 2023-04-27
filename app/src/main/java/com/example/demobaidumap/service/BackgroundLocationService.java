package com.example.demobaidumap.service;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static com.example.demobaidumap.ui.gallery.GalleryFragment.GEOFENCE_BROADCAST_ACTION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.baidu.geofence.GeoFence;
import com.baidu.geofence.GeoFenceClient;
import com.baidu.geofence.GeoFenceListener;
import com.baidu.geofence.model.DPoint;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.demobaidumap.MyNavigation;
import com.example.demobaidumap.NotificationUtils;
import com.example.demobaidumap.R;
import com.example.demobaidumap.ui.gallery.GalleryFragment;
import com.example.demobaidumap.util.StepService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BackgroundLocationService extends Service {
    private static final String CHANNEL_ID = "Service";
    private LocationClient mLocationClient = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub(){
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext().getApplicationContext();
        // 用户同意隐私条款
        SDKInitializer.setAgreePrivacy(context,true);
        // 初始化百度地图SDK
        SDKInitializer.initialize(context);
        // 指定百度地图使用地图类型
        SDKInitializer.setCoordType(CoordType.BD09LL);

        LocationClient.setAgreePrivacy(true);
        try {
            // 获取定位实例
            mLocationClient = new LocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 地图配置
        requestLocation();
        // 注册位置监听
        mLocationClient.registerLocationListener(new MyLocationListener());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLock(getApplicationContext());
//        startForeground(1,new Notification());
//        startForeground();
        startMyOwnForeground();
        //绑定建立链接
        bindService(new Intent(this,GuardService.class),
                mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
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
    public void onDestroy() {
        super.onDestroy();
        releaseLock();
    }

    /**
     * 同步方法   得到休眠锁
     * @param context
     * @return
     */
    PowerManager.WakeLock mWakeLock = null;
    synchronized private void getLock(Context context){
        if(mWakeLock==null){
            PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BackgroundLocationService.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour =c.get(Calendar.HOUR_OF_DAY);
            if(hour>=23||hour<=6){
                mWakeLock.acquire(5000);
            }else{
                mWakeLock.acquire(300000);
            }
        }
        Log.v("DemoBaiduMap lock","get lock");
    }

    synchronized private void releaseLock()
    {
        if(mWakeLock!=null){
            if(mWakeLock.isHeld()) {
                mWakeLock.release();
                Log.v("DemoBaiduMap unlock","release lock");
            }

            mWakeLock=null;
        }
    }



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d("test","StepService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            startService(new Intent(BackgroundLocationService.this,GuardService.class));
            //重新绑定
            bindService(new Intent(BackgroundLocationService.this,GuardService.class),
                    mServiceConnection, Context.BIND_IMPORTANT);
        }
    };



    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        // LocationMode.Hight_Accuracy：高精度  LocationMode.Battery_Saving：低功耗
        // LocationMode.Device_Sensors：仅使用设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 设置返回经纬坐标类型 GCJ02：国测局坐标  BD09ll：百度经纬度坐标  BD09：百度墨卡托坐标
        option.setCoorType("bd09ll");
        //可选，设置是否需要设备方向结果
        option.setNeedDeviceDirect(true);
        // 扫描次数
        option.setScanSpan(100);
        // 开启GPS
        option.setOpenGps(true);
        // 需要位置信息
        option.setIsNeedAddress(true);
        option.setIsNeedLocationPoiList(true);
        //可选，是否需要位置描述信息，默认为不需要，即参数为false
        option.setIsNeedLocationDescribe(true);

        // GPS有效时1s/1次频率输出GPS结果
        option.setLocationNotify(true);
        // 是否在stop时杀死这个进程，建议否
        option.setIgnoreKillProcess(false);
        // 是否收集Crash信息，默认收集false
        option.SetIgnoreCacheException(false);
        // 首次定位时判断当前wifi是否超过有效期
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        // 是否需要过滤GPS仿真结果，默认需要false
        option.setEnableSimulateGnss(false);

        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者。
        // 定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        option.setOpenAutoNotifyMode(2000, 2, LocationClientOption.LOC_SENSITIVITY_HIGHT);

        mLocationClient.setLocOption(option);

    }

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation Location) {
            if (Location == null) {
                return;
            }

           Log.e("BgService Location",Location.getLatitude()+""+Location.getLongitude());

//            navigateTo(Location);
            Log.d("getLocationWhere",""+Location.getLocationWhere());
            if(Location.getLocationWhere() == Location.LOCATION_WHERE_IN_CN){
                Log.e("home position",Location.getLatitude()+","+Location.getLongitude());
                setTrackLocation(Location);
            }else{
                Log.e("home position else",Location.getLatitude()+","+Location.getLongitude());
            }

        }
    }

    List<LatLng> localTrack = new ArrayList();;
    Boolean doOneCreateRail = true;
    int count = 1;
    // 存储定位信息，回看轨迹
    public void setTrackLocation(BDLocation location){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Track", MODE_PRIVATE);

        // 干扰数据去除
        if(count<=3){
            count++;
            Log.e("return",""+count);
            return;
        }

        // 判断国内外
        if(location.getLocationWhere() == BDLocation.LOCATION_WHERE_OUT_CN){
            Log.e("Location", "aboard" );
        }else{
            Log.e("Location","home");

            SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", Context.MODE_MULTI_PROCESS);
            boolean fenceCreated = pref.getBoolean("fenceSuccess", false);
            double rail_latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
            double rail_longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
            if(fenceCreated && doOneCreateRail && rail_latitude!=0.0 && rail_longitude!=0.0){
                createRail(rail_latitude, rail_longitude);
                doOneCreateRail = false;
            }

            // 当前位置
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            localTrack.add(ll);
            Log.e("in position","ok");

            String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String track = prefs.getString("track"+ nowadays, "");

            if(track == ""){
                Gson gson = new Gson();
                String json_array = gson.toJson(localTrack);
                Log.e("first add track","ok");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("track" + nowadays, ""+json_array);
                editor.apply();
            }else{
                // 字符串转JsonArray
                JsonArray jsonArray = new JsonParser().parse(track).getAsJsonArray();

                // 遍历 JsonArray 并将其转换回对象
                List<LatLng> newList = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Double latitude = jsonObject.get("latitude").getAsDouble();
                    Double longitude = jsonObject.get("longitude").getAsDouble();
                    newList.add(new LatLng(latitude, longitude));
                }

                // 本地今日键名轨迹数据最后一个
                LatLng finalLat = newList.get(newList.size()-1);

                // 两经纬度差值距离
                float[] results = new float[1];
                Location.distanceBetween(finalLat.latitude, finalLat.longitude, ll.latitude, ll.longitude, results);

                if(results[0] > 2 && results[0] < 30){
                    newList.add(ll);
                    // 更新当前位置节点后存储本地
                    Gson gson = new Gson();
                    String json_newList_array = gson.toJson(newList);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("track" + nowadays, ""+json_newList_array);
                    editor.apply();
                }

            }

        }


    }

    //  创建电子围栏入口
    public void createRail(double endLatitude, double endLongitude){
        // 实例化地理围栏客户端
        GeoFenceClient mGeoFenceClient = new GeoFenceClient(getApplicationContext());

        // 设置希望侦测的围栏触发行为，默认只侦测用户进入围栏的行为
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN_OUT_STAYED);

        // 设置进入围栏、离开围栏、在围栏内停留三种侦听行为的触发次数
        mGeoFenceClient.setTriggerCount(3, 3, 2);

        //创建一个中心点坐标
        DPoint centerPoint = new DPoint(endLatitude, endLongitude);

        int rail_radius = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).getString("rail_radius", "10000"));
        int mRadius = rail_radius;

        mGeoFenceClient.addGeoFence(centerPoint, GeoFenceClient.BD09LL, mRadius, "0001");

        Log.e("WeiLan", "Will in");

        // 创建回调监听
        GeoFenceListener fenceListenter = new GeoFenceListener() {
            @Override
            public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
                Log.i("WeiLan", errorCode+"inner"+s);
                if(errorCode == GeoFence.ADDGEOFENCE_SUCCESS){//判断围栏是否创建成功
                    Toast.makeText(getApplicationContext(), "添加围栏成功!!", Toast.LENGTH_SHORT).show();
                    Log.e("WeiLan", "ok");
                    //geoFenceList是已经添加的围栏列表，可据此查看创建的围栏
                } else {
                    Toast.makeText(getApplicationContext(), "添加围栏失败!!", Toast.LENGTH_SHORT).show();
                    Log.e("WeiLan", "fail");
                }
            }
        };
        mGeoFenceClient.setGeoFenceListener(fenceListenter);

        //创建并设置PendingIntent：将在围栏事件被触发时触发
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        getApplicationContext().registerReceiver(mGeoFenceReceiver, filter);
    }

    //  地理围栏回调 进入 离开 停留
    //根据围栏id 记录每个围栏的状态
    /*
    获取自定义的围栏标识：
    String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
    //获取当前有触发的围栏对象：
    GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
    * */
    private HashMap<String, Integer> fenceIdMap = new HashMap<>();
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                //获取Bundle：解析广播内容
                Bundle bundle = intent.getExtras();
                //获取围栏行为：
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                //获取围栏ID:
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //改变数据类型
                fenceIdMap.put(fenceId, status);
                Message msg = Message.obtain();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL:
                        Toast.makeText(context, "定位失败",
                                Toast.LENGTH_SHORT).show();
                        msg.what = 3;
                        handler.sendMessage(msg);
                        break;
                    case GeoFence.STATUS_IN:
                        Toast.makeText(context, "进入围栏",
                                Toast.LENGTH_SHORT).show();
                        msg.what = 4;
                        handler.sendMessage(msg);
                        break;
                    case GeoFence.STATUS_OUT:
                        Toast.makeText(context, "离开围栏",
                                Toast.LENGTH_SHORT).show();
                        msg.what = 5;
                        handler.sendMessage(msg);
                        break;
                    case GeoFence.STATUS_STAYED:
                        msg.what = 4;
                        handler.sendMessage(msg);
                        Toast.makeText(context, "停留在围栏内",
                                Toast.LENGTH_SHORT).show();

                        break;
                    default:
                        break;
                }
            }
        }
    };


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.e("添加围栏成功","ok");
                    Toast.makeText(getApplicationContext(), "添加围栏成功",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Log.e("添加围栏失败","err");
                    Toast.makeText(getApplicationContext(), "添加围栏失败" + errorCode,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    break;
                case 3:
//                    tv.setText("定位失败");
                    Log.e("定位失败","err");
                    break;
                case 4:
                    boolean isInFence = false;//默认false
                    for (Integer value : fenceIdMap.values()) {
                        if (value == GeoFence.STATUS_IN) {
                            isInFence = true;
                        }
                    }
                    if (isInFence) {
                        Log.e("进入围栏 back","Ok");
                    } else {
                        Log.e("离开围栏 back","leave");
                        callPhone();
                    }
                    break;
                case 5:
                    Log.e("离开围栏 back","leave2");
                    callPhone();
                    break;
                case 6:
                    Log.e("停留围栏","step");
                    break;
                default:
                    break;
            }
        }
    };

    public void callPhone(){
        Log.e("call phone","ok");
        String phoneNumber = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).getString("pre_key_phone", null);
        Log.e("phoneNumber",""+phoneNumber);
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));

        // 添加拨打电话的权限
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        Log.e("call phone","have permission");

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
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
            getApplicationContext().startActivity(dialIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 发起拨打电话
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(dialIntent);
//        }
    }


}
