package com.szip.sport.Activity.sportResult;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.sport.R;

import java.util.ArrayList;
import java.util.List;

public class MapUtilGaodeImp implements IMapUtil {
    private MapView mapView;
    private AMap aMap;
    private double [] option;
    private List<LatLng> latLngs = new ArrayList<LatLng>();
    private LocationSource.OnLocationChangedListener listener;
    private LocationSource locationSource;

    public MapUtilGaodeImp(MapView mapView) {
        this.mapView = mapView;
        aMap = mapView.getMap();
        locationSource = new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                listener = onLocationChangedListener;
            }

            @Override
            public void deactivate() {

            }
        };
    }


    @Override
    public void setLatlng(String[] lats, String[] lngs) {
        option = MathUtil.newInstance().getMapOption(lats,lngs);
        for (int a = 1;a<lats.length;a++){
            latLngs.add(new LatLng((Integer.valueOf(lats[0])+Integer.valueOf(lats[a]))/1000000.0,
                    (Integer.valueOf(lngs[0])+Integer.valueOf(lngs[a]))/1000000.0));
        }
    }

    @Override
    public void moveCamera() {
        LatLng centerBJPoint= new LatLng(option[0],
                option[1]);
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(centerBJPoint,
                (float) option[2], 0, 0)));
    }

    @Override
    public void addMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLngs.get(0));
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_start));
        aMap.addMarker(markerOptions);
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(latLngs.get(latLngs.size()-1));
        markerOptions1.icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_destination));
        aMap.addMarker(markerOptions1);
    }

    @Override
    public void addPolyline() {
        aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).
                width(30).
                useGradient(true).
                color(Color.parseColor("#bbf246")));

        aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).
                width(12).
                useGradient(true).
                color(Color.parseColor("#000000")));
    }

    @Override
    public void onResume() {
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
//        mapView.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.mipmap.sport_icon_direction));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);// 设置圆形的填充颜色
        myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(locationSource);// 设置定位监听
        aMap.getUiSettings().setZoomControlsEnabled(false);// 隐藏缩放按钮
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.moveCamera(CameraUpdateFactory.zoomTo(19f));
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    @Override
    public void setLocation(Location location) {
        if (listener!=null)
            listener.onLocationChanged(location);
    }

}
