package com.zhy.http.okhttp.builder;

import com.zhy.http.okhttp.request.PostFormRequest;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;

/**
 * Created by Administrator on 2019/6/20.
 */

public class PostFormBuilder extends OkHttpRequestBuilder<PostFormBuilder> implements HasParamsable{

    private List<PostFormBuilder.FileInput> files = new ArrayList<>();

    public PostFormBuilder addInterceptor(Interceptor interceptor){
        this.interceptor = interceptor;
        return this;
    }

    @Override
    public RequestCall build()
    {
        return new PostFormRequest(url, tag, params, headers,interceptor,files,id).build();
    }

    @Override
    public PostFormBuilder params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    public PostFormBuilder addFile(String name, String filename, File file)
    {
        files.add(new PostFormBuilder.FileInput(name, filename, file));
        return this;
    }

    @Override
    public PostFormBuilder addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }

    public static class FileInput
    {
        public String key;
        public String filename;
        public File file;

        public FileInput(String name, String filename, File file)
        {
            this.key = name;
            this.filename = filename;
            this.file = file;
        }

        @Override
        public String toString()
        {
            return "FileInput{" +
                    "key='" + key + '\'' +
                    ", filename='" + filename + '\'' +
                    ", file=" + file +
                    '}';
        }
    }
}
