package com.zulfiqar.mileagemeter.data;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.data.FillUpDao;

@Database(entities = {Vehicle.class, FillUp.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "mileage_meter_db";
    private static AppDatabase instance;

    public abstract VehicleDao vehicleDao();
    public abstract FillUpDao fillUpDao();

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add isActive column to vehicles table with default value true
            database.execSQL("ALTER TABLE vehicles ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .addMigrations(MIGRATION_2_3)
                    .build();
        }
        return instance;
    }
}
