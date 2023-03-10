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
import com.szip.blewatch.base.Util.ble.jlBleInterface.OTAManager;
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
        OTAManager.getInstance(this).init();
        initLog();
//        initBle();
    }

    private void initLog() {
        JL_Log.setTagPrefix("ota_log");
        JL_Log.configureLog(this, true, true);
        com.jieli.jl_bt_ota.util.JL_Log.setLog(true);
        com.jieli.jl_bt_ota.util.JL_Log.setLogOutput(JL_Log::addLogOutput);
    }

    private void initBle() {
        Ble.options()
                .setLogBleEnable(true)//????????????????????????????????????
                .setThrowBleException(true)//??????????????????????????????
                .setLogTAG("AndroidBLE")//??????????????????????????????TAG
                .setAutoConnect(true)//????????????????????????
                .setIgnoreRepeat(false)//????????????????????????????????????(?????????????????????????????????)
                .setConnectFailedRetryCount(3)//?????????????????????????????????????????????,??????????????????
                .setConnectTimeout(10 * 1000)//????????????????????????
                .setScanPeriod(6 * 1000)//??????????????????
                .setMaxConnectNum(1)//??????????????????
                .setFactory(new BleFactory<BleRssiDevice>() {//???????????????BleDevice???????????????
                    @Override
                    public BleRssiDevice create(String address, String name) {
                        return new BleRssiDevice(address, name);//?????????BleDevice?????????
                    }
                })
//                .setBleWrapperCallback(new MyBleWrapperCallback())
                .create(this, new Ble.InitCallback() {
                    @Override
                    public void success() {
                        BleLog.e("MainApplication", "???????????????");
                    }

                    @Override
                    public void failed(int failedCode) {
                        BleLog.e("MainApplication", "??????????????????" + failedCode);
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
