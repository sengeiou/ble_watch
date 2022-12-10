package com.szip.blewatch.base;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.location.AMapLocationClient;
import com.jieli.jl_rcsp.util.JL_Log;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.szip.blewatch.base.Model.BleRssiDevice;
import com.szip.blewatch.base.Util.FileUtil;
import com.szip.blewatch.base.Util.MusicUtil;
import com.szip.blewatch.base.Util.ble.ClientManager;
import com.szip.blewatch.base.Util.ble.jlBleInterface.FunctionManager;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.View.NotificationView;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.AutoMeasureData;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.model.BleFactory;

/**
 * @author ddnosh
 * @website http://blog.csdn.net/ddnosh
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this,true);
        initARouter();
        FlowManager.init(this);
        HttpClientUtils.newInstance().init(this);
        FileUtil.getInstance().initFile(this);
        LogUtil.getInstance().init(this);
        NotificationView.getInstance().init(this);
        MusicUtil.getSingle().init(getApplicationContext());
        SaveDataUtil.newInstance().init(this);
        initAuto();
        ClientManager.getInstance().init(this);
        FunctionManager.getInstance().init(this);


//        initBle();
    }

    private void initBle() {
        Ble.options()
                .setLogBleEnable(true)//设置是否输出打印蓝牙日志
                .setThrowBleException(true)//设置是否抛出蓝牙异常
                .setLogTAG("AndroidBLE")//设置全局蓝牙操作日志TAG
                .setAutoConnect(true)//设置是否自动连接
                .setIgnoreRepeat(false)//设置是否过滤扫描到的设备(已扫描到的不会再次扫描)
                .setConnectFailedRetryCount(3)//连接异常时（如蓝牙协议栈错误）,重新连接次数
                .setConnectTimeout(10 * 1000)//设置连接超时时长
                .setScanPeriod(6 * 1000)//设置扫描时长
                .setMaxConnectNum(1)//最大连接数量
                .setFactory(new BleFactory<BleRssiDevice>() {//实现自定义BleDevice时必须设置
                    @Override
                    public BleRssiDevice create(String address, String name) {
                        return new BleRssiDevice(address, name);//自定义BleDevice的子类
                    }
                })
//                .setBleWrapperCallback(new MyBleWrapperCallback())
                .create(this, new Ble.InitCallback() {
                    @Override
                    public void success() {
                        BleLog.e("MainApplication", "初始化成功");
                    }

                    @Override
                    public void failed(int failedCode) {
                        BleLog.e("MainApplication", "初始化失败：" + failedCode);
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
    }


    private void initAuto() {
        if (null==LoadDataUtil.newInstance().getAutoMeasureData()){
            AutoMeasureData data = new AutoMeasureData(true);
            data.save();
        }
    }

}
