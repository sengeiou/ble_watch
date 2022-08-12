package com.szip.blewatch.base.Model;

import java.util.HashMap;

/**
 * 该类用来解析2523返回的计步数据，做中转数据使用
 * */
public class BleStepModel {

    private int step;
    private int distance;
    private int calorie;
    private long time;
    private HashMap<Integer,Integer> stepInfo;

    public BleStepModel(int step, int distance, int calorie, long time, HashMap<Integer, Integer> stepInfo) {
        this.step = step;
        this.distance = distance;
        this.calorie = calorie;
        this.time = time;
        this.stepInfo = stepInfo;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step += step;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance += distance;
    }

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie += calorie;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public HashMap<Integer, Integer> getStepInfo() {
        return stepInfo;
    }

    public void setStepInfo(int key,int value) {
        if (stepInfo.containsKey(key))
            stepInfo.put(key,stepInfo.get(key)+value);
        else
            stepInfo.put(key,value);
    }
}
