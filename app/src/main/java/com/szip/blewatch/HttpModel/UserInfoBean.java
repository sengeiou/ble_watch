package com.szip.blewatch.HttpModel;


import com.szip.blewatch.base.db.dbModel.UserModel;
import com.zhy.http.okhttp.BaseApi;

/**
 * Created by Administrator on 2019/11/30.
 */

public class UserInfoBean extends BaseApi {
    private UserModel data;

    public UserModel getData() {
        return data;
    }

    public void setData(UserModel data) {
        this.data = data;
    }
}
