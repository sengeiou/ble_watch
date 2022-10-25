package com.szip.blewatch.base.Util.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.jieli.bluetooth_connect.bean.BluetoothOption;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.impl.BluetoothBle;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback;
import com.jieli.bluetooth_connect.interfaces.listener.OnBtBleListener;
import com.jieli.bluetooth_connect.tool.BleEventCbManager;
import com.szip.blewatch.base.Util.ble.jlBleInterface.IBleNotifyAndReadData;
import com.szip.blewatch.base.Util.ble.jlBleInterface.IBleScanCallback;
import com.szip.blewatch.base.Util.ble.jlBleInterface.IBleConnectState;

import java.util.List;
import java.util.UUID;


public class JLBluetoothClientManager {

    private static BluetoothManager manager;
    private static BluetoothOption option;

    private static JLBluetoothClientManager clientManager;

    private static Context context;


    private IBleScanCallback iBleScanCallback;
    private IBleConnectState iBleConnectState;
    private IBleNotifyAndReadData iBleNotifyAndReadData;

    public void setiBleHandleData(IBleNotifyAndReadData iBleNotifyAndReadData) {
        this.iBleNotifyAndReadData = iBleNotifyAndReadData;
    }

    public void setiBleConnectState(IBleConnectState iBleConnectState) {
        this.iBleConnectState = iBleConnectState;
    }

    public void setiBleScanCallback(IBleScanCallback iBleScanCallback) {
        this.iBleScanCallback = iBleScanCallback;
    }

    public static JLBluetoothClientManager getInstance() {
        if (clientManager == null) {
            synchronized (ClientManager.class) {
                if (clientManager == null) {
                    clientManager = new JLBluetoothClientManager();
                }
            }
        }
        return clientManager;
    }


    public void startBLEScan(boolean start){
        if (start){
            manager.startBLEScan(10*1000);
        }else {
            manager.stopBLEScan();
        }
    }

    public void init(Context context){
        //1. 配置连接库参数
        this.context = context;
        option = BluetoothOption.createDefaultOption()
                .setPriority(BluetoothConstant.PROTOCOL_TYPE_BLE);  //默认通讯方式
        option.setUseMultiDevice(false); //是否支持多设备管理
        if (option.getPriority() == BluetoothConstant.PROTOCOL_TYPE_BLE) { //BLE方式
            option.setNeedChangeBleMtu(true); //是否需要调整MTU
            option.setMtu(200); //设置BLE的MTU调整值
        }
        option.setBleScanStrategy(BluetoothConstant.NONE_FILTER); //扫描策略
        option.setReconnect(true);
        option.setBleUUID(UUID.fromString(Config.char0),UUID.fromString(Config.char1),UUID.fromString(Config.char2));
        manager = new BluetoothManager(context, option);
        manager.registerBluetoothCallback(bluetoothEventCallback);
    }

    public static BluetoothManager getClient() {
        if (manager == null) {
            synchronized (ClientManager.class) {
                manager = new BluetoothManager(context, option);
            }
        }
        return manager;
    }

    public void onDestroy(){
        iBleScanCallback = null;
        iBleConnectState = null;
        iBleNotifyAndReadData = null;
    }

    private BluetoothEventCallback bluetoothEventCallback = new BluetoothEventCallback() {
        @Override
        public void onAdapterStatus(boolean bEnabled, boolean bHasBle) {
            super.onAdapterStatus(bEnabled, bHasBle);
        }

        //扫描的状态
        @Override
        public void onDiscoveryStatus(boolean bBle, boolean bStart) {
            super.onDiscoveryStatus(bBle, bStart);
            if (iBleScanCallback!=null){
                if (bStart){
                    iBleScanCallback.onStart();
                }else {
                    iBleScanCallback.onStop();
                }
            }

        }

        //扫描的结果
        @Override
        public void onDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
            super.onDiscovery(device, bleScanMessage);
            if (iBleScanCallback!=null){
                iBleScanCallback.onLeScan(device);
            }

        }

        @Override
        public void onDeviceUuidsDiscovery(BluetoothDevice device, ParcelUuid[] uuids) {
            super.onDeviceUuidsDiscovery(device, uuids);
            Log.d("ble******","onDeviceUuidsDiscovery");
        }

        @Override
        public void onBleServiceDiscovery(BluetoothDevice device, int status, List<BluetoothGattService> services) {
            super.onBleServiceDiscovery(device, status, services);
            Log.d("ble******","onBleServiceDiscovery");
        }

        @Override
        public void onBleConnection(BluetoothDevice device, int status) {
            super.onBleConnection(device, status);
            Log.d("ble******","onBleConnection");
        }

        @Override
        public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
            super.onBleDataNotification(device, serviceUuid, characteristicsUuid, data);
            Log.d("ble******","onBleDataNotification");
            if (iBleNotifyAndReadData!=null)
                iBleNotifyAndReadData.onNotify(data);
        }

        @Override
        public void onBleWriteStatus(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data, int status) {
            super.onBleWriteStatus(device, serviceUuid, characteristicsUuid, data, status);
            Log.d("ble******","onBleWriteStatus");
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            Log.d("ble******","onConnection");
            if (status == BluetoothConstant.CONNECT_STATE_CONNECTED){
                manager.getBluetoothBle().addListener(onBtBleListener);
            }else if (status == BluetoothConstant.CONNECT_STATE_DISCONNECT){
                manager.getBluetoothBle().removeListener(onBtBleListener);
            }
            if (iBleConnectState!=null)
                iBleConnectState.onConnectState(device,status);
        }
    };


    private OnBtBleListener onBtBleListener = new OnBtBleListener() {
        @Override
        public void onConnectionUpdatedCallback(BluetoothGatt bluetoothGatt, int i, int i1, int i2, int i3) {

        }

        @Override
        public void onBleConnection(BluetoothDevice bluetoothDevice, int i) {

        }

        @Override
        public void onBleDataNotify(BluetoothDevice bluetoothDevice, UUID uuid, UUID uuid1, byte[] bytes) {

        }

        @Override
        public void onBleNotificationStatus(BluetoothDevice bluetoothDevice, UUID uuid, UUID uuid1, boolean b) {

        }

        @Override
        public void onBleWriteStatus(BluetoothDevice bluetoothDevice, UUID uuid, UUID uuid1, byte[] bytes, int i) {

        }

        @Override
        public void onBleMtuChanged(BluetoothDevice bluetoothDevice, int i, int i1) {

        }

        @Override
        public void onBleBond(BluetoothDevice bluetoothDevice, int i) {

        }

        @Override
        public void onSwitchBleDevice(BluetoothDevice bluetoothDevice) {

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] data = characteristic.getValue();
            if (iBleNotifyAndReadData!=null&&data!=null&&data.length!=0)
                iBleNotifyAndReadData.onRead(data);
        }
    };

    public boolean readBleData(){
        BluetoothGattCharacteristic bluetoothGattCharacteristic =
                manager.getConnectedBluetoothGatt().getService(UUID.fromString(Config.char0)).getCharacteristic(UUID.fromString(Config.char3));
        if (bluetoothGattCharacteristic==null)
            return true;
        return manager.getConnectedBluetoothGatt().readCharacteristic(bluetoothGattCharacteristic);
    }


}
