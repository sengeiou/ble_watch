package com.szip.healthy.Activity.step;

import android.content.Context;

import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StepDayPresenterImpl implements IStepReportPresenter{
    private Context mContext;
    private IStepReportView iStepReportView;

    public StepDayPresenterImpl(Context mContext, IStepReportView iStepReportView) {
        this.mContext = mContext;
        this.iStepReportView = iStepReportView;
    }
    @Override
    public void loadData(long time) {
        StepData stepData = LoadDataUtil.newInstance().getStepWithDay(time);
        if (stepData == null)
            return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        if (stepData.dataForHour!=null&&!stepData.dataForHour.equals("")){
            String[] strs = stepData.dataForHour.split(",");
            int max = 5;

            List<ReportData> reportDataList = new ArrayList<>();
            for (int i = 0;i<24;i++){
                reportDataList.add(new ReportData(0,time+i*60*60));
            }

            for (String str:strs){
                int data = Integer.valueOf(str.split(":")[1]);
                calendar.set(Calendar.HOUR_OF_DAY,Integer.valueOf(str.split(":")[0]));
                long reportTime = calendar.getTimeInMillis()/1000;
                ReportData reportData =new ReportData(data,reportTime);
                reportDataList.set(Integer.valueOf(str.split(":")[0]),reportData);
                if (data>max)
                    max = data;
            }
            if (iStepReportView!=null)
                iStepReportView.updateTableView(reportDataList,max);
        }

        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;

        String step = String.format(Locale.ENGLISH,"%d steps",stepData.steps);
        String calorieStr = String.format(Locale.ENGLISH,"%.1f kcal",((stepData.calorie+55)/100)/10f);
        String distanceStr = "";
        if (userModel.unit == 0){
            distanceStr = String.format(Locale.ENGLISH,"%.2f km",(stepData.distance+55)/100/100f);
        }else {
            distanceStr = String.format(Locale.ENGLISH,"%.2f mile", MathUtil.newInstance().km2Miles(stepData.distance/10));
        }
        String plan = String.format(Locale.ENGLISH,"%.1f%%",stepData.steps/(float)userModel.stepsPlan>1?100:
                (stepData.steps/(float)userModel.stepsPlan*100));

        if (iStepReportView!=null)
            iStepReportView.updateStepView(step,calorieStr,distanceStr,plan);

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
