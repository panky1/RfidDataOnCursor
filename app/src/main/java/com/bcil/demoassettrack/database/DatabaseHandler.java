package com.bcil.demoassettrack.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

import com.bcil.demoassettrack.model.AssetInfoNew;


/**
 * Created by NG on 20-Jul-2017.
 */
@Database(entities = {AssetInfoNew.class}, version = 2)
public abstract class DatabaseHandler extends RoomDatabase {
    public abstract DaoAccess daoAccess();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };
}

