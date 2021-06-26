package org.sitebay.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.yarolegovich.wellsql.WellSql;
import com.yarolegovich.wellsql.WellTableManager;
import com.yarolegovich.wellsql.core.TableClass;

import org.sitebay.android.fluxc.model.AccountModel;
import org.sitebay.android.fluxc.model.PostModel;
import org.sitebay.android.fluxc.model.SiteModel;
import org.sitebay.android.fluxc.persistence.WellSqlConfig;

public class TestWellSqlConfig extends WellSqlConfig {
    private static final Class[] TABLES = {
            AccountModel.class,
            SiteModel.class,
            PostModel.class,
    };

    public TestWellSqlConfig(Context context) {
        super(context);
    }

    @Override
    public String getDbName() {
        return "test-wp-fluxc";
    }

    @Override
    public void onCreate(SQLiteDatabase db, WellTableManager helper) {
        for (Class table : TABLES) {
            helper.createTable(table);
        }
    }

    /**
     * Drop and create all tables
     */
    public void reset() {
        SQLiteDatabase db = WellSql.giveMeWritableDb();
        for (Class clazz : TABLES) {
            TableClass table = getTable(clazz);
            db.execSQL("DROP TABLE IF EXISTS " + table.getTableName());
            db.execSQL(table.createStatement());
        }
    }
}
