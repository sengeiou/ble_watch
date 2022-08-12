package com.szip.healthy.Activity.spo;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BloodOxygenDayPresenterImpl implements IBloodOxygenPresenter{

    private Context mContext;
    private IBloodOxygenView iBloodOxygenView;

    public BloodOxygenDayPresenterImpl(Context mContext, IBloodOxygenView iBloodOxygenView) {
        this.mContext = mContext;
        this.iBloodOxygenView = iBloodOxygenView;
    }

    @Override
    public void loadData(long time) {
        List<BloodOxygenData> bloodOxygenDataList = LoadDataUtil.newInstance().getBloodOxygenWithDay(time);
        if (bloodOxygenDataList.size()==0)
            return;
        int sum = 0;
        int sumHour = 0;
        int allBloodOxygen = 0;
        int abnormal = 0;
        HashMap<Integer,Integer> hashMap = new HashMap<>();
        List<ReportData> reportDataList = new ArrayList<>();
        for (int i = 0;i<24;i++){
            reportDataList.add(new ReportData(0,time+i*60*60));
        }

        for (int i = 0;i < bloodOxygenDataList.size();i++){
            BloodOxygenData bloodOxygenData = bloodOxygenDataList.get(i);
            if (bloodOxygenData.bloodOxygenData==0)
                continue;
            if (bloodOxygenData.bloodOxygenData<95)
                abnormal++;
            sum++;
            allBloodOxygen+=bloodOxygenData.bloodOxygenData;

            //把血氧列表整理成24小时个区间
            int index = DateUtil.getHour(bloodOxygenDataList.get(i).getTime());
            int value = bloodOxygenDataList.get(i).bloodOxygenData;
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(bloodOxygenDataList.get(i-1).getTime());
                int oldValue = hashMap.get(oldIndex);
                hashMap.put(oldIndex,oldValue/sumHour);
                sumHour = 0;
                hashMap.put(index,value);
            }else {
                hashMap.put(index,value+hashMap.get(index));
            }
            sumHour++;

            if (i == bloodOxygenDataList.size()-1){
                hashMap.put(index,hashMap.get(index)/sumHour);
            }
        }

        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            ReportData data = new ReportData(entry.getValue(),time+entry.getKey()*60*60);
            reportDataList.set(entry.getKey(),data);
        }

        List<ReportInfoData> list = new ArrayList<>();
        for (BloodOxygenData bloodOxygenData:bloodOxygenDataList){
            list.add(new ReportInfoData(String.format("%d%%",bloodOxygenData.bloodOxygenData),bloodOxygenData.time));
        }
        if (iBloodOxygenView!=null){
            iBloodOxygenView.updateList(list);
            Collections.sort(bloodOxygenDataList);
            int min = bloodOxygenDataList.get(0).bloodOxygenData;
            int max = bloodOxygenDataList.get(bloodOxygenDataList.size()-1).bloodOxygenData;
            iBloodOxygenView.updateTable(reportDataList);
            String maxStr = String.format(Locale.ENGLISH,"%d%%",max);
            String minStr = String.format(Locale.ENGLISH,"%d%%",min);
            String averageStr = String.format(Locale.ENGLISH,"%d%%",allBloodOxygen/sum);
            String abnormalStr = String.format(Locale.ENGLISH,mContext.getString(R.string.healthy_unit),abnormal);
            iBloodOxygenView.updateView(averageStr,maxStr,minStr,abnormalStr);
        }
    }

    @Override
    public void register(IBloodOxygenView iBloodOxygenView) {
        this.iBloodOxygenView = iBloodOxygenView;
    }

    @Override
    public void unRegister() {
        this.iBloodOxygenView = null;
    }
}
