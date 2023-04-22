package com.example.demobaidumap.service;

import static com.example.demobaidumap.ui.gallery.GalleryFragment.GEOFENCE_BROADCAST_ACTION;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
import com.example.demobaidumap.R;
import com.example.demobaidumap.ui.gallery.GalleryFragment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BackgroundLocationService extends Service {
    private LocationClient mLocationClient = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext().getApplicationContext();
        SDKInitializer.setAgreePrivacy(context,true);
        SDKInitializer.initialize(context);
        SDKInitializer.setCoordType(CoordType.BD09LL);

        LocationClient.setAgreePrivacy(true);
        try {
            mLocationClient = new LocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestLocation();
        mLocationClient.registerLocationListener(new MyLocationListener());





    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 将Service置于前台状态
//        Notification notification = new NotificationCompat.Builder(this, "001")
//                .setContentTitle("后台定位")
//                .setContentText("后台正在定位，可在系统中查看轨迹")
//                .setSmallIcon(R.drawable.notification)
//                .build();
//        startForeground(1, notification);

        // 返回START_STICKY，表示系统会尝试重新启动Service，即使Service在被系统终止后也会重新启动
        return START_STICKY;
    }


    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，设置定位模式，默认高精度
        // LocationMode.Hight_Accuracy：高精度
        // LocationMode.Battery_Saving：低功耗
        // LocationMode.Device_Sensors：仅使用设备

        option.setCoorType("bd09ll");
        // 可选，设置返回经纬坐标类型，默认GCJ02
        // GCJ02：国测局坐标
        // BD09ll：百度经纬度坐标

        // BD09：百度墨卡托坐标

        //可选，设置是否需要设备方向结果
        option.setNeedDeviceDirect(true);

        option.setScanSpan(1000);

        option.setOpenGps(true);

        option.setIsNeedAddress(true);
        option.setIsNeedLocationPoiList(true);

        option.setLocationNotify(true);
        // GPS有效时1s/1次频率输出GPS结果

        option.setIgnoreKillProcess(false);
        // 是否在stop时杀死这个进程，建议否

        option.SetIgnoreCacheException(false);
        // 是否手机Crash信息，默认收集false

        option.setWifiCacheTimeOut(5 * 60 * 1000);
        // 首次定位时判断当前wifi是否超过有效期

        option.setEnableSimulateGnss(false);
        // 是否需要过滤GPS仿真结果，默认需要false

        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        //可选，是否需要位置描述信息，默认为不需要，即参数为false

        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        option.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);

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
            if(Location.getLocationWhere() == BDLocation.LOCATION_WHERE_IN_CN){
                Log.e("home position",Location.getLatitude()+","+Location.getLongitude());
                setTrackLocation(Location);
            }



        }
    }

    List<LatLng> localTrack = new ArrayList();;
    Boolean doOneCreateRail = true;
    // 存储定位信息，回看轨迹
    public void setTrackLocation(BDLocation location){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Track", MODE_PRIVATE);

        // 判断国内外
        if(location.getLocationWhere() == BDLocation.LOCATION_WHERE_OUT_CN){
            Log.e("Location", "aboard" );
        }else{
            Log.e("Location","home");

            SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", Context.MODE_MULTI_PROCESS);
            boolean fenceCreated = pref.getBoolean("fenceSuccess", false);
            double rail_latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
            double rail_longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
            if(fenceCreated && doOneCreateRail){
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
                JsonArray jsonArray = new JsonParser().parse(track).getAsJsonArray();
//            Log.e("json list",""+jsonArray);

                // 3. 遍历 JsonArray 并将其转换回对象
                List<LatLng> newList = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Double latitude = jsonObject.get("latitude").getAsDouble();
                    Double longitude = jsonObject.get("longitude").getAsDouble();
                    newList.add(new LatLng(latitude, longitude));
                }

                LatLng finalLat = newList.get(newList.size()-1);

                float[] results = new float[1];
                Location.distanceBetween(finalLat.latitude, finalLat.longitude, ll.latitude, ll.longitude, results);

                if(results[0] > 4 && results[0] < 6){
                    Log.e("more than 40","ok");
                    newList.add(ll);
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
        //实例化地理围栏客户端
        GeoFenceClient mGeoFenceClient = new GeoFenceClient(getApplicationContext());

        //设置希望侦测的围栏触发行为，默认只侦测用户进入围栏的行为
        //public static final int GEOFENCE_IN 进入地理围栏
        //public static final int GEOFENCE_OUT 退出地理围栏
        //public static final int GEOFENCE_STAYED 在地理围栏内停留
        //public static final int GEOFENCE_IN_OUT 进入、退出地理围栏
        //public static final int GEOFENCE_IN_STAYED 进入地理围栏、在地理围栏内停留
        //public static final int GEOFENCE_OUT_STAYED 退出地理围栏、在地理围栏内停留
        //public static final int GEOFENCE_IN_OUT_STAYED 进入、退出、停留
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN_OUT_STAYED);

        /**
         * setTriggerCount(int in, int out, int stay)
         * 设置进入围栏、离开围栏、在围栏内停留三种侦听行为的触发次数
         * @param in 进入围栏的触发次数,类型为int,必须是>=0
         * @param out 离开围栏的触发次数,类型为int,必须是>=0
         * @param stay 在围栏内停留的触发次数,类型为int,必须是>=0
         */
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

        //创建并设置PendingIntent
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        getApplicationContext().registerReceiver(mGeoFenceReceiver, filter);
    }

    //  地理围栏回调 进入 离开 停留
    //根据围栏id 记录每个围栏的状态
    private HashMap<String, Integer> fenceIdMap = new HashMap<>();
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                //解析广播内容
                //获取Bundle
                Bundle bundle = intent.getExtras();
                //获取围栏行为：
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                //获取自定义的围栏标识：
                String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                //获取围栏ID:
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //获取当前有触发的围栏对象：
                GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
                Log.i("sss", "获取围栏行为:" + status);
                Message msg = Message.obtain();
                //改变数据类型
                fenceIdMap.put(fenceId, status);
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
//                    tv.setText("添加围栏成功");
                    Log.e("添加围栏成功","ok");
                    Toast.makeText(getApplicationContext(), "添加围栏成功",
                            Toast.LENGTH_SHORT).show();
                    //开始画围栏
//                    drawFence2Map();
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

                    //遍历map 初始化是否在围栏里
                    boolean isInFence = false;//默认false
                    for (Integer value : fenceIdMap.values()) {
                        if (value == GeoFence.STATUS_IN) {
                            isInFence = true;
                        }
                    }
                    if (isInFence) {
                        Log.e("进入围栏 back","Ok");
                    } else {
//                        tv.setText("离开围栏");
                        Log.e("离开围栏 back","leave");
                        callPhone();
                    }
                    break;
                case 5:
//                    tv.setText("离开围栏");
                    Log.e("离开围栏 back","leave2");
                    callPhone();
                    break;
                case 6:
//                    tv.setText("停留围栏");
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
