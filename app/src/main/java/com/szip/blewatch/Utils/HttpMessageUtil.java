package com.szip.blewatch.Utils;


import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.http.TokenInterceptor;
import com.szip.blewatch.HttpModel.UserInfoBean;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostJsonListBuider;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.IOException;

public class HttpMessageUtil {

    private static HttpMessageUtil httpMessageUtil;
    private HttpMessageUtil(){

    }

    public static HttpMessageUtil newInstance(){                     // 单例模式，双重锁
        if( httpMessageUtil == null ){
            synchronized (HttpMessageUtil.class){
                if( httpMessageUtil == null ){
                    httpMessageUtil = new HttpMessageUtil();
                }
            }
        }
        return httpMessageUtil;
    }


    /**
     * 获取个人信息
     * */
    public void getForGetInfo(GenericsCallback<UserInfoBean> callback)throws IOException{
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"v2/user/getUserInfo",callback);
    }

    public void postForUploadReportData(String data,GenericsCallback<BaseApi> callback){
        PostJsonListBuider jsonBuilder =   OkHttpUtils
                .listpost()
                .addInterceptor(new TokenInterceptor())
                .addParams("data",data);
        HttpClientUtils.newInstance().buildRequest(jsonBuilder,"data/upload",callback);
    }

}
