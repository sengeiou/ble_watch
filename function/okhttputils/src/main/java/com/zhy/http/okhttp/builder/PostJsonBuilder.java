package com.zhy.http.okhttp.builder;

import com.zhy.http.okhttp.request.PostJsonRequest;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;

/**
 * Created by zhy on 15/12/14.
 */
public class PostJsonBuilder extends OkHttpRequestBuilder<PostJsonBuilder> implements HasParamsable {


    public PostJsonBuilder addInterceptor(Interceptor interceptor){
        this.interceptor = interceptor;
        return this;
    }

    @Override
    public RequestCall build()
    {
        return new PostJsonRequest(url, tag, params, headers,interceptor,id).build();
    }

    @Override
    public PostJsonBuilder params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    @Override
    public PostJsonBuilder addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }
}
