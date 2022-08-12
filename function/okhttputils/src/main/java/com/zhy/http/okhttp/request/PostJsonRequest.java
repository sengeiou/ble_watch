package com.zhy.http.okhttp.request;

import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostJsonBuilder;
import com.zhy.http.okhttp.callback.Callback;

import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import com.google.gson.Gson;

/**
 * Created by zhy on 15/12/14.
 */
public class PostJsonRequest extends OkHttpRequest {

    public PostJsonRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
                           Interceptor interceptor,int id)
    {
        super(url, tag, params, headers,interceptor,id);
    }

    @Override
    protected RequestBody buildRequestBody()
    {
        Gson gson=new Gson();
        RequestBody body = FormBody.create(MediaType.parse("application/json"),
                params==null?"{}":gson.toJson(params));
        return body;
    }

    @Override
    protected RequestBody wrapRequestBody(RequestBody requestBody, final Callback callback)
    {
        if (callback == null) return requestBody;

        final CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener()
        {
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength)
            {
                OkHttpUtils.getInstance().getDelivery().execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        callback.inProgress(bytesWritten * 1.0f / contentLength,contentLength,id);
                    }
                });

            }
        });
        return countingRequestBody;
    }

    @Override
    protected Request buildRequest(RequestBody requestBody)
    {
        return builder.post(requestBody).build();
    }
}
