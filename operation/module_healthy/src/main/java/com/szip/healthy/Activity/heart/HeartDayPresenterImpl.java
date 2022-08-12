package com.szip.healthy.Activity.heart;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.healthy.Model.ReportData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HeartDayPresenterImpl implements IHeartPresenter{

    private IHeartView  iHeartView;
    private Context mContext;

    public HeartDayPresenterImpl( Context mContext,IHeartView iHeartView) {
        this.iHeartView = iHeartView;
        this.mContext = mContext;
    }

    @Override
    public void loadData(long time) {
        List<HeartData> heartDataList = LoadDataUtil.newInstance().getHeartWithDay(time);
        if (heartDataList.size()==0)
            return;

        int level[] = new int[6];
        int sum = 0;
        int sumHour = 0;
        int allHeart = 0;
        HashMap<Integer,Integer> hashMap = new HashMap<>();
        List<ReportData> reportDataList = new ArrayList<>();
        for (int i = 0;i<24;i++){
            reportDataList.add(new ReportData(0,time+i*60*60));
        }
        for (int i = 0;i < heartDataList.size();i++){
            HeartData heartData = heartDataList.get(i);
            if (heartData.averageHeart==0)
                continue;

            if (heartData.averageHeart>=161)
                level[0]++;
            else if (heartData.averageHeart>=149)
                level[1]++;
            else if (heartData.averageHeart>=131)
                level[2]++;
            else if (heartData.averageHeart>=112)
                level[3]++;
            else if (heartData.averageHeart>=90)
                level[4]++;
            else
                level[5]++;
            sum++;
            allHeart+=heartData.averageHeart;

            //把心率列表整理成24小时个区间
            int index = DateUtil.getHour(heartDataList.get(i).getTime());
            int value = heartDataList.get(i).averageHeart;
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(heartDataList.get(i-1).getTime());
                int oldValue = hashMap.get(oldIndex);
                hashMap.put(oldIndex,oldValue/sumHour);
                sumHour = 0;
                hashMap.put(index,value);
            }else {
                hashMap.put(index,value+hashMap.get(index));
            }
            sumHour++;

            if (i == heartDataList.size()-1){
                hashMap.put(index,hashMap.get(index)/sumHour);
            }
        }

        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            ReportData data = new ReportData(entry.getValue(),time+entry.getKey()*60*60);
            reportDataList.set(entry.getKey(),data);
        }

        Collections.sort(heartDataList);
        int min = heartDataList.get(0).averageHeart;
        int max = heartDataList.get(heartDataList.size()-1).averageHeart;

        if (iHeartView!=null){
            iHeartView.updateTable(reportDataList,max);
            String maxStr = String.format(Locale.ENGLISH,"%d Bpm",max);
            String minStr = String.format(Locale.ENGLISH,"%d Bpm",min);
            String averageStr = String.format(Locale.ENGLISH,"%d Bpm",allHeart/sum);
            iHeartView.updateView(averageStr,maxStr,minStr);
            iHeartView.updateHeartLevel(level,heartDataList.size());
        }

    }

    @Override
    public void register(IHeartView iHeartView) {
        this.iHeartView = iHeartView;
    }

    @Override
    public void unRegister() {
        this.iHeartView = null;
    }
}
