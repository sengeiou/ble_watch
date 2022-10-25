package com.szip.user.Activity.search;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.szip.blewatch.base.Model.BleRssiDevice;
import com.szip.blewatch.base.Util.ble.jlBleInterface.IBleScanCallback;
import com.szip.blewatch.base.Util.ble.JLBluetoothClientManager;

import java.util.ArrayList;

import cn.com.heaton.blelibrary.ble.Ble;

@SuppressLint("MissingPermission")
public class SearchPresenter {
    private Context context;
    private ArrayList<String> mDevices = new ArrayList<>();
    private String deviceName;
    private IUpdateSearch iUpdateSearch;
    private Ble<BleRssiDevice> ble = Ble.getInstance();

    public SearchPresenter(Context context,IUpdateSearch iUpdateSearch) {
        this.context = context;
        this.iUpdateSearch = iUpdateSearch;
    }

    public void startSearch(String deviceName){
        this.deviceName = deviceName;
        searchDevice();
    }


    public void stopSearch(){
        iUpdateSearch = null;
        JLBluetoothClientManager.getInstance().setiBleScanCallback(null);
        JLBluetoothClientManager.getInstance().startBLEScan(false);
    }

    private  void searchDevice(){
        JLBluetoothClientManager.getInstance().setiBleScanCallback(iBleScanCallback);
        JLBluetoothClientManager.getInstance().startBLEScan(true);
    }


    private IBleScanCallback iBleScanCallback = new IBleScanCallback() {
        @Override
        public void onStart() {
            mDevices = new ArrayList<>();
        }


        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice) {
            if (mDevices==null)
                return;
            if (bluetoothDevice.getName()!=null&&!mDevices.contains(bluetoothDevice.getAddress())&&deviceName.equals(bluetoothDevice.getName())) {
                mDevices.add(bluetoothDevice.getAddress());
            }
        }

        @Override
        public void onStop() {
            if (iUpdateSearch!=null){
                iUpdateSearch.searchStop(mDevices);
            }
        }
    };

    //搜索列表
//    private BleScanCallback<BleRssiDevice> bleScanCallback = new BleScanCallback<BleRssiDevice>() {
//        @Override
//        public void onStart() {
//            super.onStart();
//            mDevices = new ArrayList<>();
//
//        }
//
//        @Override
//        public void onLeScan(BleRssiDevice device, int rssi, byte[] scanRecord) {
//            if (mDevices==null)
//                return;
//            if (device.getBleName()!=null&&!mDevices.contains(device.getBleAddress())&&deviceName.equals(device.getBleName())) {
//                mDevices.add(device.getBleAddress());
//            }
//        }
//
//        @Override
//        public void onStop() {
//            super.onStop();
//            if (iUpdateSearch!=null){
//                iUpdateSearch.searchStop(mDevices);
//            }
//        }
//    };
//    private final SearchResponse mSearchResponse = new SearchResponse() {
//        @Override
//        public void onSearchStarted() {
//            mDevices = new ArrayList<>();
//            subTime = Calendar.getInstance().getTimeInMillis();
//        }
//
//        @Override
//        public void onDeviceFounded(SearchResult device) {
//            if (!mDevices.contains(device.getAddress())&&device.getName()!=null&&deviceName.equals(device.getName())) {
//                mDevices.add(device.getAddress());
//            }
//        }
//
//        @Override
//        public void onSearchStopped() {
//            if (Calendar.getInstance().getTimeInMillis()-subTime<4500){
//                searchDevice();
//            }else if (iUpdateSearch!=null){
//                iUpdateSearch.searchStop(mDevices);
//            }
//
//        }
//
//        @Override
//        public void onSearchCanceled() {
//
//        }
//    };
}
