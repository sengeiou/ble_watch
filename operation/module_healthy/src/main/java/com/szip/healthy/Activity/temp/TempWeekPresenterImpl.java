package com.szip.healthy.Activity.temp;

import android.content.Context;

import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TempWeekPresenterImpl implements ITempPresenter{

    private ITempView iTempView;
    private Context mContext;

    public TempWeekPresenterImpl(Context mContext, ITempView iTempView) {
        this.iTempView = iTempView;
        this.mContext = mContext;
    }

    @Override
    public void loadData(long time) {
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_WEEK,1);
        time = calendar.getTimeInMillis()/1000;
        List<ReportData> reportDataList = new ArrayList<>();
        int allMax = 0;
        int allMin = 1000;
        int allSum = 0;
        int allTemp = 0;
        int abnormal = 0;
        for (int i = 0;i<7;i++){
            List<AnimalHeatData> animalHeatDataList = LoadDataUtil.newInstance().getAnimalHeatWithDay(time+i*24*60*60);
            if (animalHeatDataList.size() == 0){
                reportDataList.add(new ReportData(0,0,0,time+i*24*60*60));
                continue;
            }
            int temp = 0;
            int sum = 0;
            int min = 0;
            int max = 0;
            for (int a = 0;a<animalHeatDataList.size();a++){
                AnimalHeatData animalHeatData = animalHeatDataList.get(a);
                if (animalHeatData.tempData>374)
                    abnormal++;
                sum++;
                allSum++;
                temp+=animalHeatData.tempData;
                allTemp+=animalHeatData.tempData;
            }
            Collections.sort(animalHeatDataList);
            min = animalHeatDataList.get(0).tempData;
            max = animalHeatDataList.get(animalHeatDataList.size()-1).tempData;

            if (max>allMax)
                allMax = max;
            if (min<allMin)
                allMin = min;
            reportDataList.add(new ReportData(temp/sum,max,min,time+i*24*60*60));
        }
        if (iTempView!=null){
            if (allMin==1000)
                allMin = 0;
            iTempView.updateTable(reportDataList);
            String maxStr,minStr,averageStr;
            if (userModel.tempUnit==0){
                maxStr = String.format(Locale.ENGLISH,"%.1f ℃",allMax/10f);
                minStr = String.format(Locale.ENGLISH,"%.1f ℃",allMin/10f);
                averageStr = String.format(Locale.ENGLISH,"%.1f ℃",allSum==0?0:allTemp/allSum/10f);
            }else {
                maxStr = String.format(Locale.ENGLISH,"%.1f ℉", MathUtil.newInstance().c2f(allMax/10f));
                minStr = String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(allMin/10f));
                averageStr = String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(allSum==0?0:allTemp/allSum/10f));
            }
            String abnormalStr = String.format(Locale.ENGLISH,mContext.getString(R.string.healthy_unit),abnormal);
            iTempView.updateView(averageStr,maxStr,minStr,abnormalStr);
        }
    }

    @Override
    public void register(ITempView iTempView) {
        this.iTempView = iTempView;
    }

    @Override
    public void unRegister() {
        this.iTempView = null;
    }
}
