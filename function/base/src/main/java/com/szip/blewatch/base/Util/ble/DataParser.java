package com.szip.blewatch.base.Util.ble;




import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
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

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Hqs on 2018/1/4
 */
public class DataParser {


    private ArrayList<BleStepModel> stepDataArrayList;
    private ArrayList<StepData> stepOnDayDataArrayList;
    private ArrayList<BloodPressureData> bloodPressureDataArrayList;
    private ArrayList<AnimalHeatData> getAnimalHeatDataArrayList;
    private ArrayList<BloodOxygenData> bloodOxygenDataArrayList;
    private ArrayList<HeartData> heartDataArrayList;
    private ArrayList<AnimalHeatData> animalHeatDataArrayList;
    private ArrayList<SportData> sportDataArrayList;
    private ArrayList<ScheduleData> scheduleDataArrayList;
    private ArrayList<SleepData> sleepDataArrayList;
    private long timeOfdata = 0;//用来存储上一段数据的时间，以判断下一段数据是否为同一天的数据

    private IDataResponse mIDataResponse;

    private int dataType = 0;

    private DataParser(){}
    private static DataParser mDataParser;
    public static DataParser newInstance(){                     // 单例模式，双重锁
        if( mDataParser == null ){
            synchronized (DataParser.class){
                if( mDataParser == null ){
                    mDataParser = new DataParser();
                }
            }
        }
        return mDataParser ;
    }

    public void setmIDataResponse(IDataResponse mIDataResponse) {
        this.mIDataResponse = mIDataResponse;
    }

    public void parseNotifyData(byte[] data){

        if (data[1] == 0x15){
            if (mIDataResponse!=null)
                mIDataResponse.onCamera(data[8]);
        }else if (data[1] == 0x16){
            if (mIDataResponse!=null)
                mIDataResponse.findPhone(data[8]);
        }else if (data[1] == 0x46){
            if (mIDataResponse==null)
                return;
            if (data[9]==2||data[9]==5){
                mIDataResponse.sendBackgroundError();
            }else if (data[9] == 6){
                mIDataResponse.startSendFile();
            }else {
                if (data[8]==0){
                    mIDataResponse.sendBackgroundStart();
                }else if (data[8]==1){
                    mIDataResponse.sendBackgroundProgress();
                }else {
                    mIDataResponse.sendBackgroundFinish();
                }
            }
        }else if (data[1] == 0x47){
            if (mIDataResponse==null)
                return;
            switch (data[8]){
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                case 6:{//开始包
                    if(data[9]==2){//手表端已经存在该文件，无需传送直接发送传送成功
                        mIDataResponse.sendDialFinish();
                    }else if (data[9] == 5){//开始包发送失败
                        mIDataResponse.sendDialError();
                    }else {//准备开始传送
                        int address = (data[10] & 0xff) + ((data[11] & 0xFF) << 8) +
                                ((data[12] & 0xff) << 16) + ((data[13] & 0xFF) << 24);
                        mIDataResponse.sendDialStart(address);
                    }
                }
                    break;
                case 4:
                case 7:{//传输包
                    if (data[9]==1) {//传输包进度+1
                        mIDataResponse.sendDialProgress();
                    } else if (data[9] == 2) {//传输包发送失败，重新开始传送
                        mIDataResponse.sendDialContinue((data[10] & 0xff) + ((data[11] & 0xFF) << 8));
                    } else {//因其他问题导致的传输失败
                        mIDataResponse.sendDialError();
                    }
                }
                    break;
                case 5:
                case 8:{//结束包，收到即代表传输结束
                    mIDataResponse.sendDialFinish();
                }
                break;
            }
        }else if (data[1] == 0x48){//音乐控制
            if (mIDataResponse!=null){
                if (data[8] != 4){
                    mIDataResponse.onMusicControl(data[8],0);
                }else {
                    mIDataResponse.onMusicControl(data[8],data[9]);
                }
            }
        }else if (data[1] == 0x50){//来电挂断
//            if (mIDataResponse!=null)
//                mIDataResponse.endCall();
        }else if (data[1] == 0x52||data[1] == 0x53||data[1] == 0x54){
            if (mIDataResponse!=null){
                mIDataResponse.onScheduleCallback(data[1]&0xff,data[8]&0xff);
            }
        }else if (data[1] == 0x55){//手表返回空的日程列表的时候，特殊处理
            SQLite.delete()
                    .from(ScheduleData.class)
                    .execute();
            mIDataResponse.onScheduleRefresh(false);
        }else if (data[1] == 0x56){
            mIDataResponse.onScheduleRefresh(true);
        }
    }

