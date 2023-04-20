package com.example.demobaidumap.ui.gallery;

import com.baidu.mapapi.map.CircleOptions;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getSystemService;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.track.TraceOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.impl.BaiduNaviManager;
import com.baidu.navisdk.adapter.map.BNItemOverlay;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.baidu.navisdk.comapi.mapcontrol.MapParams;
import com.example.demobaidumap.MainActivity;
import com.example.demobaidumap.MyNavigation;
import com.example.demobaidumap.NotificationUtils;
import com.example.demobaidumap.R;
import com.example.demobaidumap.SharedViewModel;
//import com.example.demobaidumap.StepService;
import com.example.demobaidumap.fall.FallDetectionService;
import com.example.demobaidumap.search.Poi;
import com.example.demobaidumap.search.SearchActivity;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

public class GalleryFragment extends Fragment implements SensorEventListener,SearchActivity.OnDataPassListener {
//    private FragmentGalleryBinding binding;
    private FragmentActivity mActivity;

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

    // 初始化POI检索
    private PoiSearch mPoiSearch = null;
    ListView mPoiListView;
//    private PoiListAdapter mAdapter;
    LinearLayout searchLayout;
    ImageView searchButton;

    private SharedViewModel sharedViewModel;

    private double startLatitude;
    private double startLongitude;

    GeoFenceClient mGeoFenceClient;
    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "liyue.edu.ncst.cn.mymap.DeleveryInfo";
    //根据围栏id 记录每个围栏的状态
    private HashMap<String, Integer> fenceIdMap = new HashMap<>();

    // 设置电子围栏存入本地的中心经纬
    Double latitude;
    Double longitude;

    Context context;


    private Context mContext;
//    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext().getApplicationContext();
        SDKInitializer.setAgreePrivacy(context,true);
        SDKInitializer.initialize(context);
        SDKInitializer.setCoordType(CoordType.BD09LL);

        context = getContext().getApplicationContext();


        // 该行要加上，否则下面获取实例为null
        LocationClient.setAgreePrivacy(true);
        try {
            mLocationClient = new LocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestLocation();
        mLocationClient.registerLocationListener(new MyLocationListener());

        SharedPreferences pref = requireActivity().getSharedPreferences("myPrefs", Context.MODE_MULTI_PROCESS);
        boolean fenceCreated = pref.getBoolean("fenceSuccess", false);
        latitude = Double.parseDouble(pref.getString("latitude","0.0"));
        longitude = Double.parseDouble(pref.getString("longitude","0.0"));



        Log.e("fenceCreated",""+fenceCreated);
        Log.e("fenceCreated",""+latitude);
        Log.e("fenceCreated",""+longitude);
        if(fenceCreated){
            createRail(latitude, longitude);
        }

    }


    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);
        Context context = getContext().getApplicationContext();

        mMapView = view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        searchButton = view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 1);

            }
        });
//        searchLayout = searchLayout.findViewById(R.id.search_layout);

        mForegroundBtn = (Button) view.findViewById(R.id.bt_foreground);
//        locationInfo = view.findViewById(R.id.locationInfo);

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

        // 方向方法
        registerDirection();

        // 计步方法
//        registerStepCounter(stepFlag);


        // 后台定位
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
                    // 开启后台定位
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
//        if(ContextCompat.checkSelfPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
//        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            getContext().startActivity(intent);
        }

