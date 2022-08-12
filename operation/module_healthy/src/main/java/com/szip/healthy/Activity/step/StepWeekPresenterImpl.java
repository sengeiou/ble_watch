package com.szip.healthy.Activity.step;

import android.content.Context;

import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StepWeekPresenterImpl implements IStepReportPresenter{

    private Context mContext;
    private IStepReportView iStepReportView;

    public StepWeekPresenterImpl(Context mContext, IStepReportView iStepReportView) {
        this.mContext = mContext;
        this.iStepReportView = iStepReportView;
    }

    @Override
    public void loadData(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_WEEK,1);
        time = calendar.getTimeInMillis()/1000;

        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;
        int sum = 0;
        int allStep = 0;
        int allCalorie = 0;
        int allDistance = 0;
        int plan = 0;
        int max = 5;
        List<ReportData> reportDataList = new ArrayList<>();
        for (int i = 0;i<7;i++){
            StepData stepData = LoadDataUtil.newInstance().getStepWithDay(time+i*24*60*60);
            if (stepData == null||stepData.steps==0){
                reportDataList.add(new ReportData(0,time+i*24*60*60));
                continue;
            }
            int step = stepData.steps;
            long stepTime = stepData.time;
            ReportData reportData = new ReportData(step,stepTime);
            sum++;
            allStep+=stepData.steps;
            allDistance+=stepData.distance;
            allCalorie+=stepData.calorie;
            if (stepData.steps>max)
                max = stepData.steps;
            if (stepData.steps>userModel.stepsPlan)
                plan++;
            reportDataList.add(reportData);

        }

        if (iStepReportView!=null){
            iStepReportView.updateTableView(reportDataList,max);

            String step = String.format(Locale.ENGLISH,"%d steps",sum==0?0:allStep/sum);
            String calorieStr = String.format(Locale.ENGLISH,"%.1f kcal",((sum==0?0:allCalorie/sum+55)/100)/10f);
            String distanceStr = "";
            if (userModel.unit == 0){
                distanceStr = String.format(Locale.ENGLISH,"%.2f km",(sum==0?0:allDistance/sum+55)/100/100f);
            }else {
                distanceStr = String.format(Locale.ENGLISH,"%.2f mile", MathUtil.newInstance().km2Miles(sum==0?0:allDistance/sum/10));
            }
            String planStr = String.format(Locale.ENGLISH,"%.1f%%",plan/7f*100);
            iStepReportView.updateStepView(step,calorieStr,distanceStr,planStr);

        }
    }

    @Override
    public void register(IStepReportView iStepReportView) {
        this.iStepReportView = iStepReportView;
    }

    @Override
    public void unregister() {
        this.iStepReportView = null;
    }
}
