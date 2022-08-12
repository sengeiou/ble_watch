package com.szip.healthy.Activity.heart;

public interface IHeartPresenter {
    void loadData(long time);
    void register(IHeartView iHeartView);
    void unRegister();
}
