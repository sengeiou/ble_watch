package com.szip.healthy.Activity.reportInfo;

import android.content.Context;

import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;

import java.util.ArrayList;
import java.util.List;

public class BloodOxygenInfoPresenterImpl implements IReportInfoPresenter{

    private IReportInfoView iReportInfoView;
    private Context mContext;

    private ArrayList<ReportInfoData> groupList;
    private ArrayList<ReportInfoData> childList;

    public BloodOxygenInfoPresenterImpl(IReportInfoView iReportInfoView, Context mContext) {
        this.iReportInfoView = iReportInfoView;
        this.mContext = mContext;
    }

    @Override
    public void initList() {
        groupList = new ArrayList<>();
        childList = new ArrayList<>();
        List<BloodOxygenData> list = LoadDataUtil.newInstance().getBloodOxygenList(0);
        if (list.size()==0){
            if (iReportInfoView!=null)
                iReportInfoView.updateList(groupList,childList);
            return;
        }


        String oldTime =  DateUtil.getStringDateFromSecond(list.get(0).time,"yyyy/MM");
        ReportInfoData reportInfoData = new ReportInfoData("", DateUtil.getTimeScope(oldTime,"yyyy/MM"));
        groupList.add(reportInfoData);
        childList.add(reportInfoData);
        for (int i = 0;i<list.size();i++){
            BloodOxygenData bloodOxygenData = list.get(i);
            String time = DateUtil.getStringDateFromSecond(bloodOxygenData.time,"yyyy/MM");
            if (!time.equals(oldTime)) {
                reportInfoData = new ReportInfoData("", DateUtil.getTimeScope(time,"yyyy/MM"));
                groupList.add(reportInfoData);
                childList.add(reportInfoData);
                oldTime = time;
            }
            reportInfoData = new ReportInfoData(String.format("%d%%",bloodOxygenData.bloodOxygenData), bloodOxygenData.time);
            childList.add(reportInfoData);
        }
        if (iReportInfoView!=null)
            iReportInfoView.updateList(groupList,childList);
    }

    @Override
    public void loadData(int page) {
        List<BloodOxygenData> list = LoadDataUtil.newInstance().getBloodOxygenList(page);
        if (list.size()==0)
            if (iReportInfoView!=null)
                iReportInfoView.updateList(groupList,childList);
        String oldTime =  DateUtil.getStringDateFromSecond(groupList.get(groupList.size()-1).getTime(),"yyyy/MM");
        ReportInfoData reportInfoData;
        for (int i = 0;i<list.size();i++){
            BloodOxygenData bloodOxygenData = list.get(i);
            String time = DateUtil.getStringDateFromSecond(bloodOxygenData.time,"yyyy/MM");
            if (!time.equals(oldTime)) {
                reportInfoData = new ReportInfoData("", DateUtil.getTimeScope(time,"yyyy/MM"));
                groupList.add(reportInfoData);
                childList.add(reportInfoData);
                oldTime = time;
            }
            reportInfoData = new ReportInfoData(String.format("%d%%",bloodOxygenData.bloodOxygenData), bloodOxygenData.time);
            childList.add(reportInfoData);
        }
        if (iReportInfoView!=null)
            iReportInfoView.updateList(groupList,childList);
    }
}
