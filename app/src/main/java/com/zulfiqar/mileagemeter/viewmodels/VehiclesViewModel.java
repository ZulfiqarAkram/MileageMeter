package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.zulfiqar.mileagemeter.data.AppDatabase;
import com.zulfiqar.mileagemeter.data.FillUpDao;
import com.zulfiqar.mileagemeter.data.VehicleDao;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.utils.StatsCalculator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VehiclesViewModel extends AndroidViewModel {
    private final VehicleDao vehicleDao;
    private final FillUpDao fillUpDao;
    private final ExecutorService executorService;

    public VehiclesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        vehicleDao = db.vehicleDao();
        fillUpDao = db.fillUpDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Vehicle>> getVehicles() {
        return vehicleDao.getAllVehicles();
    }

    public void deleteVehicle(Vehicle vehicle) {
        executorService.execute(() -> {
            // Delete all fill-ups first
            vehicleDao.deleteAllFillUpsForVehicle(vehicle.getId());
            // Then delete the vehicle
            vehicleDao.delete(vehicle);
        });
    }

    private static final String TAG = "VehiclesViewModel";

    public void exportToCSV(Context context) {
        Log.d(TAG, "Starting CSV export...");
        executorService.execute(() -> {
            try {
                // Check if external storage is available
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    throw new IOException("External storage is not available");
                }
                Log.d(TAG, "External storage is available");

                // Get all vehicles and their fill-ups
                List<Vehicle> vehicles = vehicleDao.getAllVehiclesSync();
                Log.d(TAG, "Retrieved " + vehicles.size() + " vehicles");
                
                StringBuilder csv = new StringBuilder();
                csv.append("Vehicle Name,Number Plate,Type,Fuel Capacity,Fill-up Date,Odometer,Liters Filled,Mileage\n");

                for (Vehicle vehicle : vehicles) {
                    List<FillUp> fillUps = fillUpDao.getFillUpsForVehicleSync(vehicle.getId());
                    Log.d(TAG, "Retrieved " + fillUps.size() + " fill-ups for vehicle " + vehicle.getName());
                    FillUp previousFillUp = null;

                    for (FillUp fillUp : fillUps) {
                        String mileage = StatsCalculator.calculateMileage(fillUp, previousFillUp);
                        csv.append(String.format(Locale.US,
                                "%s,%s,%s,%.1f,%s,%.1f,%.1f,%s\n",
                                vehicle.getName(),
                                vehicle.getNumberPlate(),
                                vehicle.getType(),
                                vehicle.getFuelTankCapacity(),
                                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fillUp.getDate()),
                                fillUp.getOdometerReading(),
                                fillUp.getLiters(),
                                mileage));
                        previousFillUp = fillUp;
                    }
                }

                // Save to Downloads folder
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists() && !path.mkdirs()) {
                    throw new IOException("Failed to create Downloads directory");
                }
                Log.d(TAG, "Downloads directory ready: " + path.getAbsolutePath());

                String fileName = "mileage_data_" + 
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + 
                        ".csv";
                File file = new File(path, fileName);
                Log.d(TAG, "Writing to file: " + file.getAbsolutePath());

                FileWriter writer = new FileWriter(file);
                writer.write(csv.toString());
                writer.close();

                // Show success message
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, 
                        "Data exported to Downloads/" + fileName, 
                        Toast.LENGTH_LONG).show()
                );

                Log.d(TAG, "CSV file written successfully");

            } catch (IOException e) {
                Log.e(TAG, "Failed to export data", e);
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, 
                        "Failed to export data: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
