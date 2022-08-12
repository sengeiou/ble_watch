package com.szip.healthy.Activity.bp;

import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;

import java.util.List;

public interface IBpView {
    void updateTable(List<ReportData> reportDataList);
    void updateView(String sbp,String dbp,String sbpAbnormal,String dbpAbnormal);
    void updateListView(List<ReportInfoData> list);
}
