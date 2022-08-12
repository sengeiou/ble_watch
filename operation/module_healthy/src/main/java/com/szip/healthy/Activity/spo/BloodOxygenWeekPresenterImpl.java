package com.szip.healthy.Activity.spo;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BloodOxygenWeekPresenterImpl implements IBloodOxygenPresenter{

    private Context mContext;
    private IBloodOxygenView iBloodOxygenView;

    public BloodOxygenWeekPresenterImpl(Context mContext, IBloodOxygenView iBloodOxygenView) {
        this.mContext = mContext;
        this.iBloodOxygenView = iBloodOxygenView;
    }

    @Override
    public void loadData(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_WEEK,1);
        time = calendar.getTimeInMillis()/1000;
        List<ReportData> reportDataList = new ArrayList<>();
        int allMax = 0;
        int allMin = 1000;
        int allSum = 0;
        int allBloodOxygen = 0;
        int abnormal = 0;
        for (int i = 0;i<7;i++){
            List<BloodOxygenData> bloodOxygenDataList = LoadDataUtil.newInstance().getBloodOxygenWithDay(time+i*24*60*60);
            if (bloodOxygenDataList.size() == 0){
                reportDataList.add(new ReportData(0,0,0,time+i*24*60*60));
                continue;
            }
            int bloodOxygen = 0;
            int sum = 0;
            int min = 0;
            int max = 0;
            for (int a = 0;a<bloodOxygenDataList.size();a++){
                BloodOxygenData bloodOxygenData = bloodOxygenDataList.get(a);
                if (bloodOxygenData.bloodOxygenData<95)
                    abnormal++;
                sum++;
                allSum++;
                bloodOxygen+=bloodOxygenData.bloodOxygenData;
                allBloodOxygen+=bloodOxygenData.bloodOxygenData;
            }
            Collections.sort(bloodOxygenDataList);
            min = bloodOxygenDataList.get(0).bloodOxygenData;
            max = bloodOxygenDataList.get(bloodOxygenDataList.size()-1).bloodOxygenData;

            if (max>allMax)
                allMax = max;
            if (min<allMin)
                allMin = min;
            reportDataList.add(new ReportData(bloodOxygen/sum,max,min,time+i*24*60*60));
        }
        if (iBloodOxygenView!=null){
            iBloodOxygenView.updateTable(reportDataList);
            String maxStr = String.format(Locale.ENGLISH,"%d%%",allMax);
            String minStr = String.format(Locale.ENGLISH,"%d%%",allMin==1000?0:allMin);
            String averageStr = String.format(Locale.ENGLISH,"%d%%",allSum==0?0:allBloodOxygen/allSum);
            String smallStr = String.format(Locale.ENGLISH,mContext.getString(R.string.healthy_unit),abnormal);
            iBloodOxygenView.updateView(averageStr,maxStr,minStr,smallStr);
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
