package com.szip.blewatch.Activity.welcome;

public interface IWelcomeView {

    void initBleFinish();
    void initDeviceConfigFinish();
    void initUserinfoFinish(boolean isNeedLogin);
    void checkPrivacyResult(boolean comfirm);
}
