package com.szip.login.HttpModel;


import com.szip.blewatch.base.db.dbModel.UserModel;
import com.zhy.http.okhttp.BaseApi;

/**
 * Created by Administrator on 2019/11/30.
 */

public class LoginBean extends BaseApi {
    private Data data;

    public class Data{
        private String token;
        private UserModel userInfo;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserModel getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserModel userInfo) {
            this.userInfo = userInfo;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
