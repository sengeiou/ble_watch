package com.szip.blewatch.Activity.main;

import android.location.LocationManager;

import androidx.fragment.app.FragmentTabHost;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public interface IMainPrisenter {

    void initWeather(LocationManager locationManager);
    //检查蓝牙状态
    void checkBluetoochState();
    //检查GPS状态
    void checkGPSState();
    //初始化工具栏
    void initPager(ViewPager2 pager, TabLayout tabLayout);
}
