package com.szip.healthy.Activity.reportInfo;

import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.db.dbModel.SportData;

import java.util.ArrayList;

public interface IReportInfoView {
    void updateList(ArrayList<ReportInfoData> groupList, ArrayList<ReportInfoData> childList);
}
