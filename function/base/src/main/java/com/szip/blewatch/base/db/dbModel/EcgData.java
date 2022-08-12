package com.szip.blewatch.base.db.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

/**
 * Created by Administrator on 2019/12/30.
 */
@Table(database = AppDatabase.class)
public class EcgData extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public String heart;

    @Column
    public String deviceCode;

    public long getTime() {
        return time;
    }

    public EcgData(long time, String heart) {
        this.time = time;
        this.heart = heart;
    }

    public EcgData() {
    }
}

