package com.zulfiqar.mileagemeter.data;

import androidx.room.TypeConverter;

import com.zulfiqar.mileagemeter.models.VehicleType;

import java.util.Date;

public class Converters {
    @TypeConverter
    public static VehicleType toVehicleType(String value) {
        return value == null ? null : VehicleType.valueOf(value);
    }

    @TypeConverter
    public static String fromVehicleType(VehicleType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
}
