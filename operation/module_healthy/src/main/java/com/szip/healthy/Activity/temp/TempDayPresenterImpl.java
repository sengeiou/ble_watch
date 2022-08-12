package com.szip.healthy.Activity.temp;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TempDayPresenterImpl implements ITempPresenter{

    private ITempView iTempView;
    private Context mContext;

    public TempDayPresenterImpl(Context mContext,ITempView iTempView) {
        this.iTempView = iTempView;
        this.mContext = mContext;
    }

    @Override
    public void loadData(long time) {
        List<AnimalHeatData> animalHeatDataList = LoadDataUtil.newInstance().getAnimalHeatWithDay(time);
        if (animalHeatDataList.size()==0)
            return;

        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;

        int sum = 0;
        int sumHour = 0;
        int allAnimalHeat = 0;
        int abnormal = 0;
        HashMap<Integer,Integer> hashMap = new HashMap<>();
        List<ReportData> reportDataList = new ArrayList<>();
        for (int i = 0;i<24;i++){
            reportDataList.add(new ReportData(0,time+i*60*60));
        }

        for (int i = 0;i < animalHeatDataList.size();i++){
            AnimalHeatData animalHeatData = animalHeatDataList.get(i);
            if (animalHeatData.tempData==0)
                continue;
            if (animalHeatData.tempData>374)
                abnormal++;
            sum++;
            allAnimalHeat+=animalHeatData.tempData;

            //把血氧列表整理成24小时个区间
            int index = DateUtil.getHour(animalHeatDataList.get(i).getTime());
            int value = animalHeatDataList.get(i).tempData;
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(animalHeatDataList.get(i-1).getTime());
                int oldValue = hashMap.get(oldIndex);
                hashMap.put(oldIndex,oldValue/sumHour);
                sumHour = 0;
                hashMap.put(index,value);
            }else {
                hashMap.put(index,value+hashMap.get(index));
            }
            sumHour++;

            if (i == animalHeatDataList.size()-1){
                hashMap.put(index,hashMap.get(index)/sumHour);
            }
        }

        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            ReportData data = new ReportData(entry.getValue(),time+entry.getKey()*60*60);
            reportDataList.set(entry.getKey(),data);
        }

        List<ReportInfoData> list = new ArrayList<>();
        if (userModel.tempUnit==0){
            for (AnimalHeatData animalHeatData:animalHeatDataList){
                list.add(new ReportInfoData(String.format("%.1f℃",animalHeatData.tempData/10f),animalHeatData.time));
            }
        }else {
            for (AnimalHeatData animalHeatData:animalHeatDataList){
                list.add(new ReportInfoData(String.format("%.1f℉",MathUtil.newInstance().c2f(animalHeatData.tempData/10f)),animalHeatData.time));
            }
        }

        if (iTempView!=null){
            iTempView.updateList(list);
            Collections.sort(animalHeatDataList);
            int min = animalHeatDataList.get(0).tempData;
            int max = animalHeatDataList.get(animalHeatDataList.size()-1).tempData;
            iTempView.updateTable(reportDataList);
            String maxStr,minStr,averageStr;
            if (userModel.tempUnit==0){
                maxStr = String.format(Locale.ENGLISH,"%.1f ℃",max/10f);
                minStr = String.format(Locale.ENGLISH,"%.1f ℃",min/10f);
                averageStr = String.format(Locale.ENGLISH,"%.1f ℃",allAnimalHeat/sum/10f);
            }else {
                maxStr = String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(max/10f));
                minStr = String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(min/10f));
                averageStr = String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(allAnimalHeat/sum/10f));
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
