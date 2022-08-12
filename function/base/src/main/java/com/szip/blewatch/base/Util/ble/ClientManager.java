package com.szip.blewatch.base.Util.ble;

import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;


/**
 * Created by PettySion on 2016/8/27.
 */
public class ClientManager {

    private static BluetoothClient mClient;

    private static ClientManager clientManager;

    private static Context context;

    public static ClientManager getInstance() {
        if (clientManager == null) {
            synchronized (ClientManager.class) {
                if (clientManager == null) {
                    clientManager = new ClientManager();
                }
            }
        }
        return clientManager;
    }

    public void init(Context context){
        this.context = context;
    }

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null&&context!=null) {
                    mClient = new BluetoothClient(context);
                }
            }
        }
        return mClient;
    }
}
