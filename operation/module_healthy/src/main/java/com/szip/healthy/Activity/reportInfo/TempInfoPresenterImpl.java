package com.szip.healthy.Activity.reportInfo;

import android.content.Context;

import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.UserModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TempInfoPresenterImpl implements IReportInfoPresenter{

    private IReportInfoView iReportInfoView;
    private Context mContext;

    private ArrayList<ReportInfoData> groupList;
    private ArrayList<ReportInfoData> childList;

    public TempInfoPresenterImpl(IReportInfoView iReportInfoView, Context mContext) {
        this.iReportInfoView = iReportInfoView;
        this.mContext = mContext;
    }

    @Override
    public void initList() {
        groupList = new ArrayList<>();
        childList = new ArrayList<>();
        List<AnimalHeatData> list = LoadDataUtil.newInstance().getTempList(0);
        if (list.size()==0){
            if (iReportInfoView!=null)
                iReportInfoView.updateList(groupList,childList);
            return;
        }

        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;
        String oldTime =  DateUtil.getStringDateFromSecond(list.get(0).time,"yyyy/MM");
        ReportInfoData reportInfoData = new ReportInfoData("", DateUtil.getTimeScope(oldTime,"yyyy/MM"));
        groupList.add(reportInfoData);
        childList.add(reportInfoData);
        for (int i = 0;i<list.size();i++){
            AnimalHeatData animalHeatData = list.get(i);
            String time = DateUtil.getStringDateFromSecond(animalHeatData.time,"yyyy/MM");
            if (!time.equals(oldTime)) {
                reportInfoData = new ReportInfoData("", DateUtil.getTimeScope(time,"yyyy/MM"));
                groupList.add(reportInfoData);
                childList.add(reportInfoData);
                oldTime = time;
            }
            if (userModel.tempUnit==0){
                reportInfoData = new ReportInfoData(String.format(Locale.ENGLISH,"%.1f ℃",animalHeatData.tempData/10f), animalHeatData.time);
            }else {
                reportInfoData = new ReportInfoData(String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(animalHeatData.tempData/10f)),  animalHeatData.time);
            }
            childList.add(reportInfoData);
        }
        if (iReportInfoView!=null)
            iReportInfoView.updateList(groupList,childList);
    }

    @Override
    public void loadData(int page) {
        List<AnimalHeatData> list = LoadDataUtil.newInstance().getTempList(page);
        if (list.size()==0)
            if (iReportInfoView!=null)
                iReportInfoView.updateList(groupList,childList);
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
        if (userModel==null)
            return;
        String oldTime =  DateUtil.getStringDateFromSecond(groupList.get(groupList.size()-1).getTime(),"yyyy/MM");
        ReportInfoData reportInfoData;
        for (int i = 0;i<list.size();i++){
            AnimalHeatData animalHeatData = list.get(i);
            String time = DateUtil.getStringDateFromSecond(animalHeatData.time,"yyyy/MM");
            if (!time.equals(oldTime)) {
                reportInfoData = new ReportInfoData("", DateUtil.getTimeScope(time,"yyyy/MM"));
                groupList.add(reportInfoData);
                childList.add(reportInfoData);
                oldTime = time;
            }
            if (userModel.tempUnit==0){
                reportInfoData = new ReportInfoData(String.format(Locale.ENGLISH,"%.1f ℃",animalHeatData.tempData/10f), animalHeatData.time);
            }else {
                reportInfoData = new ReportInfoData(String.format(Locale.ENGLISH,"%.1f ℉",MathUtil.newInstance().c2f(animalHeatData.tempData/10f)),  animalHeatData.time);
            }
            childList.add(reportInfoData);
        }
        if (iReportInfoView!=null)
            iReportInfoView.updateList(groupList,childList);
    }
}
