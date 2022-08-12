package com.szip.healthy.Activity.temp;

import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;

import java.util.List;

public interface ITempView {
    void updateTable(List<ReportData> reportData);
    void updateView(String average,String max,String min,String small);
    void updateList(List<ReportInfoData> reportInfoData);
}
