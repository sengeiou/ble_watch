package com.szip.user.Activity.search;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Model.BleRssiDevice;
import com.szip.blewatch.base.Util.ble.ClientManager;

import java.util.ArrayList;
import java.util.Calendar;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;

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
        ClientManager.getClient().stopSearch();
    }

    private void searchDevice(){
        final SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(10000, 1).build();
        ClientManager.getClient().search(request,mSearchResponse);
    }



    private long subTime = 0;

//    //搜索列表
//    private BleScanCallback<BleRssiDevice> bleScanCallback = new BleScanCallback<BleRssiDevice>() {
//        @Override
//        public void onStart() {
//            super.onStart();
//            mDevices = new ArrayList<>();
//            subTime = Calendar.getInstance().getTimeInMillis();
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
    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            mDevices = new ArrayList<>();
            subTime = Calendar.getInstance().getTimeInMillis();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            if (!mDevices.contains(device.getAddress())&&device.getName()!=null&&deviceName.equals(device.getName())) {
                mDevices.add(device.getAddress());
            }
        }

        @Override
        public void onSearchStopped() {
            if (Calendar.getInstance().getTimeInMillis()-subTime<4500){
                searchDevice();
            }else if (iUpdateSearch!=null){
                iUpdateSearch.searchStop(mDevices);
            }

        }

        @Override
        public void onSearchCanceled() {

        }
    };
}
