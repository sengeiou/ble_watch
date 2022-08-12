package com.szip.sport.Activity.sportResult;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.sport.Model.SportResultDataModel;
import com.szip.sport.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SportResultPresenterImpl implements ISportResultPresenter{
    private SportData sportData;
    private Context mContext;
    private int unit = 0;
    private ISportResultView iSportResultView;

    public SportResultPresenterImpl(Context mContext, ISportResultView iSportResultView) {
        this.mContext = mContext;
        this.iSportResultView = iSportResultView;
    }

    @Override
    public void initSportData(SportData sportData) {
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;
        unit = userModel.unit;
        this.sportData = sportData;
        String type = MathUtil.newInstance().getSportType(sportData.type,mContext).getSportStr();
        String distance = "";
        if (sportData.distance!=0){
            if (unit==0){
                distance = String.format("<font color='#000000'><big><big><big>%.2f</big></big></big></font> km",
                        (sportData.distance+5)/10/100f);
            }else {
                distance = String.format("<font color='#000000'><big><big><big>%.2f</big></big></big></font> mile",
                        MathUtil.newInstance().km2Miles(sportData.distance/10));
            }
        }else {
            distance =  String.format("<font color='#000000'><big><big><big>%s</big></big></big></font>",
                    mContext.getString(R.string.sport_sport_info));
        }
        String time = DateUtil.getStringDateFromSecond(sportData.time,"yyyy/MM/dd HH:mm");
        List<SportResultDataModel> sportResultDataModels = getDataList();
        if (iSportResultView!=null)
            iSportResultView.updateSportView(distance,time,type,sportResultDataModels);

        if (sportData.latArray!=null&&sportData.latArray.split(",").length>=2)
            initMap();

    }

    private void initMap() {
       if (iSportResultView!=null)
           iSportResultView.updateLocation();
    }

    private List<SportResultDataModel> getDataList(){
        List<SportResultDataModel> list = new ArrayList<>();
        String data = String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                sportData.sportTime%3600/60,sportData.sportTime%3600%60);
        String unit = mContext.getString(R.string.sport_sport_time);
        list.add(new SportResultDataModel(unit,data));

        data = String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f);
        unit = mContext.getString(R.string.sport_sport_calorie)+"(kcal)";
        list.add(new SportResultDataModel(unit,data));

        if (sportData.step!=0){
            data = String.format(Locale.ENGLISH,"%d",sportData.step);
            unit = mContext.getString(R.string.sport_steps)+"(steps)";
            list.add(new SportResultDataModel(unit,data));
        }

        if (sportData.speed!=0){
            data = String.format(Locale.ENGLISH,"%02d'%02d''",sportData.speed/60,sportData.speed%60);
            unit = mContext.getString(R.string.sport_speed);
            list.add(new SportResultDataModel(unit,data));
            initSpeed();
        }

        if (sportData.speedPerHour!=0){
            if (this.unit==0){
                data = String.format(Locale.ENGLISH,"%.1f",sportData.speedPerHour/10f);
                unit = mContext.getString(R.string.sport_speed_hour)+"(km/h)";
            }else {
                data = String.format(Locale.ENGLISH,"%.1f",MathUtil.newInstance().kmPerHour2MilesPerHour(sportData.speedPerHour)/10f);
                unit = mContext.getString(R.string.sport_speed_hour)+"(mile/h)";
            }
            list.add(new SportResultDataModel(unit,data));
        }

        if (sportData.stride!=0){
            data = String.format(Locale.ENGLISH,"%d",sportData.stride);
            unit = mContext.getString(R.string.sport_stride)+"(steps/min)";
            list.add(new SportResultDataModel(unit,data));
            initStrideView();
        }
        if (sportData.heart!=0){
            data = String.format(Locale.ENGLISH,"%d",sportData.heart);
            unit = mContext.getString(R.string.sport_heart)+"(Bpm)";
            list.add(new SportResultDataModel(unit,data));
            initHeartView();
        }

        if (sportData.height!=0){
            data = String.format(Locale.ENGLISH,"%d",sportData.height);
            unit = mContext.getString(R.string.sport_height)+"(m)";
            list.add(new SportResultDataModel(unit,data));
            initAltitude();
        }

        return list;
    }

    private void initHeartView(){
        if (sportData.heartArray==null||sportData.heartArray.equals(""))
            return;
        String heartStr[] = sportData.heartArray.split(",");
        int heartLevel[] = new int[5];
        int heart[] = new int[heartStr.length];
        if (heart.length<=1)
            return;
        int level1 = 0,level2 = 0,level3 = 0,level4 = 0,level5 = 0;
        for (int i = 0;i<heartStr.length;i++){
            heart[i] = Integer.valueOf(heartStr[i]);
            if (heart[i]>=161)
                level1++;
            else if (heart[i]>=149)
                level2++;
            else if (heart[i]>=131)
                level3++;
            else if (heart[i]>=112)
                level4++;
            else
                level5++;
        }
        heartLevel[0] = level1*60;
        heartLevel[1] = level2*60;
        heartLevel[2] = level3*60;
        heartLevel[3] = level4*60;
        heartLevel[4] = level5*60;

        if (iSportResultView!=null)
            iSportResultView.updateHeartView(heart,heartLevel);

    }

    private void initStrideView(){
        if (sportData.strideArray==null||sportData.strideArray.equals(""))
            return;
        String[] stride = sportData.strideArray.split(",");
        if (stride.length<=1)
            return;
        int value[] = new int[stride.length];
        for (int i = 0;i<stride.length;i++){
            value[i] = Integer.valueOf(stride[i]);
        }
        if (iSportResultView!=null)
            iSportResultView.updateStrideView(value);
    }

    private void initAltitude(){
        if (sportData.altitudeArray==null||sportData.altitudeArray.equals(""))
            return;
        String[] altitude = sportData.altitudeArray.split(",");
        if (altitude.length<=1)
            return;
        int value[] = new int[altitude.length];
        for (int i = 0;i<altitude.length;i++){
            value[i] = Integer.valueOf(altitude[i]);
        }
        if (iSportResultView!=null)
            iSportResultView.updateAltitudeView(value);
    }

    private void initSpeed(){
        if (sportData.speedArray==null||sportData.speedArray.equals(""))
            return;
        String[] speeds = sportData.speedArray.split(",");
        if (iSportResultView!=null)
            iSportResultView.updateSpeedView(speeds);
    }
}
