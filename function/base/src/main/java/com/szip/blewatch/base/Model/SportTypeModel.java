package com.szip.blewatch.base.Model;

public class SportTypeModel {
    private int type;
    private String sportStr;

    public SportTypeModel(int type, String sportStr) {
        this.type = type;
        this.sportStr = sportStr;
    }

    public int getType() {
        return type;
    }

    public String getSportStr() {
        return sportStr;
    }
}
