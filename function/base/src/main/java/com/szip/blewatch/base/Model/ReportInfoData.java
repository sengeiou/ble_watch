package com.szip.blewatch.base.Model;

public class ReportInfoData {
    private String data;
    private long time;

    public ReportInfoData(String data, long time) {
        this.data = data;
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public long getTime() {
        return time;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
