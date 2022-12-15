package com.szip.blewatch.HttpModel;

import com.szip.blewatch.base.Model.FirmwareModel;
import com.zhy.http.okhttp.BaseApi;

public class FirmwareBean extends BaseApi {
    private FirmwareModel data;

    public FirmwareModel getData() {
        return data;
    }

    public void setData(FirmwareModel data) {
        this.data = data;
    }
}
