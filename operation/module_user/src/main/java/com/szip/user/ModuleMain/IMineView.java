package com.szip.user.ModuleMain;

import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.UserModel;

public interface IMineView {
    void updateUserInfo(boolean userVisible);
    void updateUserView(UserModel userModel);
    void updateDeviceView(int step, int sleep, int calorie,int stepPlan,int sleepPlan,int caloriePlan, SportWatchAppFunctionConfigDTO device);
}
