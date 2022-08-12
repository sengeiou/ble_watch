package com.szip.blewatch.base.Util.http;

import android.content.Context;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.blewatch.base.BuildConfig;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.HealthyCardData;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.builder.PostJsonBuilder;
import com.zhy.http.okhttp.builder.PostJsonListBuider;
import com.zhy.http.okhttp.callback.Callback;


public class HttpClientUtils {


    private String url = BuildConfig.SERVER_URL;
    private String token = "";
    private String language = "zh-CN";
    private String time;

    private Context mContext;

    private static HttpClientUtils httpClientUtils;
    private HttpClientUtils(){
    }


    public void setToken(String token) {
        this.token = token;
        if (token.equals("")){//如果token = ""说明登录过期了，把之前缓存的token换成""
            MathUtil.newInstance().saveStringData(mContext,"token",null);
            MathUtil.newInstance().saveIntData(mContext,"userId",-1);
            MathUtil.newInstance().saveIntData(mContext,"weatherTime",0);
            SaveDataUtil.newInstance().clearDB();
            SQLite.delete()
                    .from(HealthyCardData.class)
                    .execute();
        }
    }

    public void init(Context context){
        mContext = context;
        language = context.getResources().getConfiguration().locale.getLanguage()+"-"+
                context.getResources().getConfiguration().locale.getCountry();
        time = DateUtil.getGMTWithString();
    }

    public static HttpClientUtils newInstance(){                     // 单例模式，双重锁
        if( httpClientUtils == null ){
            synchronized (HttpClientUtils.class){
                if( httpClientUtils == null ){
                    httpClientUtils = new HttpClientUtils();
                }
            }
        }
        return httpClientUtils;
    }

    public void buildRequest(PostJsonBuilder builder, String method,Callback callback){
        builder.url(url+method)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addHeader("token",token)
                .addHeader("project","FitRing")
                .build()
                .execute(callback);
    }
    public void buildRequest(GetBuilder builder, String method, Callback callback){
        builder.url(url+method)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addHeader("token",token)
                .addHeader("project","FitRing")
                .build()
                .execute(callback);
    }

    public void buildRequest(PostFormBuilder builder, String method, Callback callback){
        builder.url(url+method)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addHeader("token",token)
                .addHeader("project","FitRing")
                .build()
                .execute(callback);
    }

    public void buildRequest(PostJsonListBuider builder, String method, Callback callback){
        builder.url(url+method)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addHeader("token",token)
                .addHeader("project","FitRing")
                .build()
                .execute(callback);
    }

}
