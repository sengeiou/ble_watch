package com.szip.healthy.Model;

public class ReportData {

    private int averageData;
    private int maxData;
    private int minData;
    private long time;

    private int averageDbpData;
    private int maxDbpData;
    private int minDbpData;

    public ReportData(int averageData, int maxData, int minData, int averageDbpData, int maxDbpData, int minDbpData, long time) {
        this.averageData = averageData;
        this.maxData = maxData;
        this.minData = minData;
        this.time = time;
        this.averageDbpData = averageDbpData;
        this.maxDbpData = maxDbpData;
        this.minDbpData = minDbpData;
    }

    public ReportData(int averageData, int maxData, int minData, long time) {
        this.averageData = averageData;
        this.maxData = maxData;
        this.minData = minData;
        this.time = time;
    }

    public ReportData(int averageData, int averageDbpData, long time) {
        this.averageData = averageData;
        this.time = time;
        this.averageDbpData = averageDbpData;
    }

    public ReportData(int averageData, long time) {
        this.averageData = averageData;
        this.time = time;
    }

    public int getAverageData() {
        return averageData;
    }

    public int getMaxData() {
        return maxData;
    }

    public int getMinData() {
        return minData;
    }

    public int getAverageDbpData() {
        return averageDbpData;
    }

    public int getMaxDbpData() {
        return maxDbpData;
    }

    public int getMinDbpData() {
        return minDbpData;
    }

    public long getTime() {
        return time;
    }
}
