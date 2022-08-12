package com.szip.login.HttpModel;

import com.zhy.http.okhttp.BaseApi;

/**
 * Created by Administrator on 2019/12/9.
 */

public class CheckVerificationBean extends BaseApi {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data{
        private boolean isValid;

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }
    }
}
