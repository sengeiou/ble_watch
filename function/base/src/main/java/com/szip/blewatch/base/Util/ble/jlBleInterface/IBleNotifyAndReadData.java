package com.szip.blewatch.base.Util.ble.jlBleInterface;

public interface IBleNotifyAndReadData {
    void onNotify(byte[] data);
    void onRead(byte[] data);
}
