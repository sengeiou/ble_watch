package com.szip.healthy.Activity.step;

import com.szip.healthy.Model.ReportData;
import com.szip.healthy.View.ReportTableView;

import java.util.List;

public interface IStepReportView {
    void updateTableView(List<ReportData> dataList,int maxValue);
    void updateStepView(String step,String calorie,String distance,String plan);
}
