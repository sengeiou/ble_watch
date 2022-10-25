package com.szip.blewatch.base.Util.ble.jlBleInterface;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface IBleConnectState {
    void onConnectState(BluetoothDevice device,int status);
}
