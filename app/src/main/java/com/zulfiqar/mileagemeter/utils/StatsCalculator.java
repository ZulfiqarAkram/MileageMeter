package com.zulfiqar.mileagemeter.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zulfiqar.mileagemeter.models.FillUp;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class StatsCalculator {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * Calculates the mileage between two fill-ups.
     * Formula: (Current Odometer – Previous Odometer) ÷ Fuel Filled
     *
     * @param currentFillUp The current fill-up entry
     * @param previousFillUp The previous fill-up entry
     * @return Formatted mileage string (km/L) or "–" if calculation not possible
     */
    public static String calculateMileage(@Nullable FillUp currentFillUp, @Nullable FillUp previousFillUp) {
        if (currentFillUp == null || previousFillUp == null) {
            return "–";
        }

        double distanceTraveled = currentFillUp.getOdometerReading() - previousFillUp.getOdometerReading();
        double fuelUsed = currentFillUp.getLiters();

        if (distanceTraveled <= 0 || fuelUsed <= 0) {
            return "–";
        }

        double mileage = distanceTraveled / fuelUsed;
        return formatMileage(mileage);
    }

    /**
     * Calculates the average mileage from a list of fill-ups
     *
     * @param fillUps List of fill-ups ordered by date (newest first)
     * @return Formatted average mileage string (km/L) or "–" if not enough data
     */
    public static String calculateAverageMileage(@NonNull List<FillUp> fillUps) {
        if (fillUps.size() < 2) {
            return "–";
        }

        double totalDistance = 0;
        double totalFuel = 0;

        // Calculate total distance and fuel used
        for (int i = 0; i < fillUps.size() - 1; i++) {
            FillUp current = fillUps.get(i);
            FillUp previous = fillUps.get(i + 1);

            double distance = current.getOdometerReading() - previous.getOdometerReading();
            if (distance > 0) {
                totalDistance += distance;
                totalFuel += current.getLiters();
            }
        }

        if (totalDistance <= 0 || totalFuel <= 0) {
            return "–";
        }

        double averageMileage = totalDistance / totalFuel;
        return formatMileage(averageMileage);
    }

    /**
     * Formats a mileage value with units
     */
    private static String formatMileage(double mileage) {
        return DECIMAL_FORMAT.format(mileage) + " km/L";
    }
}