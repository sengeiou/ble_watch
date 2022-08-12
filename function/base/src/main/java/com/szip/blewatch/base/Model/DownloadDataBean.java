package com.szip.blewatch.base.Model;


import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.EcgData;
import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.zhy.http.okhttp.BaseApi;

import java.util.ArrayList;

public class DownloadDataBean extends BaseApi {

    private Data data;

    public class Data{
        ArrayList<BloodOxygenData> bloodOxygenDataList;
        ArrayList<BloodPressureData> bloodPressureDataList;
        ArrayList<EcgData> ecgDataList;
        ArrayList<HeartData> heartDataList;
        ArrayList<SleepData>  sleepDataList;
        ArrayList<SportData>  sportDataList;
        ArrayList<StepData>  stepDataList;
        ArrayList<AnimalHeatData>  tempDataList;

        public ArrayList<BloodOxygenData> getBloodOxygenData() {
            return bloodOxygenDataList;
        }

        public ArrayList<BloodPressureData> getBloodPressureDataList() {
            return bloodPressureDataList;
        }

        public ArrayList<EcgData> getEcgDataList() {
            return ecgDataList;
        }

        public ArrayList<HeartData> getHeartDataList() {
            return heartDataList;
        }

        public ArrayList<SleepData> getSleepDataList() {
            return sleepDataList;
        }

        public ArrayList<SportData> getSportDataList() {
            return sportDataList;
        }

        public ArrayList<StepData> getStepDataList() {
            return stepDataList;
        }

        public ArrayList<AnimalHeatData> getAnimalHeatDataList() {
            return tempDataList;
        }
    }

    public Data getData() {
        return data;
    }
}
