package com.szip.blewatch.base.Util.ble.jlBleInterface;

import android.bluetooth.BluetoothDevice;

public interface IBleScanCallback {

    void onStart();

    void onLeScan(BluetoothDevice bluetoothDevice);

    void onStop();
}
