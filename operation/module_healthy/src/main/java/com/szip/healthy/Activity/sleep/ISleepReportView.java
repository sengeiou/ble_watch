package com.szip.healthy.Activity.sleep;

import com.szip.healthy.Model.ReportData;

import java.util.List;

public interface ISleepReportView {
    void updateTableView(List<ReportData> reportData,int startTime,int allTime);
    void updateProgressView(int startTime, int deepTime, int lightTime);
    void updateView(String allTime,String deepTime,String lightTime,String plan);
}
