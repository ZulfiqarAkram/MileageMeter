package com.zulfiqar.mileagemeter.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.zulfiqar.mileagemeter.models.Vehicle;

import java.util.List;

@Dao
public interface VehicleDao {
    @Insert
    long insert(Vehicle vehicle);

    @Update
    void update(Vehicle vehicle);

    @Delete
    void delete(Vehicle vehicle);

    @Query("SELECT * FROM vehicles")
    LiveData<List<Vehicle>> getAllVehicles();

    @Query("SELECT * FROM vehicles WHERE isActive = 1")
    LiveData<List<Vehicle>> getActiveVehicles();

    @Query("SELECT * FROM vehicles WHERE id = :id")
    LiveData<Vehicle> getVehicleById(long id);

    @Query("SELECT * FROM vehicles WHERE id = :id")
    Vehicle getVehicleByIdSync(long id);

    @Query("SELECT * FROM vehicles WHERE numberPlate = :numberPlate LIMIT 1")
    Vehicle getVehicleByNumberPlate(String numberPlate);

    @Query("SELECT * FROM vehicles WHERE numberPlate = :numberPlate AND id != :excludeId LIMIT 1")
    Vehicle getVehicleByNumberPlateExcluding(String numberPlate, long excludeId);

    @Query("DELETE FROM fill_ups WHERE vehicleId = :vehicleId")
    void deleteAllFillUpsForVehicle(long vehicleId);

    @Query("SELECT * FROM vehicles")
    List<Vehicle> getAllVehiclesSync();

    @Query("UPDATE vehicles SET isActive = 0 WHERE id = :vehicleId")
    void clearSelectedVehicle(long vehicleId);
}
