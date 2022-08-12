package com.szip.user.Utils;


import com.szip.blewatch.base.Util.ble.ClientManager;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.http.TokenInterceptor;
import com.szip.blewatch.base.db.DownloadDataUtil;
import com.szip.user.HttpModel.AvatarBean;
import com.szip.user.HttpModel.DeviceConfigBean;
import com.szip.user.HttpModel.DialBean;
import com.szip.user.HttpModel.FaqBean;
import com.szip.user.HttpModel.FaqListBean;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.builder.PostJsonBuilder;
import com.zhy.http.okhttp.builder.PostJsonListBuider;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.io.File;

import okhttp3.OkHttpClient;

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

    public void getDeviceConfig(GenericsCallback<DeviceConfigBean> callback){
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addParams("appName","FitRing")
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"comm/getAppFunctionConfigs",callback);
    }

    /**
     * 绑定设备
     * */
    public void getBindDevice(String deviceCode,String product, GenericsCallback<BaseApi> callback){
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addInterceptor(new TokenInterceptor())
                .addParams("deviceCode",deviceCode)
                .addParams("deviceName",product)
                .addParams("product",product);
        HttpClientUtils.newInstance().buildRequest(getBuilder,"device/bindDevice",callback);
    }

    public void getUnbindDevice(GenericsCallback<BaseApi> callback){
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"device/unbindDevice",callback);
    };


    public void postForSetUnit(String unit,String temp,GenericsCallback<BaseApi>callback){
        PostJsonBuilder builder = OkHttpUtils
                .jpost()
                .addParams("unit",unit)
                .addParams("tempUnit",temp)
                .addInterceptor(new TokenInterceptor());

        HttpClientUtils.newInstance().buildRequest(builder,"v2/user/setUnit",callback);
    }

    public void postForSetUserInfo(String name,String sex,String birthday,String height,String weight,
                                   String heightBritish,String weightBritish,GenericsCallback<BaseApi> callback){
        PostJsonBuilder builder = OkHttpUtils
                .jpost()
                .addParams("userName",name)
                .addParams("lastName","")
                .addParams("firstName","")
                .addParams("sex",sex)
                .addParams("birthday",birthday)
                .addParams("nation","")
                .addParams("height",height)
                .addParams("weight",weight)
                .addParams("heightBritish",heightBritish)
                .addParams("weightBritish",weightBritish)
                .addParams("blood","")
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(builder,"v2/user/updateUserInfo",callback);
    }

    public void postUploadAvatar(File avatar, GenericsCallback<AvatarBean> callback){
        PostFormBuilder formBuilder = OkHttpUtils
                .fpost()
                .addFile("file","iSmarport_6.jpg",avatar)
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(formBuilder,"user/setProfilePicture",callback);
    }

    public void postForUploadReportData(String data,GenericsCallback<BaseApi> callback){
        PostJsonListBuider jsonBuilder =   OkHttpUtils
                .listpost()
                .addInterceptor(new TokenInterceptor())
                .addParams("data",data);
        HttpClientUtils.newInstance().buildRequest(jsonBuilder,"data/upload",callback);
    }

    public void postSendFeedback(String content,GenericsCallback<BaseApi> callback){
        PostJsonBuilder jsonBuilder = OkHttpUtils
                .jpost()
                .addParams("content",content)
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(jsonBuilder,"user/uploadFeedback",callback);
    }

    public void getFaqList(GenericsCallback<FaqListBean> callback){
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addParams("pageNum","1")
                .addParams("pageSize","30")
                .addInterceptor(new TokenInterceptor());

        HttpClientUtils.newInstance().buildRequest(getBuilder,"comm/getQuestionAndAnswers",callback);
    }

    public void getFaq(String id,boolean isNumber,GenericsCallback<FaqBean> callback){
        GetBuilder getBuilder;
        if (isNumber)
            getBuilder = OkHttpUtils
                .get()
                .addParams("id",id)
                .addInterceptor(new TokenInterceptor());
        else
            getBuilder = OkHttpUtils
                    .get()
                    .addParams("questionType",id)
                    .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"comm/getQuestionAndAnswerDetail",callback);
    }

    public void getDialList(String id,GenericsCallback<DialBean> callback){
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addParams("pageNum","1")
                .addParams("pageSize","30")
                .addParams("watchPlateGroupId",id)
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"device/watchPlate",callback);
    }

    public void postForSetStepsPlan(String step,int id,GenericsCallback<BaseApi> callback){
        PostJsonBuilder postJsonBuilder = OkHttpUtils
                .jpost()
                .id(id)
                .addParams("stepsPlan",step)
                .addInterceptor(new TokenInterceptor());

        HttpClientUtils.newInstance().buildRequest(postJsonBuilder,"user/updateStepsPlan",callback);
    }

    public void postForSetSleepPlan(String sleep,int id,GenericsCallback<BaseApi> callback){
        PostJsonBuilder postJsonBuilder = OkHttpUtils
                .jpost()
                .id(id)
                .addParams("sleepPlan",sleep)
                .addInterceptor(new TokenInterceptor());

        HttpClientUtils.newInstance().buildRequest(postJsonBuilder,"user/updateSleepPlan",callback);
    }

    public void postForSetCaloriePlan(String calorie,int id,GenericsCallback<BaseApi> callback){
        PostJsonBuilder postJsonBuilder = OkHttpUtils
                .jpost()
                .id(id)
                .addParams("caloriePlan",calorie)
                .addInterceptor(new TokenInterceptor());

        HttpClientUtils.newInstance().buildRequest(postJsonBuilder,"user/updateCaloriePlan",callback);
    }

    public void deleteAccount(GenericsCallback<BaseApi> callback){
        GetBuilder builder = OkHttpUtils
                .get()
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(builder,"user/unregister",callback);
    }

    /**
     * 获取服务器上的数据
     * */
    public void getForDownloadReportData(String time, String size) {
        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addParams("time",time)
                .addParams("size",size)
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"data/get",new DownloadDataUtil(new JsonGenericsSerializator()));
    }

}
