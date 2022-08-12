package com.szip.sport.Activity.gpsSport;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.szip.blewatch.base.Util.LocationUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.UserModel;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LOCATION_SERVICE;

public class StepPresenterImpl implements IGpsPresenter{
    private Context context;
    private IGpsView iGpsView;

    private Location preLocation;
    private LocationManager locationManager;

    private boolean firstTime = true;

    private int time = 0;//运动时长
    private int uiTime = 0;//上次更新UI的时间，用于定时更新ui
    private int speedTime = 0;//记录一公里配速的时长
    private float distance = 0;//两个点之间的距离
    private float preDistance = 0;//30秒之内移动的距离，用于计算平均速度
    private float calorie = 0;
    private int speed = 0,sportType;
    private long preTime = 0;//上一次定位的时间，用于计算两次定位之间的瞬时速度
    private float preDistance1 = 0;//整数公里数，用于计算1公里的配速
    private StringBuffer speedStr = new StringBuffer();
    private StringBuffer speedPerHour = new StringBuffer();
    private StringBuffer strideStr = new StringBuffer();
    private StringBuffer latStr = new StringBuffer();
    private StringBuffer lngStr = new StringBuffer();
    private double lat;
    private double lng;


    private Timer timer;
    private TimerTask timerTask;

    private SensorManager sensorManager;
    private Sensor stepCount;
    private float systemStep = -1;//系统计步器的步数（开机到现在手机走过的步数，运动开始时需要减去这个系统步数，就是我们实际步行的步数）
    private float acc;//定位的精确度，用于判断GPS信号
    private float step = 0;//实际走的步数
    private float preStep = 0;//缓存每30秒走过的步数，用于计算每30秒的步频；

