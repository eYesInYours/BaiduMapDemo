package com.example.demobaidumap;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<Double> latitude = new MutableLiveData<>();
    private MutableLiveData<Double> longitude = new MutableLiveData<>();

    public SharedViewModel() {
        latitude.setValue(0.0);
        longitude.setValue(0.0);
    }

    public void checkLiveDataInstance() {
        Log.e("Lat LiveData instance", "" + latitude);
    }


    public void setSelectedLatitude(double latitude) {
        this.latitude.setValue(latitude);
        Log.e("setValue latitude",""+this.latitude.getValue());
    }

    public void setSelectedLongitude(double longitude) {
        this.longitude.setValue(longitude);
        Log.e("setValue longitude",""+this.longitude.getValue());
    }

    public double getLatitude(){
        Log.e("getValue latitude",""+latitude.getValue());
        return latitude.getValue()!=null ? latitude.getValue() : 0.0;
    }

    public double getLongitude(){
        Log.e("getValue longitude",""+longitude.getValue());
        return longitude.getValue()!=null ? longitude.getValue() : 0.0;
    }

//    private double latitude;
//    private double longitude;
//    private final Object lock = new Object();
//
//    public double getLatitude() {
//        Log.e("getValue longitude",""+this.latitude);
//        return this.latitude;
//    }
//
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//        Log.e("setValue latitude",""+this.latitude);
//    }
//
//    public double getLongitude() {
//        Log.e("getValue longitude",""+this.longitude);
//        return this.longitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//        Log.e("setValue latitude",""+this.longitude);
//    }
}

