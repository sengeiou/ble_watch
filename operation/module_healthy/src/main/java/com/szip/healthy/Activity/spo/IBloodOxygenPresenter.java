package com.szip.healthy.Activity.spo;

public interface IBloodOxygenPresenter {
    void loadData(long time);
    void register(IBloodOxygenView iBloodOxygenView);
    void unRegister();
}
