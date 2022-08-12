package com.szip.blewatch.base.db.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

@Table(database = AppDatabase.class)
public class AutoMeasureData  extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public int tempState;

    @Column
    public int tempStartTime;

    @Column
    public int tempEndTime;

    @Column
    public int tempFrequency;

    @Column
    public int bpState;

    @Column
    public int bpStartTime;

    @Column
    public int bpEndTime;

    @Column
    public int bpFrequency;

    @Column
    public int spoState;

    @Column
    public int spoStartTime;

    @Column
    public int spoEndTime;

    @Column
    public int spoFrequency;

    @Column
    public int heartState;

    @Column
    public int heartStartTime;

    @Column
    public int heartEndTime;

    @Column
    public int heartFrequency;


    public AutoMeasureData() {
    }

    public AutoMeasureData(boolean on) {
        if (on){
            tempState = 0;
            tempStartTime = 0;
            tempEndTime = 1439;
            tempFrequency = 30;

            bpState = 0;
            bpStartTime = 0;
            bpEndTime = 1439;
            bpFrequency = 30;

            spoState = 0;
            spoStartTime = 0;
            spoEndTime = 1439;
            spoFrequency = 30;

            heartState = 0;
            heartStartTime = 0;
            heartEndTime = 1439;
            heartFrequency = 30;
        }
    }
}

