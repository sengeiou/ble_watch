package com.szip.blewatch.base.db.dbModel;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

@Table(database = AppDatabase.class)
public class AnimalHeatData extends BaseModel implements Comparable<AnimalHeatData>{

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public int tempData;

    @Column
    public String deviceCode;

    public AnimalHeatData(long time, int tempData) {
        this.time = time;
        this.tempData = tempData;
    }

    public AnimalHeatData() {}

    public long getTime() {
        return time;
    }

    public int animalHeatData() {
        return tempData;
    }

    @Override
    public int compareTo(@NonNull AnimalHeatData o) {
        return (int)(this.tempData-o.tempData);
    }

}
