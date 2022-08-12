package com.szip.user.HttpModel;


import com.zhy.http.okhttp.BaseApi;

import java.util.ArrayList;

public class FaqListBean extends BaseApi {
    private Data data;

    public Data getData() {
        return data;
    }

    public class Data {
        private ArrayList<FaqModel> list;

        public ArrayList<FaqModel> getList() {
            return list;
        }
    }
}
