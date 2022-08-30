package com.szip.healthy.Activity.sleep;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SleepMonthPresenterImpl implements ISleepReportPresenter{
    private Context mContext;
    private ISleepReportView iSleepReportView;

    public SleepMonthPresenterImpl(Context mContext, ISleepReportView iSleepReportView) {
        this.mContext = mContext;
        this.iSleepReportView = iSleepReportView;
    }

    @Override
    public void loadData(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        time = calendar.getTimeInMillis()/1000;
        calendar.add(Calendar.MONTH,1);
        calendar.add(Calendar.HOUR_OF_DAY,-1);
        int dataSize = calendar.get(Calendar.DAY_OF_MONTH);

//        List<SleepData> sleepList = LoadDataUtil.newInstance().getSleepWithMonth(time,time+dataSize*24*60*60);
//        if (sleepList.size()==0)
//            return;
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;
        int sum = 0;
        int allDeep = 0;
        int allLight = 0;
        int allSleep = 0;
        int plan = 0;
        int max = 300;
        List<ReportData> reportDataList = new ArrayList<>();
        for (int i = 0;i<dataSize;i++){
            SleepData sleepData = LoadDataUtil.newInstance().getSleepWithDay(time+i*24*60*60);
            if (sleepData == null||(sleepData.deepTime+sleepData.lightTime)==0){
                reportDataList.add(new ReportData(0,time+i*24*60*60));
                continue;
            }
            ReportData reportData = new ReportData((sleepData.deepTime+sleepData.lightTime),0,sleepData.lightTime,sleepData.time);
            sum++;
            allDeep+=sleepData.deepTime;
            allLight+=sleepData.lightTime;
            allSleep+=(sleepData.deepTime+sleepData.lightTime);
            if ((sleepData.deepTime+sleepData.lightTime)>max)
                max = (sleepData.deepTime+sleepData.lightTime);
            if ((sleepData.deepTime+sleepData.lightTime)>userModel.sleepPlan)
                plan++;
            reportDataList.add(reportData);
        }

        if (iSleepReportView!=null){
            iSleepReportView.updateTableView(reportDataList,max,0);
            if (sum!=0){
                String allTime = String.format(Locale.ENGLISH,"%02dh%02dmin",allSleep/sum/60,allSleep/sum%60);
                String deepTime = String.format(Locale.ENGLISH,"%02dh%02dmin",allDeep/sum/60,allDeep/sum%60);
                String lightTime = String.format(Locale.ENGLISH,"%02dh%02dmin",allLight/sum/60,allLight/sum%60);
                String planStr = String.format(Locale.ENGLISH,"%.1f%%",(float)plan/dataSize*100);
                iSleepReportView.updateView(allTime,deepTime,lightTime,planStr);
            }
        }
    }

    @Override
    public void register(ISleepReportView iSleepReportView) {
        this.iSleepReportView = iSleepReportView;
    }

    @Override
    public void unRegister() {
        this.iSleepReportView = null;
    }
}
