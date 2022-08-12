package com.szip.healthy.ModuleMain;

import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.healthy.Model.HealthyData;

import java.util.List;

public interface IHealthyView {
    void updateSportData(StepData stepData);
    void updateLastSport(SportData sportData);
    void updateHealthyCard(List<HealthyData> healthyDataList);
}
