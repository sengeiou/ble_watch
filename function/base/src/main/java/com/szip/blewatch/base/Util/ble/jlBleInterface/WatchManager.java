package com.szip.blewatch.base.Util.ble.jlBleInterface;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RcspAuth;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.contacts.DeviceContacts;
import com.jieli.jl_rcsp.task.contacts.ReadContactsTask;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.ble.BluetoothUtilCoreImpl;
import com.szip.blewatch.base.Util.ble.BluetoothUtilImpl;
import com.szip.blewatch.base.Util.ble.ClientManager;
import com.szip.blewatch.base.Util.ble.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchManager extends WatchOpImpl {

    private BluetoothDevice mTargetDevice;

    private static WatchManager instance;

    private Context context;

    private RcspAuth mRcspAuth;

    private final Map<String, Boolean> mAuthDeviceMap = new HashMap<>();

    private ManagerInitCallback managerInitCallback;

    public static WatchManager getInstance() {
        if (null == instance) {
            synchronized (WatchManager.class) {
                if (null == instance) {
                    instance = new WatchManager(FUNC_WATCH);
                }
            }
        }
        return instance;
    }

    //func FUNC_WATCH:全功能  FUNC_RCSP：仅仅使用rcsp协议  FUNC_FILE_BROWSE：使用rcsp协议和目录浏览功能
    private WatchManager(int func) {
        super(func);
//        Log.d("szip******","初始化库");
//        registerOnWatchCallback(mOnWatchCallback);
    }

    public void init(BluetoothDevice device,Context context,ManagerInitCallback managerInitCallback) {
        this.managerInitCallback = managerInitCallback;
        this.context = context;
        this.mTargetDevice = device;
        mRcspAuth = new RcspAuth(this::sendDataToDevice, mRcspAuthListener);
        if (!isAuthDevice(device)) { //设备未认证
            Log.d("data******","device Auth = "+device.getName());
            mRcspAuth.stopAuth(device);
            boolean ret = mRcspAuth.startAuth(device);
            return;
        }else {
            Log.d("data******","device is Auth = "+device.getName());
            registerOnWatchCallback(mOnWatchCallback);
            notifyBtDeviceConnection(device, StateCode.CONNECTION_OK);
        }
    }


    //设置sdk使用到的device
    public void notifyData(byte[] datas) {
        Log.d("data******","收到WATCH通知");
        if (isAuthDevice(mTargetDevice)){
            notifyReceiveDeviceData(mTargetDevice, datas);
        } else {
            mRcspAuth.handleAuthData(mTargetDevice, datas);
        }
    }



    //蓝牙认证回调
    private final RcspAuth.OnRcspAuthListener mRcspAuthListener = new RcspAuth.OnRcspAuthListener() {
        @Override
        public void onInitResult(boolean b) {

        }

        @Override
        public void onAuthSuccess(BluetoothDevice bluetoothDevice) {
            Log.d("data******","onAuthSuccess device = "+bluetoothDevice.getName());
            setDevAuth(bluetoothDevice, true);
            registerOnWatchCallback(mOnWatchCallback);
            notifyBtDeviceConnection(bluetoothDevice, StateCode.CONNECTION_OK);

        }

        @Override
        public void onAuthFailed(BluetoothDevice bluetoothDevice, int i, String s) {
            Log.d("data******","onAuthFailed device = "+s);
            setDevAuth(bluetoothDevice, false);
            disconnect();
            managerInitCallback.initSuccess(false);
        }
    };

    //初始化sdk回调
    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {

        @Override
        public void onRcspInit(BluetoothDevice device, boolean isInit) {
            Log.d("data******","onRcspInit = "+isInit);
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            Log.d("data******","onRcspInit = ");
        }

        @Override
        public void onWatchSystemInit(int code) {
            Log.d("data******","初始化成功 = "+code);
            if (code == 0){
                managerInitCallback.initSuccess(true);
            }else {
                managerInitCallback.initSuccess(false);
            }

        }
    };

    public void disconnect(){

    }

    private void setDevAuth(BluetoothDevice device, boolean b) {
        if (null == device) return;
        mAuthDeviceMap.put(device.getAddress(), b);
    }

    public boolean isAuthDevice(BluetoothDevice device) {
        return device != null && isDevAuth(device.getAddress());
    }

    private boolean isDevAuth(String address) {
        Boolean b = mAuthDeviceMap.get(address);
        return b != null && b;
    }

    /**
     * 获取当前连接的设备，sdk的操作都是基于该设备
     * @return 目标设备
     */
    @Override
    public BluetoothDevice getConnectedDevice() {
        //TODO: 客户重写实现功能
        return mTargetDevice;
    }

    /**
     * SDK通知外部需要发送数据
     * @param device 蓝牙设备对象
     * @param data   数据包 byte数组
     * @return false：发送失败  true:发送成功
     */
    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        //TODO: 客户重写实现功能
        Log.d("data******","WATCH发送数据给sdk = "+ DateUtil.byteToHexString(data)+" ; data = "+data.length);
        ArrayList<byte[]> cutData = DateUtil.cutBytes(data);
        for (byte[] sendData:cutData){
            Log.d("data******","WATCH切片的数据 = "+ DateUtil.byteToHexString(sendData)+" ; data = "+sendData.length);
            BluetoothUtilImpl.getInstance().sendCommandToSdk(sendData);
        }
        return true;
    }

    public void destroy(){
        Log.d("data******","jk watch destroy");
        notifyBtDeviceConnection(mTargetDevice, StateCode.CONNECTION_DISCONNECT);
        unregisterOnWatchCallback(mOnWatchCallback);
        if (mRcspAuth!=null){
            mRcspAuth.removeListener(mRcspAuthListener);
            mRcspAuth.destroy();
            mAuthDeviceMap.clear();
        }
    }


}
