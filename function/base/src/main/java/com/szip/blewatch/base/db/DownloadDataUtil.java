package com.szip.blewatch.base.db;

import android.os.Handler;

import com.szip.blewatch.base.Model.DownloadDataBean;
import com.szip.blewatch.base.Util.MathUtil;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.callback.IGenericsSerializator;

import okhttp3.Call;

import static android.content.Context.MODE_PRIVATE;

public class DownloadDataUtil extends GenericsCallback<DownloadDataBean> {
    public DownloadDataUtil(IGenericsSerializator serializator) {
        super(serializator);
    }

    @Override
    public void onError(Call call, Exception e, int id) {

    }

    @Override
    public void onResponse(DownloadDataBean response, int id) {
        if (response.getCode() == 200){
            SaveDataUtil.newInstance().saveWebData(response);
        }
    }
}
