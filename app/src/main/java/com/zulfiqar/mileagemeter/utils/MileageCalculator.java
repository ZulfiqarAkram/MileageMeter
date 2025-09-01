package com.zulfiqar.mileagemeter.utils;

import androidx.annotation.Nullable;

import com.zulfiqar.mileagemeter.models.FillUp;

import java.text.DecimalFormat;

public class MileageCalculator {
    private static final DecimalFormat MILEAGE_FORMAT = new DecimalFormat("#.##");

    /**
     * Calculates the mileage between two fill-ups.
     * Formula: (Current Odometer – Previous Odometer) ÷ Fuel Filled
     *
     * @param currentFillUp The current fill-up entry
     * @param previousFillUp The previous fill-up entry (can be null for first entry)
     * @return Formatted mileage string (km/L) or "–" if no previous fill-up exists
     */
    public static String calculateMileage(@Nullable FillUp currentFillUp, @Nullable FillUp previousFillUp) {
        if (currentFillUp == null || previousFillUp == null) {
            return "–";
        }

        double distanceTraveled = currentFillUp.getOdometerReading() - previousFillUp.getOdometerReading();
        double fuelUsed = currentFillUp.getLiters();

        // Validate the values
        if (distanceTraveled <= 0 || fuelUsed <= 0) {
            return "–";
        }

        double mileage = distanceTraveled / fuelUsed;
        return MILEAGE_FORMAT.format(mileage) + " km/L";
    }

    /**
     * Formats a mileage value into a display string
     *
     * @param mileage The mileage value to format
     * @return Formatted mileage string with unit
     */
    public static String formatMileage(double mileage) {
        return MILEAGE_FORMAT.format(mileage) + " km/L";
    }
}
