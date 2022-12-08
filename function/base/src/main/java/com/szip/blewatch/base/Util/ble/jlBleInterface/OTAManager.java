package com.szip.blewatch.base.Util.ble.jlBleInterface;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_rcsp.interfaces.watch.OnUpdateResourceCallback;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.ble.BluetoothUtilImpl;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;

import java.util.ArrayList;

public class OTAManager extends BluetoothOTAManager {
    private BluetoothDevice mTargetDevice;

    private static OTAManager instance;

    private static Context mContext;


    public static OTAManager getInstance(Context context) {
        if (null == instance) {
            synchronized (OTAManager.class) {
                if (null == instance) {
                    mContext = context;
                    instance = new OTAManager(context);
                }
            }
        }
        return instance;
    }


    public OTAManager(Context context) {
        super(context);
    }

    public void init(BluetoothDevice device) {
        this.mTargetDevice = device;
        BluetoothOTAConfigure bluetoothOption = BluetoothOTAConfigure.createDefault();
        bluetoothOption.setPriority(BluetoothOTAConfigure.PREFER_BLE) //请按照项目需要选择
                .setUseAuthDevice(false) //具体根据固件的配置选择
                .setBleIntervalMs(500) //默认是500毫秒
                .setTimeoutMs(3000) //超时时间
                .setUseReconnect(false)
                .setMtu(514) //BLE底层通讯MTU值，会影响BLE传输数据的速率。建议用512。该MTU值会使OTA库在BLE连接时改变MTU，所以用户SDK需要对此处理。
                .setNeedChangeMtu(false); //不需要调整MTU，建议客户连接时调整好BLE的MTU
        bluetoothOption.setFirmwareFilePath(mContext.getExternalFilesDir(null).getPath()+ "/update.ufw"); //设置本地存储OTA文件的路径
//        bluetoothOption.setFirmwareFileData(firmwareData);//设置本地存储OTA文件的数据, 与setFirmwareFilePath，二者选其一
        configure(bluetoothOption); //设置OTA参数
        registerBluetoothCallback(new BtEventCallback() {
            @Override
            public void onConnection(BluetoothDevice device, int status) {
                //必须等待库回调连接成功才可以开始OTA库操作
                Log.d("jl******","OTA连接 = "+status);
                if (status == StateCode.CONNECTION_OK) {
                    Log.d("jl******","OTA连接成功");
                    //1.更新资源
                    WatchManager.getInstance().updateWatchResource(mContext.getExternalFilesDir(null).getPath() + "/upgrade_fake_1121.zip",
                            new OnUpdateResourceCallback() {
                                @Override
                                public void onStart(String s, int i) {
                                    Log.d("jl******","开始传输资源 = "+s);
                                }

                                @Override
                                public void onProgress(int i, String s, float v) {
                                    Log.d("jl******","开始传输资源 onProgress= "+i);
                                }

                                @Override
                                public void onStop(String s) {
                                    Log.d("jl******","开始传输资源 onStop path= "+s);
                                    if (s == null) {
                                       return;
                                    } else {
                                        getBluetoothOption().setFirmwareFilePath(s);
                                        startOTA(new IUpgradeCallback() {
                                            @Override
                                            public void onStartOTA() {
                                                //回调开始OTA
                                                Log.d("jl******","开始OTA");
                                            }

                                            @Override
                                            public void onNeedReconnect(String s, boolean b) {
                                                Log.d("jl******","onNeedReconnect = "+s);
                                            }


                                            @Override
                                            public void onProgress(int type, float progress) {
                                                //回调OTA进度
                                                //type : 0 --- 下载uboot  1 --- 升级固件
                                                Log.d("jl******","onProgress = "+progress);
                                            }

                                            @Override
                                            public void onStopOTA() {
                                                //回调OTA升级完成
                                                Log.d("jl******","onStopOTA");
                                            }

                                            @Override
                                            public void onCancelOTA() {
                                                //回调OTA升级被取消
                                                Log.d("jl******","onCancelOTA");
                                            }

                                            @Override
                                            public void onError(BaseError error) {
                                                //回调OTA升级发生的错误事件
                                                Log.d("jl******","onError = "+error);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(int i, String s) {

                                }
                            });

                }
            }
        });
    }

    //设置sdk使用到的device
    public void notifyData(byte[] datas) {
        Log.d("jl******","收到OTA通知");
        onReceiveDeviceData(mTargetDevice, datas);
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return mTargetDevice;
    }

    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {

        BluetoothGatt gatt = mTargetDevice.connectGatt(context, true, new BluetoothGattCallback() {
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }
        });
        Log.d("jl******","gatt = "+gatt+" ; = "+gatt.getDevice().getAddress());
        return gatt;
    }

    public void setConnectState(int state){
        onBtDeviceConnection(mTargetDevice, state);
    }



    @Override
    public void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        Log.d("jl******","重新连接 = "+bluetoothDevice.getAddress());
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(context));
        if (userModel==null)
            return;
        Log.d("jl******","重新连接原mac = "+userModel.deviceCode);
        userModel.deviceCode = bluetoothDevice.getAddress();
        userModel.update();
        Intent intent = new Intent(BroadcastConst.BIND_SERVICE);
        context.sendBroadcast(intent);
    }
//74:3E:B5:20:4B:D3
    @Override
    public void disconnectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        BluetoothUtilImpl.getInstance().disconnect();
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {
        //TODO: 客户重写实现功能
        ArrayList<byte[]> cutData = DateUtil.cutBytes(bytes);
        for (byte[] sendData:cutData){
            BluetoothUtilImpl.getInstance().sendCommandToSdk(sendData);
        }
        return true;
    }
}
