package com.example.mileagemeter.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mileagemeter.models.Vehicle;

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

    @Query("SELECT * FROM vehicles WHERE id = :id")
    LiveData<Vehicle> getVehicleById(long id);

    @Query("SELECT * FROM vehicles WHERE numberPlate = :numberPlate LIMIT 1")
    Vehicle getVehicleByNumberPlate(String numberPlate);
}
