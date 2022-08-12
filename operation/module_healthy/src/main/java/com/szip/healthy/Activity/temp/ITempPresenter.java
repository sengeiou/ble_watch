package com.szip.healthy.Activity.temp;

public interface ITempPresenter {
    void loadData(long time);
    void register(ITempView iTempView);
    void unRegister();
}
