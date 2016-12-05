package com.klolik.weatheruscitymap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

class MapMarker implements ClusterItem {
    private double latitude;
    private double longitude;
    private String name;

    MapMarker(double lat, double lon, String name) {
        latitude = lat;
        longitude = lon;
        this.name = name;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return name;
    }
}
