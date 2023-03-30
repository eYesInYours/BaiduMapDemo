package com.example.demobaidumap.ui.gallery;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.track.TraceOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.example.demobaidumap.MainActivity;
import com.example.demobaidumap.MyNavigation;
import com.example.demobaidumap.NotificationUtils;
import com.example.demobaidumap.R;
import com.example.demobaidumap.StepService;
import com.example.demobaidumap.databinding.FragmentGalleryBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements SensorEventListener {
    private FragmentGalleryBinding binding;

    TextView locationInfo;
    MapView mMapView;
    BaiduMap mBaiduMap = null;
    Polyline mPolyline;
    List<LatLng> ppp;

    // 计步传感器参数
    private Sensor stepCounter;//步伐总数传感器
    private Sensor stepDetecror;
    private SensorEventListener stepCounterListener;//步伐总数传感器事件监听器
    private SensorEventListener directionListener;// 方向传感器事件监听器
    private TriggerEventListener triggerEventListener;

    // 方向传感器参数
    private Double lastX = 0.0;
    private float mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private MyLocationData myLocationData;
    private float mCurrentAccracy;

    // 绘制轨迹，移除第一个所在
    private Boolean isRemove = true;

    SensorManager mSensorManager;
    SensorManager mSensorManager2;
    private SimpleDateFormat simpleDateFormat;//时间格式化

    boolean isFirstLocate = true;

    private LocationClient mLocationClient = null;

    // 后台实时定位参数
    private Notification mNotification;
    private Button mForegroundBtn;
    private boolean isEnableLocInForeground = false;

    private int mStep;
    private int mStepCount;
    private float StepCounter_Open=0;
    private float StepCounter_Close=0;

    private Boolean stepFlag = true;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext().getApplicationContext();
        SDKInitializer.setAgreePrivacy(context,true);
        SDKInitializer.initialize(context);
        SDKInitializer.setCoordType(CoordType.BD09LL);


        //导航初始化
//        File sdDir = null;
//        boolean sdCardExist = Environment.getExternalStorageState()
//                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
//        if(sdCardExist){
//            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
//        }
//        sdDir.toString()
        File sdcardDir = Environment.getExternalStorageDirectory();
        BaiduNaviManagerFactory.getBaiduNaviManager().init(getContext(), sdcardDir.getAbsolutePath(), "lmap",
                new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int i, String s) {
                        if(i==0)
                        {
                            Toast.makeText(getContext(), "key校验成功!", Toast.LENGTH_SHORT).show();
                        }
                        else if(i==1)
                        {
                            Toast.makeText(getContext(), "key校验失败, " + s, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void initStart() {
                        Log.e("init","start");

//                        Looper.prepare();
                        Toast.makeText(getContext(), "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
//                        Looper.loop();
                    }

                    @Override
                    public void initSuccess() {
                        Log.e("init","success");

//                        Looper.prepare();
                        Toast.makeText(getContext(), "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
//                        Looper.loop();
                        // 初始化tts
                        initTTs();

                    }

                    @Override
                    public void initFailed(int i) {
                        // 10
                        Log.e("errCode",""+i);

                        Looper.prepare();
                        Toast.makeText(getContext(), "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });

    }


    private void initTTs() {
        Log.e("initTTs","ok");
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if(sdCardExist){
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        BNTTsInitConfig.Builder builder = new BNTTsInitConfig.Builder();
        builder.context(getContext())
                .sdcardRootPath(sdDir.toString())
                .appFolderName("lmap").
                appId("31737152");
        BaiduNaviManagerFactory.getTTSManager().initTTS(builder.build());
        Log.e("initTTs","will");
        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                new IBNTTSManager.IOnTTSPlayStateChangedListener() {
                    @Override
                    public void onPlayStart() {
                        Log.e("lmap", "ttsCallback.onPlayStart");
                    }

                    @Override
                    public void onPlayEnd(String speechId) {
                        Log.e("lmap", "ttsCallback.onPlayEnd");
                    }

                    @Override
                    public void onPlayError(int code, String message) {
                        Log.e("lmap", "ttsCallback.onPlayError");
                    }
                }
        );
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);
        Context context = getContext().getApplicationContext();

        mMapView = view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        mForegroundBtn = (Button) view.findViewById(R.id.bt_foreground);

        locationInfo = view.findViewById(R.id.locationInfo);

        ppp = new ArrayList();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        Context context = getContext().getApplicationContext();

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        /*
         * 获取设备支持的传感器
         * */
//        List<Sensor> sensorsList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//        for(Sensor sensor : sensorsList){
//            Log.d("支持的传感器，",sensor.getName().toString());
//        }

        // 方向方法
        registerDirection();

        // 计步方法
//        registerStepCounter(stepFlag);



        mForegroundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(isEnableLocInForeground){
                    //关闭后台定位（true：通知栏消失；false：通知栏可手动划除）
                    mLocationClient.disableLocInForeground(true);
                    isEnableLocInForeground = false;
                    mForegroundBtn.setText(R.string.startforeground);
                    mLocationClient.stop();
                } else {
                    //开启后台定位
                    // 将定位SDK的SERVICE设置成为前台服务, 提高定位进程存活率
                    mLocationClient.enableLocInForeground(1, mNotification);
                    isEnableLocInForeground = true;
                    mForegroundBtn.setText(R.string.stopforeground);
                    mLocationClient.start();
                }
            }
        });
        // 后台持续运行
        initNotification();

        List<String> permissionList = new ArrayList<String>();

        /*
         * 这里是添加权限
         * */
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        }

        // 该行要加上，否则下面获取实例为null
        LocationClient.setAgreePrivacy(true);
        try {
            mLocationClient = new LocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("LocationClient实例："+mLocationClient);
        mLocationClient.registerLocationListener(new MyLocationListener());

        requestLocation();

        // 用户跳转系统设置
        LocationManager locationManager = (LocationManager) getSystemService(context,LocationManager.class);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            Log.e("intent","setting");
            startActivity(intent);
        }

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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class MyLocationListener extends BDAbstractLocationListener{

        @Override
        public void onReceiveLocation(BDLocation Location) {
            if (Location == null || mMapView == null) {
                return;
            }

            // 判断国内外
            if(Location.getLocationWhere() == Location.LOCATION_WHERE_OUT_CN){
                Log.e("Location", "aboard" );
            }else{
                Log.e("Location","home");
            }

            navigateTo(Location);

            String locationDescribe = Location.getLocationDescribe();    //获取位置描述信息，比如：在北江豪庭附近

            StringBuffer currentPosition = new StringBuffer(256);
            currentPosition.append("纬度: ");
            currentPosition.append(Location.getLatitude()+" , ");
            currentPosition.append( "经度: ");
            currentPosition.append(Location.getLongitude()+"\n");
            if (null != locationInfo){
                locationInfo.append(currentPosition.toString());
            }

        }
    }

    /*
     * 绘制运动轨迹的方法
     * */
    private void navigateTo(BDLocation Location){
        LatLng ll = new LatLng(Location.getLatitude(), Location.getLongitude());

        if(isFirstLocate) {
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(19f);
            mBaiduMap.animateMapStatus(update);

            // 地图设置缩放状态
            MapStatus.Builder builder = new MapStatus.Builder();
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(msu);

            isFirstLocate = false;
        }


        if(ll.longitude>0 && ll.latitude>0){
            ppp.add(ll);

            if(isRemove){
                // 删除第一个位置
                ppp.remove(0);
                System.out.println("do one");
                isRemove = false;
            }

        }
//        System.out.println(ppp.size());
        if(ppp.size()>=2){
            OverlayOptions mOverLay = new PolylineOptions()
                    .width(10)
                    .color(0xAAFF0000)
                    .points(ppp);

            Overlay mPolyline = (Polyline)mBaiduMap.addOverlay(mOverLay);
            mPolyline.setZIndex(3);

//            // 创建轨迹对象
//            TraceOptions traceOptions = initTraceOptions();
//            // 创建图标
//            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_blue);
//            // 设置轨迹动画图标并让图标平滑移动
//            traceOptions.icon(bitmap).setPointMove(true);
//            // 添加轨迹动画
//            mTraceOverlay = mBaiduMap.addTraceOverlay(traceOptions, this);
        }

        Log.e("MyMap",  " latitude:" + Location.getLatitude()
                + " longitude:" + Location.getLongitude() + "  数组长度：" +ppp.size());


        mCurrentLat = Location.getLatitude();
        mCurrentLon = Location.getLongitude();
        mCurrentAccracy = Location.getRadius();
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(Location.getRadius())
                .direction(Location.getDirection())
                .latitude(Location.getLatitude())
                .longitude(Location.getLongitude())
                .build();
        mBaiduMap.setMyLocationData(locData);
    }

    /*
     * 初始化前台服务：这里做后台可以持续定位的功能
     * */
    private void initNotification () {
        Context context = getContext().getApplicationContext();
        //设置后台定位
        //android8.0及以上使用NotificationUtils
        if ( Build.VERSION.SDK_INT >= 26) {
            NotificationUtils notificationUtils = new NotificationUtils(context);
            Notification.Builder builder = notificationUtils.getAndroidChannelNotification
                    ("适配android 8限制后台定位功能", "正在后台定位");
            mNotification = builder.build();
        } else {
            //获取一个Notification构造器
            Notification.Builder builder = new Notification.Builder(context);
            Intent nfIntent = new Intent(context, MainActivity.class);

            builder.setContentIntent(PendingIntent.
                            getActivity(context, 0, nfIntent, 0)) // 设置PendingIntent
                    .setContentTitle("适配android 8限制后台定位功能") // 设置下拉列表里的标题
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("正在后台定位") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

            mNotification = builder.build(); // 获取构建好的Notification
        }
        mNotification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("resCode","requestCode"+requestCode);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // 申请成功
                        Log.d("TAG", "[权限]" + "ACTIVITY_RECOGNITION 申请成功");
                    } else {
                        // 申请失败
                        Log.d("TAG", "[权限]" + "ACTIVITY_RECOGNITION 申请失败");
                    }
                }

                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getContext(), "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(getContext(), "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void finish() {
        throw new RuntimeException("Stub!");
    }

    // 计步传感器方法，true开始计数，false结束
//    public void registerStepCounter(Boolean A){
//        if(A == Boolean.TRUE){
//            mStepCount = 0;
//            Log.e("action","coming");
//
//            // 获取计步传感器服务：返回从开机到目前为止的步数
////            stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//
//            stepCounterListener = new SensorEventListener() {
//                @Override
//                public void onSensorChanged(SensorEvent sensorEvent) {
//
//                    Log.e("step","ok");
//                    if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
//                        if (sensorEvent.values[0] == 1.0f){
//                            mStep++;
//                            Log.e("step2","hello");
//                        }
//                    }else if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
//                        if(StepCounter_Open == 0){
//                            StepCounter_Open = sensorEvent.values[0];
//                        }
//                        StepCounter_Close = sensorEvent.values[0];
//
//                        Log.i("sensorStep",StepCounter_Open+"");
//                        Log.i("sensorStep",StepCounter_Close+"");
//
//                        int dayStep = (int)(StepCounter_Close - StepCounter_Open);
//
//                        String desc = String.format("设备检测到您当前走了%d步",dayStep);
//                        stepText.append(dayStep+"\n");
//
//                    }
//
//                }
//
//                @Override
//                public void onAccuracyChanged(Sensor sensor, int i) {
//
//                }
//            };
//
//            mSensorManager.registerListener(stepCounterListener, stepCounter, SensorManager.SENSOR_DELAY_UI);
//        }else{
//            mSensorManager.unregisterListener(stepCounterListener);
//            mStep = (int)StepCounter_Close - (int)StepCounter_Open;
//            StepCounter_Open = 0;
//            StepCounter_Close = 0;
//            String desc = String.format("设备检测到您当前走了%d步，总计数为%d步",mStep,mStepCount);
//            stepText.append(mStep+"\n");
//            Log.e("step",desc);
//        }
//
//    }

    // 方向传感器方法
    public void registerDirection(){
        // 圆点跟随手机的指向方向
        MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);
        // 获取传感器管理服务

        // 方向传感器监听
        directionListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                double x = sensorEvent.values[SensorManager.DATA_X];
                if (Math.abs(x - lastX) > 1.0) {
                    mCurrentDirection = (float) x;
                    // 构造定位图层数据
                    myLocationData = new MyLocationData.Builder()
                            .accuracy(mCurrentAccracy)
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(mCurrentDirection)
                            .latitude(mCurrentLat)
                            .longitude(mCurrentLon).build();
                    // 设置定位图层数据
                    mBaiduMap.setMyLocationData(myLocationData);
                }
                lastX = x;
//                while(mCurrentLat!=0 && mCurrentLon!=0){
//                    locationInfo.append("维度："+mCurrentLat+"经度："+mCurrentLon+"\n");
//                    break;
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        mSensorManager.registerListener(directionListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        // 关闭前台定位服务
        mLocationClient.disableLocInForeground(true);
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}