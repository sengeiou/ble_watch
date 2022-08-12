package com.szip.blewatch.base.db.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

@Table(database = AppDatabase.class)
public class Weather extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String city;
    @Column
    public float elevation;
    @Column
    public String weatherList;
}