//        // 该行要加上，否则下面获取实例为null
//        LocationClient.setAgreePrivacy(true);
//        try {
//            mLocationClient = new LocationClient(context);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        System.out.println("LocationClient实例："+mLocationClient);
//        mLocationClient.registerLocationListener(new MyLocationListener());
//
//        requestLocation();

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

            setTrackLocation(Location);


            String locationDescribe = Location.getLocationDescribe();    //获取位置描述信息，比如：在北江豪庭附近

        }
    }


    // 搜索结果回调
    private OnGetPoiSearchResultListener mPoiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult != null && poiResult.getAllPoi() != null) {

            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };


    List<LatLng> localTrack = new ArrayList();;
    Boolean oneSetLastLat = true;
    LatLng lastPosition;    // 上一次位置
    // 存储定位信息，回看轨迹
    public void setTrackLocation(BDLocation location){
        // 当前位置
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        localTrack.add(ll);
        Log.e("in position","ok");

        String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if(oneSetLastLat){
            lastPosition = ll;
            oneSetLastLat = false;

            // ArrayList 转 JSON
            Gson gson = new Gson();
            String json_array = gson.toJson(localTrack);

            

            SharedPreferences prefs = getActivity().getSharedPreferences("Track", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("track" + nowadays, ""+json_array);
            editor.apply();

            Log.e("one set lastpostion","return");
        }

        float[] results = new float[1];
        Location.distanceBetween(lastPosition.latitude, lastPosition.longitude, ll.latitude, ll.longitude, results);

        Log.e("distance between",""+results[0]);

        // 判断如果间距大于100则存入本地，并更新lastPosition为现在位置
        if(results[0] >= 20){
            Log.e("distance more 100","ok");

            // ArrayList 转 JSON
            Gson gson = new Gson();
            String json_array = gson.toJson(localTrack);

            

            SharedPreferences prefs = getActivity().getSharedPreferences("Track", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("track" + nowadays, ""+json_array);
            editor.apply();

            lastPosition = ll;
            Log.e("setTrackLocation",""+localTrack);
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
            // 轨迹
//            OverlayOptions mOverLay = new PolylineOptions()
//                    .width(10)
//                    .color(0xAAFF0000)
//                    .points(ppp);
//
//            Overlay mPolyline = (Polyline)mBaiduMap.addOverlay(mOverLay);
//            mPolyline.setZIndex(3);

        }

        Log.e("MyMap",  " latitude:" + Location.getLatitude()
                + " longitude:" + Location.getLongitude() + "  数组长度：" +ppp.size());
        this.startLatitude = Location.getLatitude();
        this.startLongitude = Location.getLongitude();
        Log.i("location",this.startLatitude+"-"+this.startLongitude);

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
    public void onDataPass(Poi poi) {
        Log.e("poi",""+poi);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }



    double endLat;
    double endLong;
    int mRadius;
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            int parttern = data.getIntExtra("parttern", 0);
            Log.e("parttern", "" + parttern);
            double endLatitude = data.getDoubleExtra("latitude", 0.0);  // 获取纬度数据
            double endLongitude = data.getDoubleExtra("longitude", 0.0);  // 获取经度数据
            endLat = endLatitude;
            endLong = endLongitude;
            // 在A页面中使用获取到的坐标数据进行导航
            Log.e("warning", this.startLatitude + "" + this.startLongitude);
            Log.e("error", endLatitude + "" + endLatitude);

            // 1 导航 2创建地理围栏
            if (parttern == 1) {
                try {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("baidumap://map/direction?destination=latlng:" + endLatitude + "," + endLongitude + "|name:目的地&mode=driving"));
                    intent.setPackage("com.baidu.BaiduMap");
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                        Log.e("LOG LOGIN", "百度地图客户端已经安装");
                    } else {
                        Log.e("LOG FAIL", "没有安装百度地图客户端");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                createRail(endLatitude, endLongitude);
            }

        }
    }

    //  创建电子围栏入口
    public void createRail(double endLatitude, double endLongitude){
        //实例化地理围栏客户端
        GeoFenceClient mGeoFenceClient = new GeoFenceClient(getContext());

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
                .getDefaultSharedPreferences(getContext()).getString("rail_radius", "10000"));
        mRadius = rail_radius;

        mGeoFenceClient.addGeoFence(centerPoint, GeoFenceClient.BD09LL, mRadius, "0001");

        Log.e("WeiLan", "Will in");

        // 创建回调监听
        GeoFenceListener fenceListenter = new GeoFenceListener() {
            @Override
            public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
                Log.i("WeiLan", errorCode+"inner"+s);
                if(errorCode == GeoFence.ADDGEOFENCE_SUCCESS){//判断围栏是否创建成功
                    Toast.makeText(getContext(), "添加围栏成功!!", Toast.LENGTH_SHORT).show();
                    Log.e("WeiLan", "ok");
                    //geoFenceList是已经添加的围栏列表，可据此查看创建的围栏
                } else {
                    Toast.makeText(getContext(), "添加围栏失败!!", Toast.LENGTH_SHORT).show();
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
        getActivity().registerReceiver(mGeoFenceReceiver, filter);
    }


    private void drawCircle(LatLng centerLatLng, int radius) {
            // Define circle options
            CircleOptions circleOptions = new CircleOptions()
                    .center(centerLatLng) // Set the center of the circle
                    .radius(radius) // Set the radius in meters
                    .fillColor(0x30ff0000); // Set the fill color (semi-transparent red)
            mBaiduMap.addOverlay(circleOptions);
            // Define marker options
            
            // Define marker options
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(centerLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_center)); // Set the icon of the marker
            mBaiduMap.addOverlay(markerOptions);

        }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                    tv.setText("添加围栏成功");
                    Log.e("添加围栏成功","ok");
                    Toast.makeText(context, "添加围栏成功",
                            Toast.LENGTH_SHORT).show();
                    //开始画围栏
//                    drawFence2Map();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Log.e("添加围栏失败","err");
                    Toast.makeText(context, "添加围栏失败" + errorCode,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    break;
                case 3:
//                    tv.setText("定位失败");
                    Log.e("定位失败","err");
                    break;
                case 4:
                    Log.e("end",endLat+"@"+endLong);
                    if(endLat == 0.0 || endLong == 0.0){
                        LatLng LocalCenter = new LatLng(latitude, longitude);
                        drawCircle(LocalCenter, mRadius);
                    }else{
                        LatLng center = new LatLng(endLat, endLong);
                        drawCircle(center, mRadius);
                    }

                    // 设置电子围栏表示，下次进入页面直接渲染
                    // 添加围栏成功，设置一个标记为true到SharedPreferences中
                    SharedPreferences sharedPreferences = mActivity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fenceSuccess", true);
                    // 将经度和纬度存储在SharedPreferences中
                    editor.putString("latitude", String.valueOf(endLat));
                    editor.putString("longitude", String.valueOf(endLong));
                    editor.apply();

                    //遍历map 初始化是否在围栏里
                    boolean isInFence = false;//默认false
                    for (Integer value : fenceIdMap.values()) {
                        if (value == GeoFence.STATUS_IN) {
                            isInFence = true;
                        }
                    }
                    if (isInFence) {
                        Log.e("进入围栏","Ok");
                    } else {
//                        tv.setText("离开围栏");
                        Log.e("离开围栏","leave");
//                        callPhone();
                    }
                    break;
                case 5:
//                    tv.setText("离开围栏");
                    Log.e("离开围栏","leave2");
//                    callPhone();
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

    //  地理围栏回调 进入 离开 停留
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

    // 拨打电话 Context context
    public void callPhone(){
        Log.e("call phone","ok");
        String phoneNumber = PreferenceManager
                .getDefaultSharedPreferences(mActivity).getString("pre_key_phone", null);
        Log.e("phoneNumber",""+phoneNumber);
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));

        // 添加拨打电话的权限
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
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
            mActivity.startActivity(dialIntent);
//        }
    }



    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        // 测试页面跳转，存储参数
    }
    


}