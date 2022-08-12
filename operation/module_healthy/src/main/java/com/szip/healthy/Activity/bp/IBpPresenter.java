package com.szip.healthy.Activity.bp;

public interface IBpPresenter {
    void loadData(long time);
    void register(IBpView iBpView);
    void unRegister();
}
