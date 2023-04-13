package com.example.demobaidumap.search;

public class Poi {
    private String name;
    private String address;
    private double latitude;    // 纬度
    private double longitude;   // 经度

    public Poi(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPoiName() {
        return name;
    }

    public void setPoiName(String name) {
        this.name = name;
    }

    public String getPoiAddress() {
        return address;
    }

    public void setPoiAddress(String address) {
        this.address = address;
    }

    public double getPoiLatitude() {
        return latitude;
    }

    public void setPoiLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getPoiLongitude() {
        return longitude;
    }

    public void setPoiLongitude(double longitude) {
        this.longitude = longitude;
    }
}
