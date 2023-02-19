package com.example.demobaidumap.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.example.demobaidumap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MyMapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        mMapView = view.findViewById(R.id.bmapView);
        mMapView.onCreate(getContext(), savedInstanceState);
//        mMapView.getMapAsync(this);
        return view;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
//        mBaiduMap = baiduMap;
        // 在这里可以对地图进行各种操作
    }
}

