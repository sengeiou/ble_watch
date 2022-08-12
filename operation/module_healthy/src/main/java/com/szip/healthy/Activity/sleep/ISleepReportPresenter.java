package com.szip.healthy.Activity.sleep;

public interface ISleepReportPresenter {
    void loadData(long time);
    void register(ISleepReportView iSleepReportView);
    void unRegister();
}
