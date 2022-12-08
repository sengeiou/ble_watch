package com.szip.blewatch.base.Broadcast;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.szip.blewatch.base.Const.BroadcastConst;

public class ToServiceBroadcast extends BroadcastReceiver {

    private MyHandle handle;


    public void registerReceive(MyHandle handle,Context context){
        this.handle = handle;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.SEND_BLE_DATA);
        intentFilter.addAction(BroadcastConst.SEND_BLE_FILE);
        intentFilter.addAction(BroadcastConst.UPDATE_DIAL_STATE);
        intentFilter.addAction(BroadcastConst.SEND_BLE_BACKGROUND);
        intentFilter.addAction(BroadcastConst.UPDATE_BACKGROUND_STATE);
        intentFilter.addAction(BroadcastConst.DOWNLOAD_FILE);
        intentFilter.addAction(BroadcastConst.START_SEARCH_DEVICE);
        intentFilter.addAction(BroadcastConst.START_CONNECT_DEVICE);
        intentFilter.addAction(BroadcastConst.CHECK_BLE_STATE);
        intentFilter.addAction(BroadcastConst.SEND_JL_DIAL);
        intentFilter.addAction(BroadcastConst.START_JL_OTA);
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        context.registerReceiver(this,intentFilter);
    }

    public void unregister(Context context){
        context.unregisterReceiver(this);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (handle!=null)
            handle.onReceive(intent);
    }

}
