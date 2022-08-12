package com.szip.blewatch.base.Broadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;

import java.util.Calendar;


public class UtilBroadcat extends BroadcastReceiver {
    private IntentFilter mIntentFilter;
    private Context context;
    private long time;
    public UtilBroadcat(Context context) {
        this.context = context;
    }

    public UtilBroadcat() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
//            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
//                    BluetoothAdapter.ERROR);
//            switch (state) {
//                case BluetoothAdapter.STATE_ON:
//                    LogUtil.getInstance().logd("data******","蓝牙打开");
//                    context.sendBroadcast(new Intent(BroadcastConst.START_CONNECT_DEVICE));
//                    break;
//            }
//        }else if (intent.getAction().equals(BroadcastConst.ALARM_EVENT)){
//            Log.d("data******","收到定时广播");
//        }
//        if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
//            long priTime = Calendar.getInstance().getTimeInMillis();
//            if (priTime-time>50){
//                time = priTime;
//                if (MainService.getInstance()!=null&&MainService.getInstance().getState()==3&&!MyApplication.getInstance().isMtk()){
//                    BleClient.getInstance().writeForSendVolume();
//                }
//            }
//        }
    }

    private IntentFilter getmIntentFilter() {
        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        mIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
//        mIntentFilter.addAction(BroadcastConst.ALARM_EVENT);
        return mIntentFilter;
    }

    public void onRegister() {
        context.registerReceiver(this, getmIntentFilter());
    }

    public void unRegister() {
        context.unregisterReceiver(this);
    }
}
