package com.szip.blewatch.Activity.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.szip.blewatch.R;
import com.szip.blewatch.View.HostTabView;

import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.LocationUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.Weather;
import com.szip.healthy.HttpModel.WeatherBean;
import com.szip.healthy.ModuleMain.HealthyFragment;
import com.szip.healthy.Utils.HttpMessageUtil;
import com.szip.sport.ModuleMain.SportFragment;
import com.szip.user.ModuleMain.MineFragment;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;


public class MainPresenterImpl implements IMainPrisenter{

    private Context context;
    private LocationManager locationManager;


    public MainPresenterImpl( Context context) {
        this.context = context;
    }

    @Override
    public void checkBluetoochState() {
        //判断蓝牙状态
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueadapter.isEnabled()) {
            Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(bleIntent);
        }else {
            checkGPSState();
        }
    }


    private boolean isInstalled(@NonNull String packageName, Context context) {
        if ("".equals(packageName) || packageName.length() <= 0) {
            return false;
        }

        PackageInfo packageInfo;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }

        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void checkGPSState() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(gps || network)) {
            MyAlerDialog.getSingle().showAlerDialog(context.getString(R.string.tip), context.getString(R.string.checkGPS),
                    context.getString(R.string.confirm), context.getString(R.string.cancel),
                    false, new MyAlerDialog.AlerDialogOnclickListener() {
                        @Override
                        public void onDialogTouch(boolean flag) {
                            if (flag){
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        }
                    },context);
        }
    }

    @Override
    public void initPager(ViewPager2 pager, TabLayout tabLayout) {
        pager.setUserInputEnabled(false);
        String[] tabText = new String[]{context.getString(R.string.home),context.getString(R.string.sport),context.getString(R.string.mine)};
        int[] tabBg = new int[]{R.drawable.bg_tab_state,R.drawable.bg_tab_sport,R.drawable.bg_tab_mine};

        new TabLayoutMediator(tabLayout, pager, true, (tab, position) -> {
            tab.setCustomView(R.layout.tab_host_layout);
            TextView textView = tab.getCustomView().findViewById(R.id.tabTv);
            ImageView imageView = tab.getCustomView().findViewById(R.id.tabIv);
            textView.setText(tabText[position]);
            imageView.setBackground(context.getResources().getDrawable(tabBg[position]));
        }).attach();
    }

    @Override
    public void initWeather(LocationManager locationManager) {
        this.locationManager = locationManager;
        LocationUtil.getInstance().getLocation(locationManager,true,myListener,locationListener,context);
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
                HttpMessageUtil.newInstance().getWeather(location.getLatitude()+"", location.getLongitude()+"",
                        new GenericsCallback<WeatherBean>(new JsonGenericsSerializator()) {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.d("DATA******","error = "+e.getMessage());
                            }

                            @Override
                            public void onResponse(final WeatherBean response, int id) {
                                if (response.getCode()==200){
                                    Gson gson=new Gson();
                                    Weather weather = new Weather();
                                    weather.city = response.getData().getLocation().getCity();
                                    weather.elevation = response.getData().getLocation().getElevation();
                                    weather.weatherList = gson.toJson(response.getData().getForecasts());
                                    weather.id = MathUtil.newInstance().getUserId(context);
                                    SaveDataUtil.newInstance().saveWeatherData(weather);
                                    MathUtil.newInstance().saveIntData(context,"weatherTime",
                                            Calendar.getInstance().getTimeInMillis());
                                    Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                                    intent.putExtra("command","setWeather");
                                    context.sendBroadcast(intent);
                                }
                            }
                        });
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
