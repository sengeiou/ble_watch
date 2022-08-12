package com.szip.sport.Activity.sportResult;

import android.location.Location;
import android.os.Bundle;

import com.amap.api.maps.LocationSource;

public interface IMapUtil {
    void setLatlng(String[] lats,String[] lngs);
    void moveCamera();
    void addMarker();
    void addPolyline();
    void onResume();
    void onDestroy();
    void onCreate(Bundle savedInstanceState);
    void setUpMap();
    void setLocation(Location location);
}
