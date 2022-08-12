package com.szip.healthy.Activity.step;

public interface IStepReportPresenter {
    void loadData(long time);
    void register(IStepReportView iStepReportView);
    void unregister();

}
