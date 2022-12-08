package com.szip.blewatch.base.Util.ble.jlBleInterface;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.maps.android.clustering.ClusterItem;
import com.jieli.jl_bt_ota.constant.BluetoothConstant;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_rcsp.impl.RcspAuth;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.contacts.DeviceContacts;
import com.jieli.jl_rcsp.task.contacts.ReadContactsTask;
import com.jieli.jl_rcsp.task.contacts.UpdateContactsTask;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.SendFileConst;
import com.szip.blewatch.base.Model.ContactModel;
import com.szip.blewatch.base.Util.ble.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionManager {

    private static FunctionManager instance;

    private Context context;

    private ArrayList<FatFile> dialList = new ArrayList<>();
    private boolean isCreateFile = false;


    public static FunctionManager getInstance() {
        if (null == instance) {
            synchronized (FunctionManager.class) {
                if (null == instance) {
                    instance = new FunctionManager();
                }
            }
        }
        return instance;
    }

    private FunctionManager(){}


    public void init(Context context){
        this.context = context;
    }

    public void readContacts(Context context){
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        ReadContactsTask task = new ReadContactsTask(watchManager,context);
        task.setListener(new SimpleTaskListener(){
            @Override
            public void onBegin() {
                //开始传输
                Log.d("data******","开始传输");
            }

            @Override
            public void onError(int code, String msg) {
                //异常
                Log.d("data******","获取失败 = "+msg);
            }

            @Override
            public void onFinish() {
                //完成
                ArrayList<ContactModel> list = sdk2contact(task.getContacts());
                Intent intent = new Intent(BroadcastConst.CONTACT_UPDATE);
                Bundle bundle = new Bundle();
                bundle.putSerializable("contact",list);
                intent.putExtra("bundle",bundle);
                context.sendBroadcast(intent);
                Log.d("data******","获取成功size = "+list);
            }
        });
        task.start();
    }

    public void setContacts(Context context, ArrayList<ContactModel> list){
        ArrayList<DeviceContacts> contacts = contact2sdk(list);
        WatchManager watchManager = WatchManager.getInstance();
        UpdateContactsTask task = new UpdateContactsTask(watchManager,context,contacts);
        task.setListener(new SimpleTaskListener(){
            @Override
            public void onBegin() {
                //开始传输
            }

            @Override
            public void onError(int code, String msg) {
                //异常
            }

            @Override
            public void onFinish() {
                //完成
                Intent intent = new Intent(BroadcastConst.CONTACT_UPDATE);
                Bundle bundle = new Bundle();
                bundle.putSerializable("contact",list);
                intent.putExtra("bundle",bundle);
                context.sendBroadcast(intent);
                Log.d("data******","更新成功 = "+list);
            }
        });
        task.start();
    }


    private ArrayList<DeviceContacts> contact2sdk(ArrayList<ContactModel> list){
        ArrayList<DeviceContacts> deviceContacts = new ArrayList<>();
        for (int i = 0;i<list.size();i++){
            deviceContacts.add(new DeviceContacts((short) i,list.get(i).getName(), list.get(i).getMobile()));
        }
        return deviceContacts;
    }

    private ArrayList<ContactModel> sdk2contact(List<DeviceContacts> list){
        ArrayList<ContactModel> deviceContacts = new ArrayList<>();
        for (int i = 0;i<list.size();i++){
            deviceContacts.add(new ContactModel(list.get(i).getMobile(),list.get(i).getName()));
        }
        return deviceContacts;
    }

    public void sendDial(String filePath){
        WatchManager watchManager = WatchManager.getInstance();

        String fileNames[] = filePath.split("/");
        String fileName = fileNames[fileNames.length-1];
        if (isDownload(fileName)){//切换表盘
           selectDial(fileName);
        }else {//插入新表盘
            Log.d("data******","准备插入表盘 = "+filePath);
            watchManager.createWatchFile(filePath, false, new OnFatFileProgressListener() {
                @Override
                public void onStart(String filePath) {
                    //回调开始
                    isCreateFile = true;
                    Log.d("data******","发送表盘开始");
                }

                @Override
                public void onProgress(float progress) {
                    //回调进度
                    Log.d("data******","发送表盘进度 = "+progress);
                    Intent intent = new Intent(BroadcastConst.UPDATE_DIAL_STATE);
                    intent.putExtra("isJL",true);
                    intent.putExtra("progress",progress);
                    intent.putExtra("command", SendFileConst.PROGRESS);
                    context.sendBroadcast(intent);
                }

                @Override
                public void onStop(int result) {
                    //回调结束
                    //result : 0 --- 成功  非0是错误码，参考FatFsErrCode
                    Log.d("data******","发送表盘结束 = "+result);
                    if (result == 0){
                        getDial();
                        selectDial(fileName);
                    }else {
                        Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
                        intent.putExtra("isJL",true);
                        intent.putExtra("command", SendFileConst.ERROR);
                        context.sendBroadcast(intent);
                    }

                }
            });
        }

    }

    //切换表盘
    private void selectDial(String fileName){
        WatchManager watchManager = WatchManager.getInstance();
        watchManager.setCurrentWatchInfo("/"+fileName, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                //成功回调 - FatFile是表盘信息
                Log.d("data******","fatFile = "+result.toString());
                Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
                intent.putExtra("command", SendFileConst.FINISH);
                context.sendBroadcast(intent);
            }

            @Override
            public void onFailed(BaseError error) {
                Log.d("data******","fatFile error= "+error.getMessage());
                //失败回调
                Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
                intent.putExtra("command", SendFileConst.ERROR);
                context.sendBroadcast(intent);
            }
        });
    }

    public void getDial(){
        WatchManager watchManager = WatchManager.getInstance();
        watchManager.listWatchList(new OnWatchOpCallback<ArrayList<FatFile>>() {
            @Override
            public void onSuccess(ArrayList<FatFile> result) {
                dialList = result;
                //成功回调
                //result 是结果，(watch或WATCH)前缀的是表盘文件，(bgp_w或BGP_W)前缀的是自定义背景文件
                //可以过滤获取所有Watch文件
                Log.d("data******","dial list = "+result.toString());
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    /**
     * 判断手表中是否已经存在该表盘
     * */
    private boolean isDownload(String fileName){
        for (FatFile file:dialList){
            if (file.getName().equalsIgnoreCase(fileName)){
                return true;
            }
        }
        return false;
    }

}
