package com.szip.healthy.Model;

import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.HeartData;

import java.util.List;

public class HealthyData {

    private int type;//1:心率 2:计步 3:睡眠 4:心电 5:血氧 6:血压 7:体温
    private int data,data1;
    private String dataStr;
    private List<BloodOxygenData> bloodOxygenDataList;
    private List<BloodPressureData> bloodPressureDataList;
    private List<AnimalHeatData> animalHeatDataList;
    private List<HeartData> heartDataList;
    private long time;

    public HealthyData(int type) {
        this.type = type;
    }

    public void setData(int data) {
        this.data = data;
    }

    public void setData1(int data1) {
        this.data1 = data1;
    }

    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
    }

    public void setHeartDataList(List<HeartData> heartDataList) {
        this.heartDataList = heartDataList;
    }

    public List<HeartData> getHeartDataList() {
        return heartDataList;
    }

    public void setBloodOxygenDataList(List<BloodOxygenData> bloodOxygenDataList) {
        this.bloodOxygenDataList = bloodOxygenDataList;
    }

    public void setBloodPressureDataList(List<BloodPressureData> bloodPressureDataList) {
        this.bloodPressureDataList = bloodPressureDataList;
    }

    public void setAnimalHeatDataList(List<AnimalHeatData> animalHeatDataList) {
        this.animalHeatDataList = animalHeatDataList;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public int getData() {
        return data;
    }

    public int getData1() {
        return data1;
    }

    public String getDataStr() {
        return dataStr;
    }

    public List<BloodOxygenData> getBloodOxygenDataList() {
        return bloodOxygenDataList;
    }

    public List<BloodPressureData> getBloodPressureDataList() {
        return bloodPressureDataList;
    }

    public List<AnimalHeatData> getAnimalHeatDataList() {
        return animalHeatDataList;
    }

    public long getTime() {
        return time;
    }
}
