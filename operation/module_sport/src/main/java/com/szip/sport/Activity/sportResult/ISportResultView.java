package com.szip.sport.Activity.sportResult;

import com.szip.sport.Model.SportResultDataModel;

import java.util.List;

public interface ISportResultView {
    void updateSportView(String distance, String time, String sportType, List<SportResultDataModel> list);
    void updateHeartView(int[] heartData,int[] heartLevel);
    void updateSpeedView(String[]speed);
    void updateStrideView(int []stride);
    void updateAltitudeView(int []altitude);
    void updateLocation();
}
