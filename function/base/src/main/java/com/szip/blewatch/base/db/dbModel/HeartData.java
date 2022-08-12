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
public class HeartData extends BaseModel implements Comparable<HeartData>{

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public int averageHeart;

    @Column
    public String heartArray;

    @Column
    public String deviceCode;

    public long getTime() {
        return time;
    }

    public int getAverageHeart() {
        return averageHeart;
    }

    public String getHeartArray() {
        return heartArray;
    }


    public HeartData(long time, int averageHeart) {
        this.time = time;
        this.averageHeart = averageHeart;
    }

    public HeartData(long time, int averageHeart, String heartArray) {
        this.time = time;
        this.averageHeart = averageHeart;
        this.heartArray = heartArray;
    }

    public HeartData() {}

    @Override
    public int compareTo(@NonNull HeartData o) {
        return (int)(this.averageHeart-o.averageHeart);
    }

}
