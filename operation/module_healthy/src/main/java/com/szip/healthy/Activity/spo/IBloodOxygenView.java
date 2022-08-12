package com.szip.healthy.Activity.spo;

import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;

import java.util.List;

public interface IBloodOxygenView {
    void updateTable(List<ReportData> reportData);
    void updateView(String average,String max,String min,String small);
    void updateList(List<ReportInfoData> reportInfoData);
}
