package com.szip.sport.Model;

public class SportResultDataModel {

    private String unit;
    private String data;

    public SportResultDataModel(String dataType, String data) {
        this.unit = dataType;
        this.data = data;
    }

    public String getUnit() {
        return unit;
    }

    public String getData() {
        return data;
    }
}
