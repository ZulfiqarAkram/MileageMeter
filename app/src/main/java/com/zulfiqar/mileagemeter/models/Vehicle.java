package com.zulfiqar.mileagemeter.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class Vehicle {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    @NonNull
    private String numberPlate;

    @NonNull
    private VehicleType type;

    private double fuelTankCapacity; // in liters

    private boolean isActive = true; // default to true

    // Default constructor required by Room
    public Vehicle() {}

    public Vehicle(@NonNull String name, @NonNull String numberPlate, 
                  @NonNull VehicleType type, double fuelTankCapacity) {
        this.name = name;
        this.numberPlate = numberPlate;
        this.type = type;
        this.fuelTankCapacity = fuelTankCapacity;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getNumberPlate() {
        return numberPlate;
    }

    public void setNumberPlate(@NonNull String numberPlate) {
        this.numberPlate = numberPlate;
    }

    @NonNull
    public VehicleType getType() {
        return type;
    }

    public void setType(@NonNull VehicleType type) {
        this.type = type;
    }

    public double getFuelTankCapacity() {
        return fuelTankCapacity;
    }

    public void setFuelTankCapacity(double fuelTankCapacity) {
        this.fuelTankCapacity = fuelTankCapacity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numberPlate='" + numberPlate + '\'' +
                ", type=" + type +
                ", fuelTankCapacity=" + fuelTankCapacity +
                '}';
    }
}
