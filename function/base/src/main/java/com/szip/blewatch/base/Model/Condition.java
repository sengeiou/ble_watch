package com.szip.blewatch.base.Model;

public class Condition{
    private float temperature;
    private float low;
    private float high;
    private String text;
    private int code;
    private String iconUrl;

    public float getTemperature() {
        return temperature;
    }

    public String getText() {
        return text;
    }

    public int getCode() {
        return code;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public float getLow() {
        return low;
    }

    public float getHigh() {
        return high;
    }
}