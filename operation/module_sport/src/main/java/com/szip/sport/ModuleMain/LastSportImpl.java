package com.szip.sport.ModuleMain;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.szip.blewatch.base.Util.LocationUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportData;

import java.util.List;

public class LastSportImpl implements ILastSportPresenter{

    private ILastSportView iLastSportView;
    private LocationManager locationManager;
    private Context mContext;

    public LastSportImpl(ILastSportView iLastSportView, Context mContext) {
        this.iLastSportView = iLastSportView;
        this.mContext = mContext;
    }

    @Override
    public void initLastSport() {
        List<SportData> sportDataList = LoadDataUtil.newInstance().getLastSportDataList();
        if (iLastSportView!=null){
            iLastSportView.updateLastSport(sportDataList);
        }
    }

    @Override
    public void initLocation(LocationManager locationManager) {
        this.locationManager = locationManager;
        Location state = LocationUtil.getInstance().getLocation(locationManager,true,myListener,locationListener,mContext);
        LogUtil.getInstance().logd("data******","state = "+state);
        if (state == null){
            float acc = 0;
            if (iLastSportView!=null)
                iLastSportView.updateLocation(acc);
        }

    }

    private GpsStatus.Listener myListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int i) {

        }
    };

    //监听GPS位置改变后得到新的经纬度
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location != null) {
                //获取国家，省份，城市的名称
                Log.e("LOCATION******", location.toString());
                location = LocationUtil.getInstance().getGaoLocation(location,mContext);
                float acc = location.getAccuracy();
                if (iLastSportView!=null)
                    iLastSportView.updateLocation(acc);
                locationManager.removeUpdates(locationListener);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
