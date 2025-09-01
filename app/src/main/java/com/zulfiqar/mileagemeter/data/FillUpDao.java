package com.zulfiqar.mileagemeter.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;

import java.util.List;

@Dao
public interface FillUpDao {
    @Insert
    long insert(FillUp fillUp);

    @Update
    void update(FillUp fillUp);

    @Delete
    void delete(FillUp fillUp);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC")
    LiveData<List<FillUp>> getFillUpsForVehicle(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE id = :id")
    LiveData<FillUp> getFillUpById(long id);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC LIMIT 1")
    FillUp getLatestFillUpForVehicle(long vehicleId);

    @Query("SELECT * FROM fill_ups ORDER BY date DESC")
    LiveData<List<FillUp>> getAllFillUps();

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId AND date < (SELECT date FROM fill_ups WHERE id = :fillUpId) ORDER BY date DESC LIMIT 1")
    FillUp getPreviousFillUp(long vehicleId, long fillUpId);

    @Query("SELECT v.* FROM vehicles v INNER JOIN fill_ups f ON v.id = f.vehicleId WHERE f.id = :fillUpId")
    Vehicle getVehicleForFillUp(long fillUpId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC LIMIT 2")
    LiveData<List<FillUp>> getLastTwoFillUps(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId AND date >= date('now', 'start of month') ORDER BY date DESC")
    LiveData<List<FillUp>> getCurrentMonthFillUps(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC")
    LiveData<List<FillUp>> getAllFillUpsForStats(long vehicleId);

    // Queries for Reports
    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC")
    LiveData<List<FillUp>> getFillUpsForReports(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId AND date >= date('now', '-6 months') ORDER BY date ASC")
    LiveData<List<FillUp>> getLastSixMonthsFillUps(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC")
    List<FillUp> getFillUpsForExport(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId ORDER BY date DESC")
    List<FillUp> getFillUpsForVehicleSync(long vehicleId);

    @Query("SELECT * FROM fill_ups WHERE vehicleId = :vehicleId " +
           "AND date >= strftime('%s000', 'now', 'start of month') " +
           "AND date <= strftime('%s000', 'now', 'localtime') " +
           "ORDER BY date DESC")
    List<FillUp> getFillUpsForCurrentMonth(long vehicleId);
}
