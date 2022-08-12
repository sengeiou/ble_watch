package com.szip.blewatch.base.db;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;


import static com.szip.blewatch.base.db.AppDatabase.VERSION;

@Migration(version = VERSION, database = AppDatabase.class)//=2的升级
public class Migration1 extends AlterTableMigration<SportWatchAppFunctionConfigDTO> {
    public Migration1(Class<SportWatchAppFunctionConfigDTO> table) {
        super(table);
    }

    @Override
    public void onPreMigrate() {
        //所有Java标准的数据类型(boolean、byte、short、int、long、float、double等)及相应的包装类，
        // 以及String，当然我们还默认提供了对java.util.Date、java.sql.Date与Calendar的支持。
        addColumn(SQLiteType.get(String.class.getName()), "mac");//基本数据类型
        addColumn(SQLiteType.get(String.class.getName()), "productImg");//基本数据类型
        addColumn(SQLiteType.get(String.class.getName()), "dialImg");//基本数据类型
        addColumn(SQLiteType.get(Boolean.class.getName()), "cameraSwitch");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "heartRateAutoTest");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "ecgAutoTest");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "bloodOxygenAutoTest");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "bloodPressureAutoTest");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "stepCounterAutoTest");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "temperatureAutoTest");//基本数据类型
        addColumn(SQLiteType.get(Byte.class.getName()), "sleepAutoTest");//基本数据类型

    }
}
