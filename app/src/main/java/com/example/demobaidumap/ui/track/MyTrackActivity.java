package com.example.demobaidumap.ui.track;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.demobaidumap.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MyTrackActivity extends Activity {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    String selected_track_data;
    private LocationClient locationClient = null;
    List<LatLng> newList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext().getApplicationContext();
        SDKInitializer.setAgreePrivacy(context,true);
        SDKInitializer.initialize(context);
        SDKInitializer.setCoordType(CoordType.BD09LL);

        // option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        LocationClient.setAgreePrivacy(true);
        try {
            locationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error",""+e);
        }
        locationClient.registerLocationListener(new MyLocationListener());

        initLocation();
        locationClient.start();

        setContentView(R.layout.activity_track);
        //初始化地图相关
        mMapView = findViewById(R.id.track);
        mBaiduMap = mMapView.getMap();

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapStatus(msu);

        Intent intent = getIntent();
        selected_track_data = intent.getStringExtra("date");

        // 1. 从本地从获取数据
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Track", MODE_PRIVATE);
        String track = prefs.getString("track"+selected_track_data, "");

        // JSON数组转换成ArrayList
        // 2. 将 JSON 字符串转换成 JsonArray 对象
        JsonArray jsonArray = new JsonParser().parse(track).getAsJsonArray();
        Log.e("json list",""+jsonArray);

        // 3. 遍历 JsonArray 并将其转换回对象
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            Double latitude = jsonObject.get("latitude").getAsDouble();
            Double longitude = jsonObject.get("longitude").getAsDouble();
            newList.add(new LatLng(latitude, longitude));
        }

        // 打印遍历后的 newList
        for (LatLng item : newList) {
            System.out.println(item.latitude + ", " + item.longitude);
        }

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

        option.setScanSpan(3000);

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

        locationClient.setLocOption(option);

    }

    private class MyLocationListener extends BDAbstractLocationListener {

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

            showTrack();

            String locationDescribe = Location.getLocationDescribe();    //获取位置描述信息，比如：在北江豪庭附近

        }
    }

    /*
     * 定位当前位置
     * 从track数组第一个点开始，进行轨迹绘制
     * */
    boolean isFirstLocate = true;
    private void navigateTo(LatLng latlng){
        LatLng ll = new LatLng(latlng.latitude, latlng.longitude);

        if(isFirstLocate) {
            Log.e("isFirst",""+isFirstLocate);
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

        // 显示当前定位的小箭头
//        MyLocationData locData = new MyLocationData.Builder()
////                .accuracy(Location.getRadius())
////                .direction(Location.getDirection())
//                .latitude(latlng.latitude)
//                .longitude(latlng.longitude)
//                .build();
//        mBaiduMap.setMyLocationData(locData);
    }

    // 获取轨迹数据与绘制
    public void showTrack(){
        Log.e("first array i",""+newList.get(0));
        navigateTo(newList.get(0));
    }


}
