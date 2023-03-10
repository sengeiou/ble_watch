package com.szip.blewatch.Utils;


import com.szip.blewatch.HttpModel.FirmwareBean;
import com.szip.blewatch.base.Model.FirmwareModel;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.http.TokenInterceptor;
import com.szip.blewatch.HttpModel.UserInfoBean;
import com.szip.blewatch.base.db.LoadDataUtil;
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

    /**
     * 获取个人信息
     * */
    public void getFirmware(String configId,GenericsCallback<FirmwareBean> callback){
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addParams("configId", configId)
                .addParams("currentVersion","0")
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"device/getLatestFirmware",callback);
    }

}
