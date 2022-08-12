package com.szip.healthy.Activity.bp;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BpDayPresenterImpl implements IBpPresenter{
    private IBpView iBpView;
    private Context mContext;

    public BpDayPresenterImpl(Context mContext,IBpView iBpView) {
        this.iBpView = iBpView;
        this.mContext = mContext;
    }

    @Override
    public void loadData(long time) {
        List<BloodPressureData> bloodPressureDataList = LoadDataUtil.newInstance().getBloodPressureWithDay(time);
        if (bloodPressureDataList.size()==0)
            return;

        int sum = 0;
        int sumHour = 0;
        int allSbp = 0;
        int allDbp = 0;
        int sbpAbnormal = 0;
        int dbpAbnormal = 0;
        HashMap<Integer,Integer> sbpHashMap = new HashMap<>();
        HashMap<Integer,Integer> dbpHashMap = new HashMap<>();
        List<ReportData> reportDataList = new ArrayList<>();
        for (int i = 0;i<24;i++){
            reportDataList.add(new ReportData(0,time+i*60*60));
        }

        for (int i = 0;i < bloodPressureDataList.size();i++){
            BloodPressureData bloodPressureData = bloodPressureDataList.get(i);
            if (bloodPressureData.sbpDate==0)
                continue;
            if (bloodPressureData.sbpDate>140)
                sbpAbnormal++;
            if (bloodPressureData.dbpDate>90)
                dbpAbnormal++;
            sum++;
            allSbp+=bloodPressureData.sbpDate;
            allDbp+=bloodPressureData.dbpDate;

            //把列表整理成24小时个区间
            int index = DateUtil.getHour(bloodPressureDataList.get(i).getTime());
            int sbpValue = bloodPressureDataList.get(i).sbpDate;
            int dbpValue = bloodPressureDataList.get(i).dbpDate;
            if (i==0){
                sbpHashMap.put(index,sbpValue);
                dbpHashMap.put(index,dbpValue);
            }else if (sbpHashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(bloodPressureDataList.get(i-1).getTime());
                int oldSbpValue = sbpHashMap.get(oldIndex);
                int oldDbpValue = dbpHashMap.get(oldIndex);
                sbpHashMap.put(oldIndex,oldSbpValue/sumHour);
                dbpHashMap.put(oldIndex,oldDbpValue/sumHour);
                sumHour = 0;
                sbpHashMap.put(index,sbpValue);
                dbpHashMap.put(index,dbpValue);
            }else {
                sbpHashMap.put(index,sbpValue+sbpHashMap.get(index));
                dbpHashMap.put(index,dbpValue+dbpHashMap.get(index));
            }
            sumHour++;

            if (i == bloodPressureDataList.size()-1){
                sbpHashMap.put(index,sbpHashMap.get(index)/sumHour);
                dbpHashMap.put(index,dbpHashMap.get(index)/sumHour);
            }
        }

        for (int key : sbpHashMap.keySet()){
            ReportData data = new ReportData(sbpHashMap.get(key),dbpHashMap.get(key),time+key*60*60);
            reportDataList.set(key,data);
        }

        List<ReportInfoData> list = new ArrayList<>();
        for (BloodPressureData bloodPressureData:bloodPressureDataList){
            list.add(new ReportInfoData(String.format("%d/%d mmhg",bloodPressureData.sbpDate,bloodPressureData.dbpDate),bloodPressureData.time));
        }
        if (iBpView!=null){
            iBpView.updateListView(list);
            iBpView.updateTable(reportDataList);
            String averageSbpStr = String.format(Locale.ENGLISH,"%dmmhg",allSbp/sum);
            String averageDbpStr = String.format(Locale.ENGLISH,"%dmmhg",allDbp/sum);
            String abnormalSbpStr = String.format(Locale.ENGLISH,mContext.getString(R.string.healthy_unit),sbpAbnormal);
            String abnormalDbpStr = String.format(Locale.ENGLISH,mContext.getString(R.string.healthy_unit),dbpAbnormal);
            iBpView.updateView(averageSbpStr,averageDbpStr,abnormalSbpStr,abnormalDbpStr);
        }
    }

    @Override
    public void register(IBpView iBpView) {
        this.iBpView = iBpView;
    }

    @Override
    public void unRegister() {
        this.iBpView = null;
    }
}
