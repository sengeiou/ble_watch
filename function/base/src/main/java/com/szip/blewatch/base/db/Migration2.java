package com.szip.blewatch.base.db;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.UserModel;

import static com.szip.blewatch.base.db.AppDatabase.VERSION;

@Migration(version = VERSION, database = AppDatabase.class)//=2的升级
public class Migration2 extends AlterTableMigration<UserModel> {
    public Migration2(Class<UserModel> table) {
        super(table);
    }

    @Override
    public void onPreMigrate() {
        //所有Java标准的数据类型(boolean、byte、short、int、long、float、double等)及相应的包装类，
        // 以及String，当然我们还默认提供了对java.util.Date、java.sql.Date与Calendar的支持。
        addColumn(SQLiteType.get(Integer.class.getName()), "caloriePlan");//基本数据类型
        addColumn(SQLiteType.get(String.class.getName()), "deviceName");//基本数据类型
        addColumn(SQLiteType.get(String.class.getName()), "product");//基本数据类型
    }
}
