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
public class BloodOxygenData extends BaseModel implements Comparable<BloodOxygenData>{

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public int bloodOxygenData;

    @Column
    public String deviceCode;

    public BloodOxygenData(long time, int bloodOxygenData) {
        this.time = time;
        this.bloodOxygenData = bloodOxygenData;
    }

    public BloodOxygenData() {}

    public long getTime() {
        return time;
    }

    public int getBloodOxygenData() {
        return bloodOxygenData;
    }

    @Override
    public int compareTo(@NonNull BloodOxygenData o) {
        return (int)(this.bloodOxygenData-o.bloodOxygenData);
    }


}
