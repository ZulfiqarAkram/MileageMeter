package com.zulfiqar.mileagemeter.models;

public enum VehicleType {
    BIKE("Bike"),
    CAR("Car");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
