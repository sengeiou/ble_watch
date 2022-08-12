package com.szip.blewatch.base.db.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.db.AppDatabase;

@Table(database = AppDatabase.class)
public class UserModel extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String birthday;
    @Column
    public int height;
    @Column
    public int weight;
    @Column
    public String phoneNumber;
    @Column
    public String email;
    @Column
    public String deviceCode;
    @Column
    public String product;
    @Column
    public String areaCode;
    @Column
    public String userName;
    @Column
    public String lastName;
    @Column
    public String firstName;
    @Column
    public String avatar;
    @Column
    public int sex;
    @Column
    public String nation;
    @Column
    public int unit;
    @Column
    public int heightBritish;
    @Column
    public int weightBritish;
    @Column
    public String bindId;
    @Column
    public int stepsPlan;
    @Column
    public int sleepPlan;
    @Column
    public int caloriePlan;
    @Column
    public int tempUnit;


    public UserModel() {
    }


    public UserModel(int height, int weight, int unit, int heightBritish, int weightBritish, int stepsPlan, int tempUnit,int sex) {
        this.height = height;
        this.weight = weight;
        this.unit = unit;
        this.heightBritish = heightBritish;
        this.weightBritish = weightBritish;
        this.stepsPlan = stepsPlan;
        this.tempUnit = tempUnit;
        this.sex = sex;
    }
}
