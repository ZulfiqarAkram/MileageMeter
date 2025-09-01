package com.zulfiqar.mileagemeter.models;

public class FillUpWithMileage {
    private final FillUp fillUp;
    private final String mileage;
    private final Vehicle vehicle;

    public FillUpWithMileage(FillUp fillUp, String mileage, Vehicle vehicle) {
        this.fillUp = fillUp;
        this.mileage = mileage;
        this.vehicle = vehicle;
    }

    public FillUp getFillUp() {
        return fillUp;
    }

    public String getMileage() {
        return mileage;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
