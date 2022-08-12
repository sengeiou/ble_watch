package com.szip.healthy.Activity.bp;

import android.content.Context;

import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BpWeekPresenterImpl implements IBpPresenter{
    private IBpView iBpView;
    private Context mContext;

    public BpWeekPresenterImpl(Context mContext, IBpView iBpView) {
        this.iBpView = iBpView;
        this.mContext = mContext;
    }

    @Override
    public void loadData(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_WEEK,1);
        time = calendar.getTimeInMillis()/1000;
        List<ReportData> reportDataList = new ArrayList<>();
        int allSum = 0;
        int allSbp = 0;
        int allDbp = 0;
        int sbpAbnormal = 0;
        int dbpAbnormal = 0;
        for (int i = 0;i<7;i++){
            List<BloodPressureData> bloodPressureDataList = LoadDataUtil.newInstance().getBloodPressureWithDay(time+i*24*60*60);

            if (bloodPressureDataList.size() == 0){
                reportDataList.add(new ReportData(0,0,0,0,0,0,time+i*24*60*60));
                continue;
            }
            int sbp = 0;
            int dbp = 0;
            int sum = 0;
            int minSbp = 0;
            int maxSbp = 0;
            int minDbp = 0;
            int maxDbp = 0;
            for (int a = 0;a<bloodPressureDataList.size();a++){
                BloodPressureData bloodPressureData = bloodPressureDataList.get(a);
                if (bloodPressureData.sbpDate>140)
                    sbpAbnormal++;
                if (bloodPressureData.dbpDate>90)
                    dbpAbnormal++;
                sum++;
                allSum++;
                sbp+=bloodPressureData.sbpDate;
                allSbp+=bloodPressureData.sbpDate;
                dbp+=bloodPressureData.dbpDate;
                allDbp+=bloodPressureData.dbpDate;
            }
            Collections.sort(bloodPressureDataList);
            minSbp = bloodPressureDataList.get(0).sbpDate;
            maxSbp = bloodPressureDataList.get(bloodPressureDataList.size()-1).sbpDate;
            minDbp = bloodPressureDataList.get(0).dbpDate;
            maxDbp = bloodPressureDataList.get(bloodPressureDataList.size()-1).dbpDate;
            reportDataList.add(new ReportData(sbp/sum,maxSbp,minSbp,dbp/sum,maxDbp,minDbp,time+i*24*60*60));
        }
        if (iBpView!=null){
            iBpView.updateTable(reportDataList);
            String averageSbpStr = String.format(Locale.ENGLISH,"%dmmhg",allSum==0?0:allSbp/allSum);
            String averageDbpStr = String.format(Locale.ENGLISH,"%dmmhg",allSum==0?0:allDbp/allSum);
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
