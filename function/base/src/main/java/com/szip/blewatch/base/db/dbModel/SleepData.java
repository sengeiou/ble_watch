package com.szip.blewatch.base.db.dbModel;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

/**
 * Created by Administrator on 2019/12/28.
 */

@Table(database = AppDatabase.class)
public class SleepData extends BaseModel implements Comparable<SleepData>{

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public int deepTime;

    @Column
    public int lightTime;

    @Column
    public String dataForHour;

    @Column
    public String deviceCode;


    public long getTime() {
        return time;
    }

    public int getDeepTime() {
        return deepTime;
    }

    public int getLightTime() {
        return lightTime;
    }

    public SleepData(long time, int deepTime, int lightTime, String dataForHour) {
        this.time = time;
        this.deepTime = deepTime;
        this.lightTime = lightTime;
        this.dataForHour = dataForHour;
    }

    public SleepData() {}

    @Override
    public int compareTo(@NonNull SleepData o) {
        return (int)(this.time-o.time);
    }

}
