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
            SaveDataUtil saveDataUtil = SaveDataUtil.newInstance();
            if (response.getData().getBloodOxygenData().size()!=0)
                saveDataUtil.saveBloodOxygenDataListData(response.getData().getBloodOxygenData());

            if (response.getData().getBloodPressureDataList().size()!=0)
                saveDataUtil.saveBloodPressureDataListData(response.getData().getBloodPressureDataList());

            if (response.getData().getHeartDataList().size()!=0)
                saveDataUtil.saveHeartDataListData(response.getData().getHeartDataList());

            if (response.getData().getSleepDataList().size()!=0)
                saveDataUtil.saveSleepDataListData(response.getData().getSleepDataList());

            if (response.getData().getStepDataList().size()!=0)
                saveDataUtil.saveStepDataListDataFromWeb(response.getData().getStepDataList());

            if (response.getData().getEcgDataList().size()!=0)
                saveDataUtil.saveEcgDataListData(response.getData().getEcgDataList());

            if (response.getData().getAnimalHeatDataList().size()!=0)
                saveDataUtil.saveAnimalHeatDataListData(response.getData().getAnimalHeatDataList());

            if (response.getData().getSportDataList().size()!=0)
                saveDataUtil.saveSportDataListData(response.getData().getSportDataList());
        }
    }
}
