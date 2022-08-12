package com.szip.user.HttpModel;



import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.zhy.http.okhttp.BaseApi;

import java.util.ArrayList;

public class DeviceConfigBean extends BaseApi {
    ArrayList<SportWatchAppFunctionConfigDTO> data;

    public ArrayList<SportWatchAppFunctionConfigDTO> getData() {
        return data;
    }
}
