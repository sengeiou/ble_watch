package com.szip.healthy.Activity.sleep;

import android.content.Context;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.healthy.Model.ReportData;

import java.util.ArrayList;
import java.util.List;

public class SleepDayPresenterImpl implements ISleepReportPresenter{
    private Context mContext;
    private ISleepReportView iSleepReportView;

    public SleepDayPresenterImpl(Context mContext, ISleepReportView iSleepReportView) {
        this.mContext = mContext;
        this.iSleepReportView = iSleepReportView;
    }

    @Override
    public void loadData(long time) {
        SleepData sleepData = LoadDataUtil.newInstance().getSleepWithDay(time);
        if (sleepData==null)
            return;

        if (sleepData.dataForHour!=null&&!sleepData.dataForHour.equals("")){
            String[] sleepList = sleepData.dataForHour.split(",");
            int deepTime = 0,lightTime = 0,allTime = 0,startTime;
            List<ReportData> reportDataList = new ArrayList<>();
            for (int i = 1;i<sleepList.length;i++){
                String state[] = sleepList[i].split(":");
                if (state[1].equals("2")){
                    deepTime += Integer.valueOf(state[0]);
                    reportDataList.add(new ReportData(2,Integer.valueOf(state[0])));
                }else {
                    lightTime += Integer.valueOf(state[0]);
                    reportDataList.add(new ReportData(1,Integer.valueOf(state[0])));
                }
            }
            allTime = lightTime+deepTime;
            startTime = DateUtil.getMinue(sleepList[0]);
            if (iSleepReportView!=null){
                iSleepReportView.updateProgressView(startTime,deepTime,lightTime);
                iSleepReportView.updateTableView(reportDataList,startTime,allTime);
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