    public StepPresenterImpl(Context context, Sensor stepCount,IGpsView iGpsView,int sportType) {
        this.context = context;
        this.iGpsView = iGpsView;
        this.locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        this.sensorManager =  (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sportType = sportType;
        this.stepCount = stepCount;
    }


    @Override
    public void startLocationService() {
        if (firstTime){
            if (iGpsView!=null)
                iGpsView.startCountDown();
            firstTime = false;
        }else {
            if (locationManager!=null) {
                LocationUtil.getInstance().getLocation(locationManager,false,myListener,locationListener,context);
                sensorManager.registerListener(sensorEventListener,stepCount,4000);
                initTask();
                timer.schedule(timerTask,0,1000);
                if (iGpsView!=null)
                    iGpsView.startRun();
            }
        }
    }

    @Override
    public void stopLocationService(){
        if(locationManager!=null){
            locationManager.removeUpdates(locationListener);
            if (timer!=null){
                timer.cancel();
                timerTask.cancel();
                timer = null;
                timerTask = null;
            }
            if (iGpsView!=null)
                iGpsView.stopRun();
        }
    }

    @Override
    public void finishLocationService(boolean isSave) {
        if(locationManager!=null){
            locationManager.removeUpdates(locationListener);
            sensorManager.unregisterListener(sensorEventListener);
            if (timer!=null){
                timer.cancel();
                timerTask.cancel();
            }
            if (isSave){
                if (distance-preDistance1>200){
                    speedStr.append(","+getInstantaneousSpeed((distance-preDistance1)/(time- speedTime)));
                }
                if (iGpsView!=null)
                    iGpsView.saveRun(getSportData());
            }
        }
    }

    @Override
    public void setViewDestory() {
        iGpsView = null;
    }

    private void initTask(){
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                time++;
                if (iGpsView!=null)
                    iGpsView.upDateTime(time);
                /**
                 * 每30秒采集一次平均速度的数据
                 * */
                if (time!=0&&time%30==0){
                    speedPerHour.append(String.format(",%d",(int)((distance-preDistance)/30f*3.6f*10)));
                    strideStr.append(String.format(",%d",getStride(step)));
                    preDistance = distance;
                }
            }
        };
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(LocationUtil.getInstance().getGaoLocation(location,context));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private GpsStatus.Listener myListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int i) {

        }
    };

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float data = event.values[0];
            if (timer!=null){
                if (systemStep == -1){
                    systemStep = data;
                    preTime =System.currentTimeMillis();
                }else {
                    float stepData = data-systemStep;
                    systemStep = data;
                    updataWithStep(stepData);
                }
            }else {
                systemStep = data;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    private void updateWithNewLocation(Location location) {
        LogUtil.getInstance().logd("data******","location = "+location.toString());
        if (location != null) {
            acc = location.getAccuracy();
            if (preLocation!=null){//非第一次获取经纬度
                if (time-uiTime>4&&location.getLatitude()!=0&&location.getLongitude()!=0){
                    latStr.append(String.format(",%d",(int)((location.getLatitude()-lat)*1000000)));//第二次开始的经纬度数据只保存相对第一次的增减量，不需要全部保存
                    lngStr.append(String.format(",%d",(int)((location.getLongitude()-lng)*1000000)));
                    uiTime = time;
                }
            }else {//第一次获取到经纬度
                lat = location.getLatitude();
                lng = location.getLongitude();
                latStr.append(String.format(",%d",(int)(lat*1000000)));//把第一组经纬度*1000000存到数据库备用
                lngStr.append(String.format(",%d",(int)(lng*1000000)));
                preLocation=location;
            }
            if (iGpsView!=null)
                iGpsView.updateLocation(location);
        }
    }

    private void updataWithStep(float stepData){
        step+=stepData;
        long subTime=(System.currentTimeMillis()- preTime)/1000;
        float d = 170*0.45f*stepData/100;
        float v = (subTime==0)?0:(d/subTime);
        speed = getInstantaneousSpeed(v);
        distance+=d;
        calorie+=getCalorie(d);
        if (iGpsView!=null)
            iGpsView.upDateRunData(speed,distance,calorie,acc);
//        if (mapFragment !=null&&!mapFragment.isHidden()){
//            if (context.getResources().getConfiguration().locale.getCountry().equals("CN"))
//                ((GaoDeMapFragment)mapFragment).setData(speed,distance,calorie,acc);
//            else
//                ((GoogleMapFragment)mapFragment).setData(speed,distance,calorie,acc);
//        }
        if (distance-preDistance1>1000){
            speedStr.append(String.format(",%d",time- speedTime));
            preDistance1+=1000;
            speedTime = time;
        }
        preTime =System.currentTimeMillis();
    }

    private int getStride(float step){
        /**
         *步频公式：step(30s)*2 = 一分钟步行的步数
         * k=身高（厘米）*0.45f/100
         */
        float data = (step-preStep)*2;
        preStep = step;
        return (int)data;
    }

    private float getCalorie(float d) {
        //卡路里公式：体重(千克)*1.036f*距离(千米)
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(context));
        if (userModel==null)
            return 0;
        float calorie = userModel.weight*1.036f*(d/1000);
        return calorie;
    }

    private int getInstantaneousSpeed(float v){
        //当前配速公式：1000/当前速度
        float speed = (v == 0)?0:(1000/v);
        return (int)speed;
    }

    private SportData getSportData(){
        SportData sportData = new SportData();
        if(time>30&&distance>100){//运动时长大于30秒并且运动里程超过200米，才能生成报告
            sportData.type = sportType;//运动类型
            sportData.time = System.currentTimeMillis()/1000;//运动时间（结束运动的时间）
            sportData.sportTime = time;//运动时长
            sportData.calorie = (int)(calorie*1000);//卡路里
            sportData.distance = (int)distance;//里程
            sportData.step = (int) step;
            if (!strideStr.toString().equals("")) {//步频数组/平均步频
                sportData.strideArray = strideStr.toString().substring(1);
                sportData.stride = getAverageData(sportData.strideArray);
            }
            if (!speedPerHour.toString().equals("")){//时速数组/平均时速
                sportData.speedPerHourArray = speedPerHour.toString().substring(1);
                sportData.speedPerHour = getAverageData(sportData.speedPerHourArray);
            }
            if (!speedStr.toString().equals("")){//配速数组/平均配速
                sportData.speedArray = speedStr.toString().substring(1);
                sportData.speed = getAverageData(sportData.speedArray);
            }
            if (!lngStr.toString().equals("")){//经度数组
                sportData.lngArray = lngStr.toString().substring(1);
            }
            if (!latStr.toString().equals("")){//纬度数组
                sportData.latArray = latStr.toString().substring(1);
            }
        }
        return sportData;
    }

    private int getAverageData(String data){
        int sum = 0;
        int allData = 0;
        String [] datas = data.split(",");
        for (String str:datas){
            if (Integer.valueOf(str)!=0){
                allData+=Integer.valueOf(str);
                sum++;
            }
        }
        if (sum!=0)
            return allData/sum;
        else
            return 0;
    }
}
