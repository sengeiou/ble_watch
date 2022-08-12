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
public class StepData extends BaseModel implements Comparable<StepData>{
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public int steps;

    @Column
    public int distance;

    @Column
    public int calorie;

    @Column
    public String dataForHour;

    @Column
    public String deviceCode;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getSteps() {
        return steps;
    }

    public int getDistance() {
        return distance;
    }

    public int getCalorie() {
        return calorie;
    }

    public String getDataForHour() {
        return dataForHour;
    }

    public StepData(long time, int steps, int distance, int calorie, String dataForHour) {
        this.time = time;
        this.steps = steps;
        this.distance = distance;
        this.calorie = calorie;
        this.dataForHour = dataForHour;
    }

    public StepData() {}

    @Override
    public int compareTo(@NonNull StepData o) {
        return (int)(this.time-o.time);
    }
}