    public void parseReadData(int type, byte[] data, long time, boolean isEnd){
        if(dataType == 0){
            dataType = type;
        }else if (dataType != type){
            saveData(dataType);
            dataType = type;
        }
        if(type==0x01){//计步数据
            if (stepDataArrayList==null)
                stepDataArrayList = new ArrayList<>();
            long timeOfDay = DateUtil.getTimeScopeForDay(time);//这段计步数据所属的日期
            LogUtil.getInstance().logd("DATA******","timeOfDay = "+timeOfDay);
            if (timeOfDay!= timeOfdata){//判断这段计步数据是否属于同一天，如果跟上次缓存的时间不一样，说明是跨天了
                timeOfdata = timeOfDay;
                int step = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
                int distence = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
                int calorie = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time*1000);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                LogUtil.getInstance().logd("DATA******","hour = "+hour);
                HashMap<Integer,Integer> hashMap = new HashMap<>();
                hashMap.put(hour,step);
                stepDataArrayList.add(new BleStepModel(step,distence,calorie/1000,timeOfDay,hashMap));
            }else {
                BleStepModel model = stepDataArrayList.get(stepDataArrayList.size()-1);
                int step = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
                int distence = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
                int calorie = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time*1000);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                model.setStep(step);
                model.setCalorie(calorie/1000);
                model.setDistance(distence);
                model.setStepInfo(hour,step);
            }

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveStepDatas(stepDataArrayList);
                stepDataArrayList = null;
                timeOfdata = 0;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","计步数据接受结束");
            }
        }else if (type==0x02){//心率数据
            if (heartDataArrayList==null)
                heartDataArrayList = new ArrayList<>();
            int heart = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            if (heart!=0)
                heartDataArrayList.add(new HeartData(time,heart));
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveHeartDatas(heartDataArrayList);
                heartDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","心率数据接受结束");
            }
        }else if (type==0x03){//睡眠数据
            if (sleepDataArrayList==null)
                sleepDataArrayList = new ArrayList<>();
            long timeOfDay = DateUtil.getSleepTimeScopeForDay(time);//这段心率数据所属的日期
            int all = 0;
            int deep = 0;
            StringBuffer dataForHour = new StringBuffer();
            for (int i = 0;i<data.length-3;i+=3){
                if (i == 0){//第一个点，保存为睡眠起始时间
                    dataForHour.append(String.format(Locale.ENGLISH,"%d:%d",data[0],data[1]));
                }

                int num = data[i+3]*60+data[i+4]-data[i]*60-data[i+1];
                if (num<0)
                    num+=1440;
                dataForHour.append(String.format(",%d:%d",num,data[i+2]));
                all+=num;
                if (data[i+2]==2){//深睡
                    deep+=num;
                }
            }

            sleepDataArrayList.add(new SleepData(timeOfDay,deep,all-deep,dataForHour.toString()));
            LogUtil.getInstance().logd("DATA******","解析到的睡眠详情:"+"深睡 = "+deep+" ;浅睡 = "+(all-deep)+" ;睡眠详情 = "+dataForHour.toString());
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveSleepDatas(sleepDataArrayList);
                sleepDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","睡眠数据接受结束");
            }
        }else if (type==0x04){//跑步数据
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            if (data[0]==0)
                sportData.type = 2;
            else
                sportData.type = 6;
            sportData.time = time;
            sportData.distance = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            sportData.sportTime = (data[12] & 0xff) + ((data[13] & 0xFF) << 8) + ((data[14] & 0xff) << 16) + ((data[15] & 0xFF) << 24);
            sportData.calorie = (data[16] & 0xff) + ((data[17] & 0xFF) << 8) + ((data[18] & 0xff) << 16) + ((data[19] & 0xFF) << 24);
            sportData.height = (data[20] & 0xff) + ((data[21] & 0xFF) << 8) + ((data[22] & 0xff) << 16) + ((data[23] & 0xFF) << 24);
            int longLenght = (data[32] & 0xff) + ((data[33] & 0xFF) << 8);
            int latLenght = (data[32+2+longLenght] & 0xff) + ((data[33+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[32+4+longLenght+latLenght] & 0xff) + ((data[33+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,32+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int altitudeLenght = (data[32+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[33+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,32+8+longLenght+latLenght+speedPerHourLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }
            int speedLenght = (data[32+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xff) +
                    ((data[33+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] speedDatas = new byte[speedLenght*2];
            if (speedLenght!=0)
                System.arraycopy(data,32+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2,speedDatas,0,speedLenght*2);
            dataHash = CommandUtil.getAverage(speedDatas,2);
            for (int key:dataHash.keySet()){
                sportData.speed = key;
                sportData.speedArray = dataHash.get(key);
            }

            int heartLenght = (data[32+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xff) +
                    ((data[33+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,32+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            int strideLenght = (data[32+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xff) +
                    ((data[33+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF+heartLenght) << 8);
            byte[] strideArray = new byte[strideLenght*2];
            if (strideLenght!=0)
                System.arraycopy(data,32+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght,
                        strideArray,0,strideLenght*2);
            dataHash = CommandUtil.getAverage(strideArray,2);
            for (int key:dataHash.keySet()){
                sportData.stride = key;
                sportData.strideArray = dataHash.get(key);
            }

            sportData.step = (data[32+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght+strideLenght*2] & 0xff) +
                    ((data[33+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght+strideLenght*2] & 0xFF)<< 8) +
                    ((data[34+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght+strideLenght*2] & 0xFF)<< 16) +
                    ((data[35+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght+strideLenght*2] & 0xFF)<< 24);

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的跑步数据 : "+time+" ;跑步时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;距离 = "+sportData.distance+" ;平均时速 = "+sportData.speedPerHour+" ;时速数组 = "+sportData.speedPerHourArray
                    +" ;平均配速 = "+sportData.speed+" ;配速数组 = "+sportData.speedArray+" ;平均心率 = "+sportData.heart
                    +" ;心率数组 = "+sportData.heartArray+" ;平均海拔 = "+sportData.altitude+" ;海拔数组 = "+sportData.altitudeArray);
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","跑步数据接受结束");
            }
        }else if (type==0x05){//徒步数据
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 1;
            sportData.time = time;
            sportData.distance = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.step = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            sportData.calorie = (data[12] & 0xff) + ((data[13] & 0xFF) << 8) + ((data[14] & 0xff) << 16) + ((data[15] & 0xFF) << 24);
            sportData.height = (data[16] & 0xff) + ((data[17] & 0xFF) << 8) + ((data[18] & 0xff) << 16) + ((data[19] & 0xFF) << 24);

            int longLenght = (data[28] & 0xff) + ((data[29] & 0xFF) << 8);
            int latLenght = (data[28+2+longLenght] & 0xff) + ((data[29+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[28+4+longLenght+latLenght] & 0xff) + ((data[29+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,28+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int altitudeLenght = (data[28+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[29+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,28+8+longLenght+latLenght+speedPerHourLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }
            int speedLenght = (data[28+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xff) +
                    ((data[29+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] speedDatas = new byte[speedLenght*2];
            if (speedLenght!=0)
                System.arraycopy(data,28+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2,speedDatas,0,speedLenght*2);
            dataHash = CommandUtil.getAverage(speedDatas,2);
            for (int key:dataHash.keySet()){
                sportData.speed = key;
                sportData.speedArray = dataHash.get(key);
            }
            int heartLenght = (data[28+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xff) +
                    ((data[29+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,28+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            int strideLenght = (data[28+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xff) +
                    ((data[29+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 8);
            byte[] strideArray = new byte[strideLenght*2];
            if (strideLenght!=0)
                System.arraycopy(data,28+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght,
                        strideArray,0,strideLenght*2);
            dataHash = CommandUtil.getAverage(strideArray,2);
            for (int key:dataHash.keySet()){
                sportData.stride = key;
                sportData.strideArray = dataHash.get(key);
            }
            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的徒步数据 : "+time+" ;时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;步数 = "+sportData.step+" ;徒步里程"+sportData.distance+" ;平均时速 = "+sportData.speedPerHour+" ;时速数组"+sportData.speedPerHourArray
                    +" ;平均配速 = "+sportData.speed+" ;配速数组"+sportData.speedArray+ " ;平均心率 = "+sportData.heart+" ;心率数组"+sportData.heartArray+
                    " ;平均海拔 = "+sportData.altitude+" ;海拔数组 = "+sportData.altitudeArray);
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","徒步数据接受结束");
            }
        }else if (type==0x06){//马拉松
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 5;
            sportData.time = time;
            sportData.distance = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            sportData.sportTime = (data[12] & 0xff) + ((data[13] & 0xFF) << 8) + ((data[14] & 0xff) << 16) + ((data[15] & 0xFF) << 24);
            sportData.calorie = (data[16] & 0xff) + ((data[17] & 0xFF) << 8) + ((data[18] & 0xff) << 16) + ((data[19] & 0xFF) << 24);
            sportData.height = (data[20] & 0xff) + ((data[21] & 0xFF) << 8) + ((data[22] & 0xff) << 16) + ((data[23] & 0xFF) << 24);

            int longLenght = (data[32] & 0xff) + ((data[33] & 0xFF) << 8);
            int latLenght = (data[32+2+longLenght] & 0xff) + ((data[33+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[32+4+longLenght+latLenght] & 0xff) + ((data[33+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,32+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }


            int altitudeLenght = (data[32+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[33+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,32+8+longLenght+latLenght+speedPerHourLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }
            int speedLenght = (data[32+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xff) +
                    ((data[33+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] speedDatas = new byte[speedLenght*2];
            if (speedLenght!=0)
                System.arraycopy(data,32+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2,speedDatas,0,speedLenght*2);
            dataHash = CommandUtil.getAverage(speedDatas,2);
            for (int key:dataHash.keySet()){
                sportData.speed = key;
                sportData.speedArray = dataHash.get(key);
            }
            int heartLenght = (data[32+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xff) +
                    ((data[33+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,32+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            int strideLenght = (data[32+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xff) +
                    ((data[33+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF+heartLenght) << 8);
            byte[] strideArray = new byte[strideLenght*2];
            if (strideLenght!=0)
                System.arraycopy(data,32+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght,
                        strideArray,0,strideLenght*2);
            dataHash = CommandUtil.getAverage(strideArray,2);
            for (int key:dataHash.keySet()){
                sportData.stride = key;
                sportData.strideArray = dataHash.get(key);
            }

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的马拉松数据 : "+time+" ;跑步时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;步数 = "+sportData.distance+" ;平均时速 = "+sportData.speedPerHour+" ;时速数组"+sportData.speedPerHourArray+" ;平均配速 = "+sportData.speed+
                    " ;配速数组"+sportData.speedArray+ " ;平均心率 = "+sportData.heart+" ;心率数组"+sportData.heartArray+" ;平均海拔 = "
                    +sportData.altitude+" ;海拔数组 = "+sportData.altitudeArray);
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","马拉松数据接受结束");
            }

        }else if (type==0x07){//跳绳
            if (isEnd)
                LogUtil.getInstance().logd("DATA******","跳绳数据接受结束");

        }else if (type==0x08){//户外游泳
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 18;
            sportData.time = time;
            sportData.distance = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            int longLenght = (data[16] & 0xff) + ((data[17] & 0xFF) << 8);
            int latLenght = (data[16+2+longLenght] & 0xff) + ((data[17+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[16+4+longLenght+latLenght] & 0xff) + ((data[17+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,16+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int heartLenght = (data[16+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[17+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,16+8+longLenght+latLenght+speedPerHourLenght,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            int tempLenght = (data[16+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xff) +
                    ((data[17+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 8);
            int speedLenght = (data[16+10+longLenght+latLenght+speedPerHourLenght+heartLenght+tempLenght*2] & 0xff) +
                    ((data[17+10+longLenght+latLenght+speedPerHourLenght+heartLenght+tempLenght*2] & 0xFF) << 8);

            sportData.calorie = (data[16+12+longLenght+latLenght+speedPerHourLenght+heartLenght+tempLenght*2+speedLenght*2] & 0xff) +
                    ((data[17+12+longLenght+latLenght+speedPerHourLenght+heartLenght+tempLenght*2+speedLenght*2] & 0xFF)<< 8) +
                    ((data[18+12+longLenght+latLenght+speedPerHourLenght+heartLenght+tempLenght*2+speedLenght*2] & 0xFF)<< 16) +
                    ((data[19+12+longLenght+latLenght+speedPerHourLenght+heartLenght+tempLenght*2+speedLenght*2] & 0xFF)<< 24);


            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的游泳数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;里程"+sportData.distance+
                    " ;卡路里"+sportData.calorie+ " ;平均心率 = "+ sportData.heart+" ;心率数组 ="+ sportData.heartArray +" ;平均速度 = "+
                    sportData.speedPerHour+" ;配速数组 = "+ sportData.speedPerHourArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","游泳数据接受结束");
            }

        }else if (type==0x09){//攀岩
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 19;
            sportData.time = time;
            sportData.sportTime = (data[2] & 0xff) + ((data[3] & 0xFF) << 8) + ((data[4] & 0xff) << 16) + ((data[5] & 0xFF) << 24);
            sportData.height = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);

            int heartLenght = (data[6] & 0xff) + ((data[7] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,8,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            sportData.calorie = (data[8+heartLenght] & 0xff) +
                    ((data[9+heartLenght] & 0xFF)<< 8) +
                    ((data[10+heartLenght] & 0xFF)<< 16) +
                    ((data[11+heartLenght] & 0xFF)<< 24);

            int altitudeLenght = (data[12+heartLenght] & 0xff) + ((data[13+heartLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,14+heartLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的攀岩数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;攀爬高度"+sportData.height+
                    " ;平均心率 = "+ sportData.heart+" ;心率数组 ="+ sportData.heartArray+" ;卡路里 = "+sportData.calorie+" ;爬升高度 = "+sportData.height+
                    " ;海拔数组 = "+sportData.altitudeArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","攀岩数据接受结束");
            }

        }else if (type==0x0a){//滑雪
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 12;
            sportData.time = time;
            sportData.distance = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);
            sportData.height = (data[2] & 0xff) + ((data[3] & 0xFF) << 8);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            int longLenght = (data[16] & 0xff) + ((data[17] & 0xFF) << 8);
            int latLenght = (data[16+2+longLenght] & 0xff) + ((data[17+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[16+4+longLenght+latLenght] & 0xff) + ((data[17+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,16+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int altitudeLenght = (data[16+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[17+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,16+8+longLenght+latLenght+speedPerHourLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }

            int heartLenght = (data[16+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xff) +
                    ((data[17+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,16+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportData.calorie = (data[16+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xff) +
                    ((data[17+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF)<< 8) +
                    ((data[18+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF) << 16) +
                    ((data[19+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF) << 24);

            int speedLenght = (data[16+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xff) +
                    ((data[17+14+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF) << 8);
            byte[] speedDatas = new byte[speedLenght*2];
            if (speedLenght!=0)
                System.arraycopy(data,16+16+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght,speedDatas,0,speedLenght*2);
            dataHash = CommandUtil.getAverage(speedDatas,2);
            for (int key:dataHash.keySet()){
                sportData.speed = key;
                sportData.speedArray = dataHash.get(key);
            }

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的滑雪数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;爬升高度 = "+sportData.altitude+
                    " ;里程 = "+sportData.distance+" ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray+" ;海拔数组 = "+sportData.altitudeArray+
                    " ;卡路里 = "+sportData.calorie+ " ;时速 = "+sportData.speedPerHour+" ;时速数组 = "+sportData.speedPerHourArray+ " ;配速 = "+sportData.speed+
                    " ;配速数组 = " +sportData.speedArray);
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","滑雪数据接受结束");
            }
        }else if (type==0x0b){//骑行
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 11;
            sportData.time = time;
            sportData.sportTime = (data[14] & 0xff) + ((data[15] & 0xFF) << 8) + ((data[16] & 0xff) << 16) + ((data[17] & 0xFF) << 24);
            sportData.distance = (data[10] & 0xff) + ((data[11] & 0xFF) << 8) + ((data[12] & 0xff) << 16) + ((data[13] & 0xFF) << 24);
            int longLenght = (data[26] & 0xff) + ((data[27] & 0xFF) << 8);
            int latLenght = (data[26+2+longLenght] & 0xff) + ((data[27+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[26+4+longLenght+latLenght] & 0xff) + ((data[27+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,26+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int altitudeLenght = (data[26+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[27+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,26+8+longLenght+latLenght+speedPerHourLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }

            int speedLenght = (data[26+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xff) +
                    ((data[27+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] speedDatas = new byte[speedLenght*2];
            if (speedLenght!=0)
                System.arraycopy(data,26+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2,speedDatas,0,speedLenght*2);
            dataHash = CommandUtil.getAverage(speedDatas,2);
            for (int key:dataHash.keySet()){
                sportData.speed = key;
                sportData.speedArray = dataHash.get(key);
            }

            int heartLenght = (data[26+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xff) +
                    ((data[27+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,26+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            sportData.calorie = (data[26+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xff) +
                    ((data[27+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 8) +
                    ((data[28+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 16) +
                    ((data[29+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 24);

            sportData.height = (data[30+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xff) +
                    ((data[31+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 8)+
                    ((data[32+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 16) +
                    ((data[33+12+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+speedLenght*2+heartLenght] & 0xFF) << 24);

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的骑行数据 : "+time+" ;运动时长 = "+sportData.sportTime+
                    " ;距离 = "+sportData.distance+" ;平均配速 = "+sportData.speed+" ;配速数组"+sportData.speedArray+
                    " ;平均心率 = "+sportData.heart+" ;心率数组"+sportData.heartArray+" ;平均海拔 = "+sportData.altitude+" ;海拔数组 = "+sportData.altitudeArray+
                    " ;卡路里 = "+sportData.calorie+" ;爬升高度 = "+sportData.height);
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","骑行数据接受结束");
            }

        }else if (type==0x0c){//划船
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 20;
            sportData.time = time;
            sportData.distance = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.speed = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            int longLenght = (data[20] & 0xff) + ((data[21] & 0xFF) << 8);
            int latLenght = (data[20+2+longLenght] & 0xff) + ((data[21+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[20+4+longLenght+latLenght] & 0xff) + ((data[21+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,20+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int heartLenght = (data[20+6+longLenght+latLenght+speedPerHourLenght] & 0xff) +
                    ((data[21+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,20+8+longLenght+latLenght+speedPerHourLenght,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            sportData.calorie = (data[20+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xff) +
                    ((data[21+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 8) +
                    ((data[22+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 16) +
                    ((data[23+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 24);

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的划船数据 : "+time+" ;运动时长 = "+sportData.sportTime+
                    " ;距离 = "+sportData.distance+" ;划桨频率 = "+sportData.speed+" ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","划船数据接受结束");
            }
        }else if (type==0x0d){//蹦极
            if (isEnd)
                LogUtil.getInstance().logd("DATA******","蹦极数据接受结束");


        }else if (type==0x0e){//登山
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 4;
            sportData.time = time;
            sportData.distance = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.step = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            sportData.height = (data[12] & 0xff) + ((data[13] & 0xFF) << 8) + ((data[14] & 0xff) << 16) + ((data[15] & 0xFF) << 24);
            int longLenght = (data[24] & 0xff) + ((data[25] & 0xFF) << 8);
            int latLenght = (data[24+2+longLenght] & 0xff) + ((data[25+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[24+4+longLenght+latLenght] & 0xff) + ((data[25+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,24+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }

            int altitudeLenght = (data[24+6+longLenght+latLenght+speedPerHourLenght] & 0xff) + ((data[25+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,24+8+longLenght+latLenght+speedPerHourLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }
            int heartLenght = (data[24+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xff) +
                    ((data[25+8+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,24+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportData.calorie = (data[24+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xff) +
                    ((data[25+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF) << 8) +
                    ((data[26+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF) << 16) +
                    ((data[27+10+longLenght+latLenght+speedPerHourLenght+altitudeLenght*2+heartLenght] & 0xFF) << 24);

            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的登山数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;记步数 = "+sportData.step+
                    " ;攀爬高度"+sportData.height+" ;距离 = "+sportData.distance+" ;平均心率 = "+sportData.heart+" ;心率数组"+sportData.heartArray
                    +" ;海拔数组 = "+sportData.altitudeArray+" ;卡路里 = "+sportData.calorie);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","登山数据接受结束");
            }

        }else if (type==0x0f){//跳伞
            if (isEnd)
                LogUtil.getInstance().logd("DATA******","跳伞数据接受结束");


        }else if (type==0x10){//高尔夫
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 21;
            sportData.time = time;
            sportData.pole = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.step = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            int longLenght = (data[20] & 0xff) + ((data[21] & 0xFF) << 8);
            int latLenght = (data[20+2+longLenght] & 0xff) + ((data[21+2+longLenght] & 0xFF) << 8);
            int altitudeLenght = (data[20+4+longLenght+latLenght] & 0xff) + ((data[21+6+longLenght+latLenght] & 0xFF) << 8);
            byte[] altitudeDatas = new byte[altitudeLenght*2];
            if (altitudeLenght!=0)
                System.arraycopy(data,20+6+longLenght+latLenght,altitudeDatas,0,altitudeLenght*2);
            dataHash = CommandUtil.getAverage(altitudeDatas,2);
            for (int key:dataHash.keySet()){
                sportData.altitude = key;
                sportData.altitudeArray = dataHash.get(key);
            }
            int heartLenght = (data[20+6+longLenght+latLenght+altitudeLenght*2] & 0xff) +
                    ((data[21+6+longLenght+latLenght+altitudeLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,20+8+longLenght+latLenght+altitudeLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的高尔夫数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;挥杆次数 = "+sportData.pole+
                    " ;计步数据 = "+sportData.step+" ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray+" ;海拔数组 = "+sportData.altitudeArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","高尔夫数据接受结束");
            }

        }else if (type==0x11){//冲浪
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 22;
            sportData.time = time;
            sportData.distance = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            int longLenght = (data[16] & 0xff) + ((data[17] & 0xFF) << 8);
            int latLenght = (data[16+2+longLenght] & 0xff) + ((data[17+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[16+4+longLenght+latLenght] & 0xff) + ((data[17+4+longLenght+latLenght] & 0xFF) << 8);
            byte[] speedHourDatas = new byte[speedPerHourLenght];
            if (speedPerHourLenght!=0)
                System.arraycopy(data,16+6+longLenght+latLenght,speedHourDatas,0,speedPerHourLenght);
            dataHash = CommandUtil.getAverage(speedHourDatas,1);
            for (int key:dataHash.keySet()){
                sportData.speedPerHour = key;
                sportData.speedPerHourArray = dataHash.get(key);
            }


            int heartLenght = (data[16+6+longLenght+latLenght+speedPerHourLenght] & 0xff) +
                    ((data[17+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,16+8+longLenght+latLenght+speedPerHourLenght,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }

            sportData.calorie = (data[16+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xff) +
                    ((data[17+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 8) +
                    ((data[18+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 16) +
                    ((data[19+8+longLenght+latLenght+speedPerHourLenght+heartLenght] & 0xFF) << 24);


            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的登山数据 : "+time+" ;运动时长 = "+sportData.sportTime+
                    " ;距离 = "+sportData.distance+" ;平均心率 = "+sportData.heart);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","冲浪数据接受结束");
            }
        }else if (type==0x14){//跑步机
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 3;
            sportData.time = time;
            sportData.sportTime = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            sportData.step = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.calorie = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            int strideLenght = (data[12] & 0xff) + ((data[13] & 0xFF) << 8);
            byte[] strideDatas = new byte[strideLenght*2];
            if (strideLenght!=0)
                System.arraycopy(data,12+2,
                        strideDatas,0,strideLenght*2);
            dataHash = CommandUtil.getAverage(strideDatas,2);
            for (int key:dataHash.keySet()){
                sportData.stride = key;
                sportData.strideArray = dataHash.get(key);
            }

            int heartLenght = (data[12+2+strideLenght*2] & 0xff) +
                    ((data[13+2+strideLenght*2] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,12+4+strideLenght*2,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的跑步机数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;步频 = "+sportData.stride+" ;步频数组 = "+sportData.strideArray+" ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","跑步机数据接受结束");
            }
        }else if (type == 0x17){//实时心率
            if (heartDataArrayList==null)
                heartDataArrayList = new ArrayList<>();
            int heart = data[0]&0xff;
            if (heart!=0)
                heartDataArrayList.add(new HeartData(time,heart));
            if (mIDataResponse!=null)
                mIDataResponse.onSaveHeartDatas(heartDataArrayList);
            heartDataArrayList = null;
            dataType = 0;
            LogUtil.getInstance().logd("DATA******","实时心率数据接受结束 = "+heart);
        }else if (type == 0x18){//实时体温
            if (animalHeatDataArrayList==null)
                animalHeatDataArrayList = new ArrayList<>();
            int temp = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);
            animalHeatDataArrayList.add(new AnimalHeatData(time,temp));
            if (mIDataResponse!=null)
                mIDataResponse.onSaveTempDatas(animalHeatDataArrayList);
            animalHeatDataArrayList = null;
            dataType = 0;
            LogUtil.getInstance().logd("DATA******","实时体温数据接受结束 = "+temp);
        }else if (type == 0x19){//总计步
            if (stepOnDayDataArrayList==null)
                stepOnDayDataArrayList = new ArrayList<>();
            long timeOfDay = DateUtil.getTimeScopeForDay(time);
            int step = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);
            int distence = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            int calorie = (data[8] & 0xff) + ((data[9] & 0xFF) << 8) + ((data[10] & 0xff) << 16) + ((data[11] & 0xFF) << 24);
            stepOnDayDataArrayList.add(new StepData(timeOfDay,step,distence*10,calorie,null));
            if (mIDataResponse!=null)
                mIDataResponse.onSaveDayStepDatas(stepOnDayDataArrayList);
            stepOnDayDataArrayList = null;
            dataType = 0;
            LogUtil.getInstance().logd("DATA******","总计步接受结束 step= "+step+" ;distance = "+distence+" ;calorie = "+calorie);
        }else if (type == 0x20){//用户设置
            int height = ((data[0] & 0xff) + ((data[1] & 0xFF) << 8));
            int weight = ((data[2] & 0xff) + ((data[3] & 0xFF) << 8));
            int stepPlan = ((data[4] & 0xff) + ((data[5] & 0xFF) << 8));
            int sex = ((data[7] & 0xff));
            int heightBritish = ((data[8] & 0xff) + ((data[9] & 0xFF) << 8));
            int weightBritish = ((data[10] & 0xff) + ((data[11] & 0xFF) << 8));
            int unit = ((data[12] & 0xff));
            int tempUnit = ((data[13] & 0xff));
            UserModel userModel = new UserModel(height,weight,
                    unit,heightBritish,weightBritish,
                    stepPlan,tempUnit,sex);
            if (mIDataResponse!=null)
                mIDataResponse.updateUserInfo(userModel);
        }else if (type==0x21){//羽毛球
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 9;
            sportData.time = time;
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.calorie = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);

            int longLenght = (data[16] & 0xff) + ((data[17] & 0xFF) << 8);
            int latLenght = (data[16+2+longLenght] & 0xff) + ((data[17+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[16+4+longLenght+latLenght] & 0xff) + ((data[17+4+longLenght+latLenght] & 0xFF) << 8);

            int heartLenght = (data[16+6+longLenght+latLenght+speedPerHourLenght] & 0xff) +
                    ((data[17+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,16+8+longLenght+latLenght+speedPerHourLenght,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的羽毛球数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","羽毛球数据接受结束");
            }
        }else if (type==0x22){//篮球
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 10;
            sportData.time = time;
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.calorie = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);

            int longLenght = (data[16] & 0xff) + ((data[17] & 0xFF) << 8);
            int latLenght = (data[16+2+longLenght] & 0xff) + ((data[17+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[16+4+longLenght+latLenght] & 0xff) + ((data[17+4+longLenght+latLenght] & 0xFF) << 8);

            int heartLenght = (data[16+6+longLenght+latLenght+speedPerHourLenght] & 0xff) +
                    ((data[17+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,16+8+longLenght+latLenght+speedPerHourLenght,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的篮球数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","篮球数据接受结束");
            }
        }else if (type==0x23){//足球
            if(sportDataArrayList==null)
                sportDataArrayList = new ArrayList<>();
            HashMap<Integer,String> dataHash;
            SportData sportData = new SportData();
            sportData.type = 17;
            sportData.time = time;
            sportData.sportTime = (data[4] & 0xff) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xff) << 16) + ((data[7] & 0xFF) << 24);
            sportData.calorie = (data[0] & 0xff) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xFF) << 24);

            int longLenght = (data[16] & 0xff) + ((data[17] & 0xFF) << 8);
            int latLenght = (data[16+2+longLenght] & 0xff) + ((data[17+2+longLenght] & 0xFF) << 8);
            int speedPerHourLenght = (data[16+4+longLenght+latLenght] & 0xff) + ((data[17+4+longLenght+latLenght] & 0xFF) << 8);

            int heartLenght = (data[16+6+longLenght+latLenght+speedPerHourLenght] & 0xff) +
                    ((data[17+6+longLenght+latLenght+speedPerHourLenght] & 0xFF) << 8);
            byte[] heartDatas = new byte[heartLenght];
            if (heartLenght!=0)
                System.arraycopy(data,16+8+longLenght+latLenght+speedPerHourLenght,
                        heartDatas,0,heartLenght);
            dataHash = CommandUtil.getAverage(heartDatas,1);
            for (int key:dataHash.keySet()){
                sportData.heart = key;
                sportData.heartArray = dataHash.get(key);
            }
            sportDataArrayList.add(sportData);
            LogUtil.getInstance().logd("DATA******","解析到的足球数据 : "+time+" ;运动时长 = "+sportData.sportTime+" ;卡路里 = "+sportData.calorie+
                    " ;平均心率 = "+sportData.heart+" ;心率数组 = "+sportData.heartArray);

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveRunDatas(sportDataArrayList);
                sportDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","足球数据接受结束");
            }
        }else if (type == 0x24){//实时血氧
            if (bloodOxygenDataArrayList==null)
                bloodOxygenDataArrayList = new ArrayList<>();
            long timeOfDay = time;
            int bloodOxy = (data[0] & 0xff);
            bloodOxygenDataArrayList.add(new BloodOxygenData(timeOfDay,bloodOxy));
            if (mIDataResponse!=null)
                mIDataResponse.onSaveBloodOxygenDatas(bloodOxygenDataArrayList);
            bloodOxygenDataArrayList = null;
            dataType = 0;
            LogUtil.getInstance().logd("DATA******","实时血氧数据接收 time= "+timeOfDay+" ;data = "+bloodOxy);
        }else if (type == 0x25){//体温
            if (animalHeatDataArrayList==null)
                animalHeatDataArrayList = new ArrayList<>();
            int temp = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);
            if (temp!=0)
                animalHeatDataArrayList.add(new AnimalHeatData(time,temp));
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveTempDatas(animalHeatDataArrayList);
                animalHeatDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","总计体温数据接收 time= "+time+" ;data = "+temp);
            }
        }else if (type == 0x26){//血压
            if (bloodPressureDataArrayList==null)
                bloodPressureDataArrayList = new ArrayList<>();
            int sbp = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);
            int dbp = (data[2] & 0xff) + ((data[3] & 0xFF) << 8);
            if (sbp!=0&&sbp!=0)
                bloodPressureDataArrayList.add(new BloodPressureData(time,sbp,dbp));
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveBpDatas(bloodPressureDataArrayList);
                bloodPressureDataArrayList = null;
                dataType = 0;

            }
        }else if (type == 0x27){//血氧
            if (bloodOxygenDataArrayList==null)
                bloodOxygenDataArrayList = new ArrayList<>();
            int spo = (data[0] & 0xff);
            if (spo!=0)
                bloodOxygenDataArrayList.add(new BloodOxygenData(time,spo));
            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveBloodOxygenDatas(bloodOxygenDataArrayList);
                bloodOxygenDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","总计血氧数据接收 time= "+time+" ;data = "+spo);
            }
        }else if (type == 0x28){//实时血压
            if (bloodPressureDataArrayList==null)
                bloodPressureDataArrayList = new ArrayList<>();
            int sbp = (data[0] & 0xff) + ((data[1] & 0xFF) << 8);
            int dbp = (data[2] & 0xff) + ((data[3] & 0xFF) << 8);
            if (sbp!=0&&sbp!=0)
                bloodPressureDataArrayList.add(new BloodPressureData(time,sbp,dbp));
            if (mIDataResponse!=null)
                mIDataResponse.onSaveBpDatas(bloodPressureDataArrayList);
            bloodOxygenDataArrayList = null;
            dataType = 0;
            LogUtil.getInstance().logd("DATA******","总计血压数据接收 time= "+time+" ;sbp = "+sbp+ " ;dbp = "+dbp);
        }else if (type==0x32){//数据标示
            int deviceNum = (data[1]&0xff)<<8|(data[0]&0xff)&0x0ffff;
            if (deviceNum==8){
                return;
            }else {
                ArrayList<Integer> datas = new ArrayList<>();
                for (int i = 2;i<data.length-12;i++){
                    if (data[i]!=0){
                        datas.add(i-1);
                    }
                }
                if (data[19]!=0)
                    datas.add(0x14);

                //todo 调起经典蓝牙配对
                if (data.length>21){
                    mIDataResponse.pairBluetooth(String.format("%02X:%02X:%02X:%02X:%02X:%02X",data[26],data[25], data[24],data[23],
                            data[22],data[21]));
                }
                if (data.length>27){
                    for (int i = 0;i<3;i++){
                        if (data[27+i]!=0){
                            datas.add(0x21+i);
                        }
                    }
                }
                if (data.length>30&&data[30]==1){
                    datas.add(0x27);
                }

                if (data.length>31&&data[31]==1){
                    datas.add(0x25);
                }

                if (data.length>32&&data[32]==1){
                    datas.add(0x26);
                }
                datas.add(0x19);
                if (mIDataResponse!=null)
                    mIDataResponse.onGetDataIndex(deviceNum+"",datas);
            }

        }else if (type == 0x55){//获取日程列表
            if(scheduleDataArrayList==null)
                scheduleDataArrayList = new ArrayList<>();
            ScheduleData scheduleData = new ScheduleData();
            scheduleData.setIndex(data[0]);
            scheduleData.setType(data[1]);
            scheduleData.setTime((data[2] & 0xff) + ((data[3] & 0xFF) << 8) + ((data[4] & 0xff) << 16) + ((data[5] & 0xFF) << 24));
            int strLength = (data[6] & 0xff) + ((data[7] & 0xFF) << 8);
            byte strs[] = new byte[strLength];
            System.arraycopy(data,8, strs,0,strLength);
            String value = DateUtil.byteToHexString(strs);
            try {
                scheduleData.setMsg(new String(strs,"UnicodeBigUnmarked"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            scheduleDataArrayList.add(scheduleData);
            LogUtil.getInstance().logd("DATA******","解析到的计划表 : "+time+" ;时间 = "+scheduleData.getTime()+" ;消息 = "+scheduleData.getMsg()+
                    " ;序号 = "+scheduleData.getIndex());

            if (isEnd){
                if (mIDataResponse!=null)
                    mIDataResponse.onSaveScheduleData(scheduleDataArrayList);
                scheduleDataArrayList = null;
                dataType = 0;
                LogUtil.getInstance().logd("DATA******","计划表同步结束");
            }
        }
    }

    /**
     * 出现沾包的情况数据无法及时存储时调用此方法存储
     * */
    private void saveData(int type){
        if (type == 0x01){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveStepDatas(stepDataArrayList);
            stepDataArrayList = null;
            timeOfdata = 0;
            LogUtil.getInstance().logd("DATA******","计步数据接受结束");
        }else if (type == 0x02){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveHeartDatas(heartDataArrayList);
            heartDataArrayList = null;
            timeOfdata = 0;
            LogUtil.getInstance().logd("DATA******","心率数据接受结束");
        }else if (type == 0x03){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveSleepDatas(sleepDataArrayList);
            sleepDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","睡眠数据接受结束");
        }else if (type == 0x04){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","跑步数据接受结束");
        }else if (type == 0x05){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","徒步数据接受结束");
        }else if (type == 0x06){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","马拉松数据接受结束");
        }else if (type == 0x08){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","游泳数据接受结束");
        }else if (type == 0x09){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","攀岩数据接受结束");
        }else if (type == 0x0a){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","滑雪数据接受结束");
        }else if (type == 0x0b){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","骑行数据接受结束");
        }else if (type == 0x0c){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","划船数据接受结束");
        }else if (type == 0x0e){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","登山数据接受结束");
        }else if (type == 0x14){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","跑步机数据接受结束");
        }else if (type == 0x18){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveTempDatas(animalHeatDataArrayList);
            animalHeatDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","体温数据接受结束");
        }else if (type == 0x19){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveDayStepDatas(stepOnDayDataArrayList);
            stepOnDayDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","总计步数据接受结束");
        }else if (type == 0x21){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","羽毛球数据接受结束");
        }else if (type == 0x22){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","篮球数据接受结束");
        }else if (type == 0x23){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveRunDatas(sportDataArrayList);
            sportDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","足球数据接受结束");
        }else if (type == 0x25){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveBloodOxygenDatas(bloodOxygenDataArrayList);
            bloodOxygenDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","体温数据接受结束");
        }else if (type == 0x26){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveBloodOxygenDatas(bloodOxygenDataArrayList);
            bloodOxygenDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","血压数据接受结束");
        }else if (type == 0x27){
            if (mIDataResponse!=null)
                mIDataResponse.onSaveBloodOxygenDatas(bloodOxygenDataArrayList);
            bloodOxygenDataArrayList = null;
            LogUtil.getInstance().logd("DATA******","血氧数据接受结束");
        }
    }


}
