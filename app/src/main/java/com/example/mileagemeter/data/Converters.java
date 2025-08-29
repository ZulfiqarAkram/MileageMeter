package com.example.mileagemeter.data;

import androidx.room.TypeConverter;

import com.example.mileagemeter.models.VehicleType;

public class Converters {
    @TypeConverter
    public static VehicleType toVehicleType(String value) {
        return value == null ? null : VehicleType.valueOf(value);
    }

    @TypeConverter
    public static String fromVehicleType(VehicleType type) {
        return type == null ? null : type.name();
    }
}
