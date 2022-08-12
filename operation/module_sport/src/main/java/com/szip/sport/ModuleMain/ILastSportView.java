package com.szip.sport.ModuleMain;

import com.szip.blewatch.base.db.dbModel.SportData;

import java.util.List;

public interface ILastSportView {
    void updateLastSport(List<SportData> sportDataList);
    void updateLocation(float acc);
}
