package com.zhy.http.okhttp.builder;

import com.zhy.http.okhttp.request.PostJsonListRequest;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;

/**
 * Created by Administrator on 2019/6/14.
 */

public class PostJsonListBuider extends OkHttpRequestBuilder<PostJsonListBuider> implements HasParamsable{

    public PostJsonListBuider addInterceptor(Interceptor interceptor){
        this.interceptor = interceptor;
        return this;
    }

    @Override
    public RequestCall build() {
        return new PostJsonListRequest(url, tag, params, headers,interceptor,id).build();
    }

    @Override
    public PostJsonListBuider params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    @Override
    public PostJsonListBuider addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }
}
