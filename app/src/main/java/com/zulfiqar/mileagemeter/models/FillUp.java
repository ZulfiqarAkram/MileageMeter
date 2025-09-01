package com.zulfiqar.mileagemeter.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
    tableName = "fill_ups",
    foreignKeys = @ForeignKey(
        entity = Vehicle.class,
        parentColumns = "id",
        childColumns = "vehicleId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("vehicleId")}
)
public class FillUp {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long vehicleId;

    private double odometerReading; // in kilometers

    private double liters;

    private Date date;

    // Default constructor required by Room
    public FillUp() {}

    public FillUp(long vehicleId, double odometerReading, double liters, Date date) {
        this.vehicleId = vehicleId;
        this.odometerReading = odometerReading;
        this.liters = liters;
        this.date = date;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public double getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(double odometerReading) {
        this.odometerReading = odometerReading;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "FillUp{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", odometerReading=" + odometerReading +
                ", liters=" + liters +
                ", date=" + date +
                '}';
    }
}
