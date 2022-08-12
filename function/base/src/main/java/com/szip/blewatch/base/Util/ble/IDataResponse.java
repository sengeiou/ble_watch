package com.szip.blewatch.base.Util.ble;



import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.blewatch.base.db.dbModel.ScheduleData;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.Model.BleStepModel;
import com.szip.blewatch.base.db.dbModel.UserModel;

import java.util.ArrayList;

/**
 * Created by Hqs on 2018/1/12
 * 设备回传上来的信息
 */
public interface IDataResponse {

    /**
     * 接收完成计步数据
     */
    void onSaveStepDatas(ArrayList<BleStepModel> datas);

    /**
     * 接收完成总计步数据
     */
    void onSaveDayStepDatas(ArrayList<StepData> datas);

    /**
     * 接收完成心率数据
     */
    void onSaveHeartDatas(ArrayList<HeartData> datas);

    /**
     * 接收完成体温数据
     */
    void onSaveTempDatas(ArrayList<AnimalHeatData> datas);

    /**
     * 接收完成体温数据
     */
    void onSaveBpDatas(ArrayList<BloodPressureData> datas);

    /**
     * 接收完成睡眠数据
     */
    void onSaveSleepDatas(ArrayList<SleepData> datas);

    /**
     * 接收完成跑步数据
     */
    void onSaveRunDatas(ArrayList<SportData> datas);

    /**
     * 接收完成血氧数据
     */
    void onSaveBloodOxygenDatas(ArrayList<BloodOxygenData> datas);

    /**
     * 解析完业务数据索引
     */
    void onGetDataIndex(String deviceNum, ArrayList<Integer> dataIndex);

    /**
     * 远程拍照
     * */
    void onCamera(int flag);

    /**
     * 寻找手机
     * */
    void findPhone(int flag);

    /**
     * 更新个人信息
     * */
    void updateUserInfo(UserModel userModel);

    void onSaveScheduleData(ArrayList<ScheduleData> scheduleDataArrayList);
    void onScheduleRefresh(boolean refresh);

    void updateOtaProgress(int type, int state, int address);
    void onMusicControl(int cmd, int voiceValue);
    void endCall();

    void onScheduleCallback(int type,int state);

    void sendDialFinish();
    void sendDialError();
    void sendDialStart(int address);
    void sendDialContinue(int page);
    void sendDialProgress();

    void sendBackgroundFinish();
    void sendBackgroundError();
    void sendBackgroundStart();
    void startSendFile();
    void sendBackgroundProgress();

    void pairBluetooth(String mac);
}
