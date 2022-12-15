package com.szip.blewatch.base.Util.ble.jlBleInterface;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_rcsp.interfaces.watch.OnUpdateResourceCallback;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.SendFileConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.FileUtil;
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

    public void init() {
        BluetoothOTAConfigure bluetoothOption = BluetoothOTAConfigure.createDefault();
        bluetoothOption.setPriority(BluetoothOTAConfigure.PREFER_BLE) //请按照项目需要选择
                .setUseAuthDevice(false) //具体根据固件的配置选择
                .setBleIntervalMs(500) //默认是500毫秒
                .setMtu(500)
                .setTimeoutMs(3000) //超时时间
                .setUseReconnect(false)
                .setNeedChangeMtu(false); //不需要调整MTU，建议客户连接时调整好BLE的MTU
        bluetoothOption.setFirmwareFilePath(mContext.getExternalFilesDir(null).getPath()+ "/update.ufw"); //设置本地存储OTA文件的路径
        configure(bluetoothOption); //设置OTA参数

        registerBluetoothCallback(btEventCallback);
    }

    private BtEventCallback btEventCallback = new BtEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            //必须等待库回调连接成功才可以开始OTA库操作

            if (status == StateCode.CONNECTION_OK) {
                Log.d("data******","OTA连接成功");
            }else {
                Log.d("data******","OTA连接断开");
                mTargetDevice = null;
            }
        }
    };

    //BLE收到的notify转发给OTA SDK
    public void notifyData(byte[] datas) {
        Log.d("data******","get data = "+DateUtil.byteToHexString(datas));
        onReceiveDeviceData(mTargetDevice, datas);
    }

    public void startResource(){
        //1.更新资源
        WatchManager.getInstance().updateWatchResource(mContext.getExternalFilesDir(null).getPath() + "/ota_file.zip",
                new OnUpdateResourceCallback() {
                    @Override
                    public void onStart(String s, int i) {
                        Log.d("data******","开始传输资源 = "+s);
                        Intent intent = new Intent(BroadcastConst.UPDATE_RESOURCE_STATE);
                        intent.putExtra("command", SendFileConst.START_SEND);
                        context.sendBroadcast(intent);
                    }

                    @Override
                    public void onProgress(int i, String s, float v) {
                        Log.d("data******","开始传输资源 onProgress= "+v);
                        Intent intent = new Intent(BroadcastConst.UPDATE_RESOURCE_STATE);
                        intent.putExtra("command", SendFileConst.PROGRESS);
                        intent.putExtra("progress", v);
                        context.sendBroadcast(intent);
                    }

                    @Override
                    public void onStop(String s) {
                        Log.d("data******","开始传输资源 onStop path= "+s);
                        Intent intent = new Intent(BroadcastConst.UPDATE_RESOURCE_STATE);
                        intent.putExtra("command", SendFileConst.FINISH);
                        context.sendBroadcast(intent);
                        if (s == null) {
                            return;
                        } else {
                            //资源传输完成，开始发送OTA文件
                            getBluetoothOption().setFirmwareFilePath(s);
                            startOTA(new IUpgradeCallback() {
                                @Override
                                public void onStartOTA() {
                                    //回调开始OTA
                                    Log.d("data******","开始OTA");
                                    Intent intent = new Intent(BroadcastConst.UPDATE_OTA_STATE);
                                    intent.putExtra("command", SendFileConst.START_SEND);
                                    context.sendBroadcast(intent);
                                }

                                @Override
                                public void onNeedReconnect(String s, boolean b) {
                                    Log.d("data******","onNeedReconnect = "+s);
                                }


                                @Override
                                public void onProgress(int type, float progress) {
                                    //回调OTA进度
                                    //type : 0 --- 下载uboot  1 --- 升级固件
                                    Log.d("data******","onProgress = "+progress);
                                    Intent intent = new Intent(BroadcastConst.UPDATE_OTA_STATE);
                                    intent.putExtra("command", SendFileConst.PROGRESS);
                                    intent.putExtra("progress", progress);
                                    context.sendBroadcast(intent);
                                }

                                @Override
                                public void onStopOTA() {
                                    //回调OTA升级完成
                                    Intent intent = new Intent(BroadcastConst.UPDATE_OTA_STATE);
                                    intent.putExtra("command", SendFileConst.FINISH);
                                    context.sendBroadcast(intent);
                                    Log.d("data******","onStopOTA");
                                    FileUtil.getInstance().deleteFile(mContext.getExternalFilesDir(null).getPath() + "/ota_file.zip");
                                    FileUtil.getInstance().deleteFile(s);
                                }

                                @Override
                                public void onCancelOTA() {
                                    //回调OTA升级被取消
                                    Log.d("data******","onCancelOTA");
                                }

                                @Override
                                public void onError(BaseError error) {
                                    //回调OTA升级发生的错误事件
                                    Log.d("data******","onError = "+error);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });
    }

    //返回当前已经连接的device
    @Override
    public BluetoothDevice getConnectedDevice() {
        return mTargetDevice;
    }

    //返回当前GATT
    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {

        BluetoothGatt gatt = mTargetDevice.connectGatt(context, true, new BluetoothGattCallback() {
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }
        });
        Log.d("data******","gatt = "+gatt+" ; = "+gatt.getDevice().getAddress());
        return gatt;
    }

    //设置BLE的状态到OTA SDK
    public void setConnectState(int state,BluetoothDevice device){
        if (device!=null)
            this.mTargetDevice = device;
        onBtDeviceConnection(mTargetDevice, state);
    }
    //执行回连设备的逻辑
    @Override
    public void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        Log.d("data******","ota升级成功，开始回连设备 = "+bluetoothDevice.getAddress());

        BluetoothUtilImpl.getInstance().connect(bluetoothDevice.getAddress(),null);
    }

    //执行断开设备连接的逻辑
    @Override
    public void disconnectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        BluetoothUtilImpl.getInstance().disconnect();
    }

    //把OTA SDK返回的byte[]通过BLE发送给设备（需要分包）
    @Override
    public boolean sendDataToDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {
        Log.d("data******","send data = "+bytes.length+" ;"+DateUtil.byteToHexString(bytes));
        //数据分包
        ArrayList<byte[]> cutData = DateUtil.cutBytes(bytes);
        for (byte[] sendData:cutData){
            BluetoothUtilImpl.getInstance().sendCommandToSdk(sendData);
        }
        return true;
    }
}
