package com.szip.blewatch.base.Model;

import com.raizlabs.android.dbflow.structure.BaseModel;


public class HealthyConfig extends BaseModel {

    /**
     * 是否支持心率 1支持 0不支持
     */
    public byte heartRate;

    /**
     * 是否支持心电 1支持 0不支持
     */
    public byte ecg;

    /**
     * 是否支持血氧 1支持 0不支持
     */
    public byte bloodOxygen;

    /**
     * 是否支持血压 1支持 0不支持
     */
    public byte bloodPressure;

    /**
     * 是否支持计步 1支持 0不支持
     */
    public byte stepCounter;

    /**
     * 是否支持体温 1支持 0不支持
     */
    public byte temperature;

    /**
     * 是否支持睡眠 1支持 0不支持
     */
    public byte sleep;


    public byte heartRateAutoTest;
    public byte ecgAutoTest;
    public byte bloodOxygenAutoTest;
    public byte bloodPressureAutoTest;
    public byte stepCounterAutoTest;

    public byte temperatureAutoTest;
    public byte sleepAutoTest;





    public HealthyConfig(byte heartRate, byte ecg, byte bloodOxygen, byte bloodPressure, byte stepCounter, byte temperature, byte sleep) {
        this.heartRate = heartRate;
        this.ecg = ecg;
        this.bloodOxygen = bloodOxygen;
        this.bloodPressure = bloodPressure;
        this.stepCounter = stepCounter;
        this.temperature = temperature;
        this.sleep = sleep;
    }
}
