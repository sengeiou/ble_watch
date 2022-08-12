package com.szip.healthy.Activity.heart;

import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.healthy.Model.ReportData;

import java.util.List;

public interface IHeartView {
    void updateTable(List<ReportData> reportDataList, int max);
    void updateView(String average,String max,String min);
    void updateHeartLevel(int []date,int size);
}
