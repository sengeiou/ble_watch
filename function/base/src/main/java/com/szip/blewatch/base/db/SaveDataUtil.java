package com.szip.blewatch.base.db;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Model.DownloadDataBean;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.http.TokenInterceptor;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData_Table;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData_Table;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData_Table;
import com.szip.blewatch.base.db.dbModel.EcgData;
import com.szip.blewatch.base.db.dbModel.EcgData_Table;
import com.szip.blewatch.base.db.dbModel.HealthyCardData;
import com.szip.blewatch.base.Model.HealthyConfig;
import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.blewatch.base.db.dbModel.HeartData_Table;
import com.szip.blewatch.base.db.dbModel.NotificationData;
import com.szip.blewatch.base.db.dbModel.NotificationData_Table;
import com.szip.blewatch.base.db.dbModel.ScheduleData;
import com.szip.blewatch.base.db.dbModel.ScheduleData_Table;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.blewatch.base.db.dbModel.SleepData_Table;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.SportData_Table;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO_Table;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.db.dbModel.StepData_Table;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.blewatch.base.db.dbModel.UserModel_Table;
import com.szip.blewatch.base.db.dbModel.Weather;
import com.szip.blewatch.base.db.dbModel.Weather_Table;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostJsonListBuider;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class SaveDataUtil {


    private int broadCount = 0;//发送更新数据广播的计数，为0的时候则发送广播

    private static SaveDataUtil saveDataUtil;
    private Context context;
    private SaveDataUtil(){

    }

    public static SaveDataUtil newInstance(){                     // 单例模式，双重锁
        if( saveDataUtil == null ){
            synchronized (SaveDataUtil.class){
                if( saveDataUtil == null ){
                    saveDataUtil = new SaveDataUtil();
                }
            }
        }
        return saveDataUtil ;
    }

    public void init(Context context) {
        this.context = context;
    }

    /**
     * 保存sport
     * */
    public void saveSportData(SportData sportData){
        SportData sqlData = SQLite.select()
                .from(SportData.class)
                .where(SportData_Table.time.is(sportData.time))
                .querySingle();
        if (sqlData == null){//为null则代表数据库没有保存
            sportData.save();
            LogUtil.getInstance().logd("DATA******","sport数据保存成功 time = "+sportData.time+" ;distance = "+sportData.distance+" ;caloria = "+sportData.calorie+
                    " ;speed = "+sportData.speed+" ;sportTime = "+sportData.sportTime+" type = "+sportData.type);
        }
    }

    public void saveUserInfo(UserModel userModel){
        UserModel sqlData =  SQLite.select()
                .from(UserModel.class)
                .where(UserModel_Table.id.is(userModel.id))
                .querySingle();
        if (sqlData==null) {
            userModel.save();
        } else{
            sqlData = userModel;
            sqlData.update();
        }
    }

    /**
     * 批量保存设备配置
     * */
    public void saveConfigData(SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO){
        HealthyConfig healthMonitorConfig = sportWatchAppFunctionConfigDTO.getHealthMonitorConfig();
        if (null!=healthMonitorConfig){
            sportWatchAppFunctionConfigDTO.heartRateAutoTest = healthMonitorConfig.heartRateAutoTest;
            sportWatchAppFunctionConfigDTO.ecgAutoTest = healthMonitorConfig.ecgAutoTest;
            sportWatchAppFunctionConfigDTO.bloodOxygenAutoTest= healthMonitorConfig.bloodOxygenAutoTest;
            sportWatchAppFunctionConfigDTO.bloodPressureAutoTest= healthMonitorConfig.bloodPressureAutoTest;
            sportWatchAppFunctionConfigDTO.stepCounterAutoTest= healthMonitorConfig.stepCounterAutoTest;
            sportWatchAppFunctionConfigDTO.temperatureAutoTest= healthMonitorConfig.temperatureAutoTest;
            sportWatchAppFunctionConfigDTO.sleepAutoTest= healthMonitorConfig.sleepAutoTest;
        }
        SportWatchAppFunctionConfigDTO sql = SQLite.select()
                .from(SportWatchAppFunctionConfigDTO.class)
                .where(SportWatchAppFunctionConfigDTO_Table.mac.is(sportWatchAppFunctionConfigDTO.mac))
                .querySingle();

        if (sql==null)
            sportWatchAppFunctionConfigDTO.save();
        else {
            sql = sportWatchAppFunctionConfigDTO;
            sql.update();
        }

    }

    /**
     * 批量保存设备健康配置
     * */
    public void saveHealthyConfigData(HealthyConfig healthyConfig){
        List<HealthyCardData> saveList = SQLite.select()
                .from(HealthyCardData.class)
                .queryList();
        if (saveList!=null&&saveList.size()!=0)
            return;
        ArrayList<HealthyCardData> list = new ArrayList<>();
        if (healthyConfig.heartRate == 1){
            list.add(new HealthyCardData(HealthyConst.HEART,true,0));
        }
        if (healthyConfig.stepCounter == 1){
            list.add(new HealthyCardData(HealthyConst.STEP,true,1));
        }
        if (healthyConfig.sleep == 1){
            list.add(new HealthyCardData(HealthyConst.SLEEP,true,2));
        }
        if (healthyConfig.bloodOxygen == 1){
            list.add(new HealthyCardData(HealthyConst.BLOOD_OXYGEN,true,3));
        }
        if (healthyConfig.bloodPressure == 1){
            list.add(new HealthyCardData(HealthyConst.BLOOD_PRESSURE,true,4));
        }
        if (healthyConfig.temperature == 1){
            list.add(new HealthyCardData(HealthyConst.TEMPERATURE,true,5));
        }
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<HealthyCardData>() {
                            @Override
                            public void processModel(HealthyCardData healthyCardData, DatabaseWrapper wrapper) {
                                healthyCardData.save();
                            }
                        }).addAll(list).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","卡片配置缓存成功");
            }
        }).build().execute();
    }

    /**
     * 批量更新设备健康配置
     * */
    public void updateHealthyConfigData(List<HealthyCardData> list){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<HealthyCardData>() {
                            @Override
                            public void processModel(HealthyCardData healthyCardData, DatabaseWrapper wrapper) {
                                healthyCardData.update();
                            }
                        }).addAll(list).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","卡片配置缓存成功");
            }
        }).build().execute();
    }

    /**
     * 保存天气数据
     * */
    public void saveWeatherData(Weather weather){
        Weather sql = SQLite.select()
                .from(Weather.class)
                .where(Weather_Table.id.is(weather.id))
                .querySingle();

        if (sql==null)
            weather.save();
        else {
            sql = weather;
            sql.update();
        }
    }


    /**
     * 批量保存计步
     * */
    public void saveStepDataListData(final List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();
                                if (sqlData == null) {//为null则代表数据库没有保存
                                    stepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    sqlData.calorie = stepData.calorie;
                                    sqlData.distance = stepData.distance;
                                    sqlData.steps = stepData.steps;
                                    sqlData.update();
                                }

                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计步数据保存成功");
                if (stepDataList.size()==0)
                    return;
                context.sendBroadcast(new Intent(BroadcastConst.UPDATE_STEP_VIEW));
                long start = stepDataList.get(0).getTime();
                long end = stepDataList.get(stepDataList.size()-1).getTime();
                List<StepData> sqlData = SQLite.select()
                        .from(StepData.class)
                        .where(StepData_Table.time.greaterThanOrEq(start),
                                StepData_Table.time.lessThanOrEq(end))
                        .queryList();
                uploadData(sqlData,"stepDataList");

            }
        }).build().execute();
    }


    /**
     * 批量保存详情计步
     * */
    public void saveStepInfoDataListData(final List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();

                                if (sqlData == null){//为null则代表数据库没有保存
                                    stepData.save();
                                }
                                else {//不为null则代表数据库存在，进行更新
                                    if (sqlData.dataForHour != null&&
                                            !sqlData.dataForHour.equals(stepData.dataForHour)){
                                        LogUtil.getInstance().logd("DATA******","STEP D = "+sqlData.dataForHour);
                                        int sql[] = new int[24];
                                        String[] sqlStr = (sqlData.dataForHour == null)?new String[0]:(sqlData.dataForHour.split(","));
                                        int step[] = new int[24];
                                        String[] stepStr = stepData.dataForHour.split(",");
                                        for (int i = 0;i<sqlStr.length;i++){
                                            sql[Integer.valueOf(sqlStr[i].split(":")[0])] = Integer.valueOf(sqlStr[i].split(":")[1]);
                                        }
                                        for (int i = 0;i<stepStr.length;i++){
                                            step[Integer.valueOf(stepStr[i].split(":")[0])] = Integer.valueOf(stepStr[i].split(":")[1]);
                                        }
                                        StringBuffer stepString = new StringBuffer();
                                        for (int i = 0;i<24;i++){
                                            if (sql[i]+step[i]!=0){
                                                stepString.append(String.format(Locale.ENGLISH,",%02d:%d",i,sql[i]+step[i]));
                                            }
                                        }
                                        sqlData.dataForHour = stepString.toString().substring(1);
                                    }else
                                        sqlData.dataForHour = stepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计步详情数据保存成功");
                if (stepDataList.size()==1)
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_STEP_VIEW));
            }
        }).build().execute();
    }

    /**
     * 批量保存详情计步(用来保存2523的计步数据，2523的总计步与详情计步是放在一条协议里面的)
     * */
    public void saveStepInfoDataListData1(final List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    stepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    LogUtil.getInstance().logd("DATA******","sql = "+sqlData.dataForHour+" ;step = "+stepData.dataForHour);
                                    int sql[] = new int[24];
                                    String[] sqlStr = (sqlData.dataForHour == null)?new String[0]:(sqlData.dataForHour.split(","));
                                    int step[] = new int[24];
                                    String[] stepStr = stepData.dataForHour.split(",");
                                    for (int i = 0;i<sqlStr.length;i++){
                                        sql[Integer.valueOf(sqlStr[i].substring(0,2))] = Integer.valueOf(sqlStr[i].substring(3));
                                    }
                                    for (int i = 0;i<stepStr.length;i++){
                                        step[Integer.valueOf(stepStr[i].substring(0,2))] = Integer.valueOf(stepStr[i].substring(3));
                                    }
                                    StringBuffer stepString = new StringBuffer();
                                    for (int i = 0;i<24;i++){
                                        if (sql[i]+step[i]!=0){
                                            stepString.append(String.format(Locale.ENGLISH,",%02d:%d",i,sql[i]+step[i]));
                                        }
                                    }
                                    sqlData.dataForHour = stepString.toString().substring(1);
                                    sqlData.steps += stepData.steps;
                                    sqlData.distance += stepData.distance;
                                    sqlData.calorie += stepData.calorie;
                                    sqlData.update();
                                }
                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {
                        LogUtil.getInstance().logd("DATA******",error.getMessage());
                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计步详情数据保存成功");

            }
        }).build().execute();
    }

    /**
     * 批量保存睡眠
     * */
    public void saveSleepDataListData(final List<SleepData> sleepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SleepData>() {
                            @Override
                            public void processModel(SleepData sleepData, DatabaseWrapper wrapper) {
                                if (sleepData.lightTime+sleepData.deepTime==0)
                                    return;
                                SleepData sqlData = SQLite.select()
                                        .from(SleepData.class)
                                        .where(SleepData_Table.time.is(sleepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    sleepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    sqlData.deepTime = sleepData.deepTime;
                                    sqlData.lightTime = sleepData.lightTime;
                                    sqlData.dataForHour = sleepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(sleepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","睡眠数据保存成功");
                uploadData(sleepDataList,"sleepDataList");
            }
        }).build().execute();
    }

    /**
     * 批量保存详情睡眠
     * */
    public void saveSleepInfoDataListData(final List<SleepData> sleepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SleepData>() {
                            @Override
                            public void processModel(SleepData sleepData, DatabaseWrapper wrapper) {
                                SleepData sqlData = SQLite.select()
                                        .from(SleepData.class)
                                        .where(SleepData_Table.time.is(sleepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    sleepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    sqlData.dataForHour = sleepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(sleepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","睡眠详情保存成功");
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_HEALTHY_VIEW));
            }
        }).build().execute();
    }


    /**
     * 批量保存心率
     * */
    public void saveHeartDataListData(final List<HeartData> heartDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<HeartData>() {
                            @Override
                            public void processModel(HeartData heartData, DatabaseWrapper wrapper) {
                                HeartData sqlData = SQLite.select()
                                        .from(HeartData.class)
                                        .where(HeartData_Table.time.is(heartData.time))
                                        .querySingle();
                                if (sqlData == null&&heartData.averageHeart!=0){//为null则代表数据库没有保存
                                    heartData.save();
                                }
                            }
                        }).addAll(heartDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                uploadData(heartDataList,"heartDataList");
                LogUtil.getInstance().logd("DATA******","心率数据保存成功");
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_HEALTHY_VIEW));
            }
        }).build().execute();
    }


    /**
     * 批量保存血压
     * */
    public void saveBloodPressureDataListData(final List<BloodPressureData> bloodPressureDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<BloodPressureData>() {
                            @Override
                            public void processModel(BloodPressureData bloodPressureData, DatabaseWrapper wrapper) {
                                BloodPressureData sqlData = SQLite.select()
                                        .from(BloodPressureData.class)
                                        .where(BloodPressureData_Table.time.is(bloodPressureData.time))
                                        .querySingle();
                                if (sqlData == null&&bloodPressureData.dbpDate!=0){//为null则代表数据库没有保存
                                    bloodPressureData.save();
                                }
                            }
                        }).addAll(bloodPressureDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","血压数据保存成功");
                uploadData(bloodPressureDataList,"bloodPressureDataList");
                context.sendBroadcast(new Intent(BroadcastConst.UPDATE_HEALTHY_VIEW));
            }
        }).build().execute();
    }

    /**
     * 批量保存血氧
     * */
    public void saveBloodOxygenDataListData(final List<BloodOxygenData> bloodOxygenDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<BloodOxygenData>() {
                            @Override
                            public void processModel(BloodOxygenData bloodOxygenData, DatabaseWrapper wrapper) {
                                BloodOxygenData sqlData = SQLite.select()
                                        .from(BloodOxygenData.class)
                                        .where(BloodOxygenData_Table.time.is(bloodOxygenData.time))
                                        .querySingle();
                                if (sqlData == null&&bloodOxygenData.bloodOxygenData!=0){//为null则代表数据库没有保存
                                    bloodOxygenData.save();
                                }
                            }
                        }).addAll(bloodOxygenDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","血氧数据保存成功");
                uploadData(bloodOxygenDataList,"bloodOxygenDataList");
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_HEALTHY_VIEW));
            }
        }).build().execute();
    }

    /**
     * 批量保存体温
     * */
    public void saveAnimalHeatDataListData(final List<AnimalHeatData> animalHeatDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<AnimalHeatData>() {
                            @Override
                            public void processModel(AnimalHeatData animalHeatData, DatabaseWrapper wrapper) {
                                AnimalHeatData sqlData = SQLite.select()
                                        .from(AnimalHeatData.class)
                                        .where(AnimalHeatData_Table.time.is(animalHeatData.time))
                                        .querySingle();
                                if (sqlData == null&&animalHeatData.tempData!=0){//为null则代表数据库没有保存
                                    animalHeatData.save();
                                }
                            }
                        }).addAll(animalHeatDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","体温数据保存成功");
                uploadData(animalHeatDataList,"tempDataList");
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_HEALTHY_VIEW));
            }
        }).build().execute();
    }

    /**
     * 批量保存ecg
     * */
    public void saveEcgDataListData(final List<EcgData> ecgDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<EcgData>() {
                            @Override
                            public void processModel(EcgData ecgData, DatabaseWrapper wrapper) {
                                EcgData sqlData = SQLite.select()
                                        .from(EcgData.class)
                                        .where(EcgData_Table.time.is(ecgData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    ecgData.save();
                                }
                            }
                        }).addAll(ecgDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","ECG数据保存成功");
                uploadData(ecgDataList,"ecgDataList");
                if (ecgDataList.size()==1)
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_UI_VIEW));
            }
        }).build().execute();
    }

    /**
     * 批量保存sport
     * */
    public void saveSportDataListData(final List<SportData> sportDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SportData>() {
                            @Override
                            public void processModel(SportData sportData, DatabaseWrapper wrapper) {
                                SportData sqlData = SQLite.select()
                                        .from(SportData.class)
                                        .where(SportData_Table.time.is(sportData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    sportData.save();
                                }
                            }
                        }).addAll(sportDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","多运动数据保存成功");
                uploadData(sportDataList,"sportDataList");
                if (sportDataList.size()==1)
                    context.sendBroadcast(new Intent(BroadcastConst.UPDATE_SPORT_VIEW));
            }
        }).build().execute();
    }

    public void saveNotificationList(final List<NotificationData> notificationDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<NotificationData>() {
                            @Override
                            public void processModel(NotificationData notificationData, DatabaseWrapper wrapper) {
                                NotificationData sqlData = SQLite.select()
                                        .from(NotificationData.class)
                                        .where(NotificationData_Table.packageName.is(notificationData.packageName))
                                        .querySingle();
                                if (sqlData!=null){
                                    sqlData.name = notificationData.name;
                                    sqlData.update();
                                }else
                                    notificationData.save();
                            }
                        }).addAll(notificationDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
            }
        }).build().execute();
    }

    public void unbind(long id){
        UserModel userModel = SQLite.select()
                .from(UserModel.class)
                .where(UserModel_Table.id.is(id))
                .querySingle();
        if (userModel!=null){
            userModel.deviceCode = null;
            userModel.update();
        }

        SQLite.delete()
                .from(HealthyCardData.class)
                .execute();
    }

    /**
     * 批量保存计划表数据
     * */
    public void saveScheduleListData(List<ScheduleData> scheduleDataList){
        SQLite.delete()
                .from(ScheduleData.class)
                .execute();
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<ScheduleData>() {
                            @Override
                            public void processModel(ScheduleData scheduleData, DatabaseWrapper wrapper) {
                                ScheduleData sqlData = SQLite.select()
                                        .from(ScheduleData.class)
                                        .where(ScheduleData_Table.time.is(scheduleData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    scheduleData.save();
                                }
                            }
                        }).addAll(scheduleDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计划表数据保存成功");
                context.sendBroadcast(new Intent(BroadcastConst.UPDATE_UI_VIEW));
            }
        }).build().execute();
    }


    private void uploadData(Object datas,String type){
        Map<String,Object> json = new HashMap<>();
        json.put(type,datas);
        Gson gson = new Gson();
        PostJsonListBuider jsonBuilder =   OkHttpUtils
                .listpost()
                .addInterceptor(new TokenInterceptor())
                .addParams("data",gson.toJson(json));
        HttpClientUtils.newInstance().buildRequest(jsonBuilder, "data/upload", new Callback() {
            @Override
            public Object parseNetworkResponse(Response response, int id) throws Exception {
                return null;
            }

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(Object response, int id) {

            }
        });
    }

    //保存云端下载的数据
    public void saveWebData(DownloadDataBean response){
        if (response.getData().getBloodOxygenData().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<BloodOxygenData>() {
                                @Override
                                public void processModel(BloodOxygenData bloodOxygenData, DatabaseWrapper wrapper) {
                                    BloodOxygenData sqlData = SQLite.select()
                                            .from(BloodOxygenData.class)
                                            .where(BloodOxygenData_Table.time.is(bloodOxygenData.time))
                                            .querySingle();
                                    if (sqlData == null&&bloodOxygenData.bloodOxygenData!=0){//为null则代表数据库没有保存
                                        bloodOxygenData.save();
                                    }
                                }
                            }).addAll(response.getData().getBloodOxygenData()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {

                }
            }).build().execute();
        }

        if (response.getData().getBloodPressureDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<BloodPressureData>() {
                                @Override
                                public void processModel(BloodPressureData bloodPressureData, DatabaseWrapper wrapper) {
                                    BloodPressureData sqlData = SQLite.select()
                                            .from(BloodPressureData.class)
                                            .where(BloodPressureData_Table.time.is(bloodPressureData.time))
                                            .querySingle();
                                    if (sqlData == null&&bloodPressureData.dbpDate!=0){//为null则代表数据库没有保存
                                        bloodPressureData.save();
                                    }
                                }
                            }).addAll(response.getData().getBloodPressureDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {

                }
            }).build().execute();
        }

        if (response.getData().getHeartDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<HeartData>() {
                                @Override
                                public void processModel(HeartData heartData, DatabaseWrapper wrapper) {
                                    HeartData sqlData = SQLite.select()
                                            .from(HeartData.class)
                                            .where(HeartData_Table.time.is(heartData.time))
                                            .querySingle();
                                    if (sqlData == null&&heartData.averageHeart!=0){//为null则代表数据库没有保存
                                        heartData.save();
                                    }
                                }
                            }).addAll(response.getData().getHeartDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {

                }
            }).build().execute();
        }

        if (response.getData().getSleepDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<SleepData>() {
                                @Override
                                public void processModel(SleepData sleepData, DatabaseWrapper wrapper) {
                                    if (sleepData.lightTime+sleepData.deepTime==0)
                                        return;
                                    SleepData sqlData = SQLite.select()
                                            .from(SleepData.class)
                                            .where(SleepData_Table.time.is(sleepData.time))
                                            .querySingle();
                                    if (sqlData == null){//为null则代表数据库没有保存
                                        sleepData.save();
                                    } else {//不为null则代表数据库存在，进行更新
                                        sqlData.deepTime = sleepData.deepTime;
                                        sqlData.lightTime = sleepData.lightTime;
                                        sqlData.dataForHour = sleepData.dataForHour;
                                        sqlData.update();
                                    }
                                }
                            }).addAll(response.getData().getSleepDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {
                }
            }).build().execute();
        }

        if (response.getData().getStepDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<StepData>() {
                                @Override
                                public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                    StepData sqlData = SQLite.select()
                                            .from(StepData.class)
                                            .where(StepData_Table.time.is(stepData.time))
                                            .querySingle();
                                    if (sqlData == null){//为null则代表数据库没有保存
                                        stepData.save();
                                    }
                                    else {//不为null则代表数据库存在，进行更新
                                        sqlData.calorie = stepData.calorie;
                                        sqlData.distance = stepData.distance;
                                        sqlData.steps = stepData.steps;
                                        if (sqlData.dataForHour == null)
                                            sqlData.dataForHour = stepData.dataForHour;
                                        sqlData.update();
                                    }
                                }
                            }).addAll(response.getData().getStepDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {
                }
            }).build().execute();
        }


        if (response.getData().getEcgDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<EcgData>() {
                                @Override
                                public void processModel(EcgData ecgData, DatabaseWrapper wrapper) {
                                    EcgData sqlData = SQLite.select()
                                            .from(EcgData.class)
                                            .where(EcgData_Table.time.is(ecgData.time))
                                            .querySingle();
                                    if (sqlData == null){//为null则代表数据库没有保存
                                        ecgData.save();
                                    }
                                }
                            }).addAll(response.getData().getEcgDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {

                }
            }).build().execute();
        }


        if (response.getData().getAnimalHeatDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<AnimalHeatData>() {
                                @Override
                                public void processModel(AnimalHeatData animalHeatData, DatabaseWrapper wrapper) {
                                    AnimalHeatData sqlData = SQLite.select()
                                            .from(AnimalHeatData.class)
                                            .where(AnimalHeatData_Table.time.is(animalHeatData.time))
                                            .querySingle();
                                    if (sqlData == null&&animalHeatData.tempData!=0){//为null则代表数据库没有保存
                                        animalHeatData.save();
                                    }
                                }
                            }).addAll(response.getData().getAnimalHeatDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {

                }
            }).build().execute();
        }

        if (response.getData().getSportDataList().size()!=0){
            FlowManager.getDatabase(AppDatabase.class)
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                            new ProcessModelTransaction.ProcessModel<SportData>() {
                                @Override
                                public void processModel(SportData sportData, DatabaseWrapper wrapper) {
                                    SportData sqlData = SQLite.select()
                                            .from(SportData.class)
                                            .where(SportData_Table.time.is(sportData.time))
                                            .querySingle();
                                    if (sqlData == null){//为null则代表数据库没有保存
                                        sportData.save();
                                    }
                                }
                            }).addAll(response.getData().getSportDataList()).build())  // add elements (can also handle multiple)
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable error) {

                        }
                    }).success(new Transaction.Success() {
                @Override
                public void onSuccess(Transaction transaction) {

                }
            }).build().execute();
        }
    }

    /**
     * 清除数据库
     * */
    public void clearDB(){
        SQLite.delete()
                .from(BloodOxygenData.class)
                .execute();
        SQLite.delete()
                .from(BloodPressureData.class)
                .execute();
        SQLite.delete()
                .from(EcgData.class)
                .execute();
        SQLite.delete()
                .from(HeartData.class)
                .execute();
        SQLite.delete()
                .from(SleepData.class)
                .execute();
        SQLite.delete()
                .from(StepData.class)
                .execute();
        SQLite.delete()
                .from(SportData.class)
                .execute();
        SQLite.delete()
                .from(AnimalHeatData.class)
                .execute();
    }

}
