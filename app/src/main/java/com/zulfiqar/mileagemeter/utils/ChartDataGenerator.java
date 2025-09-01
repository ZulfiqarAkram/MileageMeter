package com.zulfiqar.mileagemeter.utils;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChartDataGenerator {
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
    private static final int MAX_MONTHS = 6;

    /**
     * Generates line chart data for mileage over time
     */
    public static LineData generateMileageLineData(@NonNull Context context, @NonNull List<FillUp> fillUps) {
        List<Entry> entries = new ArrayList<>();
        
        // We need at least 2 fill-ups to calculate mileage
        if (fillUps.size() < 2) {
            return new LineData();
        }

        // Calculate mileage points
        for (int i = 0; i < fillUps.size() - 1; i++) {
            FillUp current = fillUps.get(i);
            FillUp previous = fillUps.get(i + 1);

            double distance = current.getOdometerReading() - previous.getOdometerReading();
            if (distance > 0) {
                double mileage = distance / current.getLiters();
                // Use days since first fill-up as X-axis
                long days = TimeUnit.MILLISECONDS.toDays(
                        current.getDate().getTime() - fillUps.get(fillUps.size() - 1).getDate().getTime()
                );
                entries.add(new Entry(days, (float) mileage));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, context.getString(R.string.mileage_km_l));
        dataSet.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(ContextCompat.getColor(context, R.color.colorPrimary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return new LineData(dataSet);
    }

    /**
     * Generates pie chart data for fuel consumption by month
     */
    public static PieData generateFuelPieData(@NonNull Context context, @NonNull List<FillUp> fillUps) {
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Float> monthlyFuel = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        
        // Get last MAX_MONTHS months of data
        for (FillUp fillUp : fillUps) {
            calendar.setTime(fillUp.getDate());
            String month = MONTH_FORMAT.format(calendar.getTime());
            monthlyFuel.merge(month, (float) fillUp.getLiters(), Float::sum);
        }

        // Convert to pie entries
        for (Map.Entry<String, Float> entry : monthlyFuel.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, context.getString(R.string.fuel_consumption));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f L", value);
            }
        });

        return new PieData(dataSet);
    }

    /**
     * Generates CSV data for export
     */
    public static String generateCsvData(@NonNull List<FillUp> fillUps) {
        StringBuilder csv = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Header
        csv.append("Date,Odometer (km),Fuel (L),Mileage (km/L)\n");

        // Data rows
        for (int i = 0; i < fillUps.size(); i++) {
            FillUp current = fillUps.get(i);
            String mileage = "–";

            // Calculate mileage if we have a previous fill-up
            if (i < fillUps.size() - 1) {
                FillUp previous = fillUps.get(i + 1);
                double distance = current.getOdometerReading() - previous.getOdometerReading();
                if (distance > 0) {
                    double mileageValue = distance / current.getLiters();
                    mileage = String.format(Locale.getDefault(), "%.2f", mileageValue);
                }
            }

            csv.append(String.format(Locale.getDefault(),
                    "%s,%.1f,%.1f,%s\n",
                    dateFormat.format(current.getDate()),
                    current.getOdometerReading(),
                    current.getLiters(),
                    mileage
            ));
        }

        return csv.toString();
    }
}
