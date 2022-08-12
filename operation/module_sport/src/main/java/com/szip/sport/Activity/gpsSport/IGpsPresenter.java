package com.szip.sport.Activity.gpsSport;

import androidx.fragment.app.FragmentManager;

public interface IGpsPresenter {
    void startLocationService();
    void stopLocationService();
    void finishLocationService(boolean isSave);
    void setViewDestory();



}
