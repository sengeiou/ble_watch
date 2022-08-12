package com.szip.blewatch.base.db.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

@Table(database = AppDatabase.class)
public class HealthyCardData extends BaseModel implements Comparable<HealthyCardData>{

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public int type;//1:心率 2:计步 3:睡眠 4:心电 5:血氧 6:血压 7:体温

    @Column
    public boolean state;

    @Column
    public int sort;

    public HealthyCardData(int type, boolean state, int sort) {
        this.type = type;
        this.state = state;
        this.sort = sort;
    }

    public HealthyCardData() {
    }

    @Override
    public int compareTo(HealthyCardData o) {
        return (int)(this.sort-o.sort);
    }


}
