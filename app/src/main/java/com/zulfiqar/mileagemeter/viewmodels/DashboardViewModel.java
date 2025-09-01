package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.zulfiqar.mileagemeter.data.AppDatabase;
import com.zulfiqar.mileagemeter.data.FillUpDao;
import com.zulfiqar.mileagemeter.data.VehicleDao;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.ui.adapters.VehicleStatsAdapter.VehicleStats;
import com.zulfiqar.mileagemeter.utils.StatsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends AndroidViewModel {
    private final VehicleDao vehicleDao;
    private final FillUpDao fillUpDao;
    private final ExecutorService executorService;
    private final MediatorLiveData<List<VehicleStats>> vehicleStats;
    private final MutableLiveData<Long> selectedVehicleId = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        vehicleDao = db.vehicleDao();
        fillUpDao = db.fillUpDao();
        executorService = Executors.newSingleThreadExecutor();
        vehicleStats = new MediatorLiveData<>();

        // Observe active vehicles and fill-ups to update stats
        LiveData<List<Vehicle>> activeVehicles = vehicleDao.getActiveVehicles();
        LiveData<List<FillUp>> allFillUps = fillUpDao.getAllFillUps();
        
        // Update stats when either vehicles or fill-ups change
        vehicleStats.addSource(activeVehicles, this::updateVehicleStats);
        vehicleStats.addSource(allFillUps, fillUps -> {
            // Trigger stats update when fill-ups change
            updateVehicleStats(activeVehicles.getValue());
        });
    }

    private void updateVehicleStats(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            vehicleStats.setValue(new ArrayList<>());
            return;
        }

        executorService.execute(() -> {
            List<VehicleStats> stats = new ArrayList<>();

            for (Vehicle vehicle : vehicles) {
                // Get fill-ups for this vehicle
                List<FillUp> fillUps = fillUpDao.getFillUpsForExport(vehicle.getId());
                if (fillUps == null || fillUps.isEmpty()) {
                    stats.add(new VehicleStats(vehicle, "–", "–", 0, 0));
                    continue;
                }

                // Calculate stats
                String lastMileage = "–";
                String averageMileage;
                double totalDistance = 0;
                double monthlyFuel = 0;

                // Last mileage
                if (fillUps.size() >= 2) {
                    FillUp current = fillUps.get(0);
                    FillUp previous = fillUps.get(1);
                    lastMileage = StatsCalculator.calculateMileage(current, previous);
                }

                // Average mileage
                averageMileage = StatsCalculator.calculateAverageMileage(fillUps);

                // Total distance
                if (fillUps.size() >= 2) {
                    FillUp first = fillUps.get(fillUps.size() - 1);
                    FillUp last = fillUps.get(0);
                    totalDistance = last.getOdometerReading() - first.getOdometerReading();
                }

                // Monthly fuel
                List<FillUp> monthlyFillUps = fillUpDao.getFillUpsForCurrentMonth(vehicle.getId());
                monthlyFuel = 0.0; // Reset before calculating
                if (monthlyFillUps != null && !monthlyFillUps.isEmpty()) {
                    for (FillUp fillUp : monthlyFillUps) {
                        monthlyFuel += fillUp.getLiters();
                    }
                }

                stats.add(new VehicleStats(vehicle, lastMileage, averageMileage, totalDistance, monthlyFuel));
            }

            vehicleStats.postValue(stats);
        });
    }

    public LiveData<List<VehicleStats>> getVehicleStats() {
        return vehicleStats;
    }

    public LiveData<List<Vehicle>> getVehicles() {
        return vehicleDao.getActiveVehicles();
    }

    public LiveData<Long> getSelectedVehicleId() {
        return selectedVehicleId;
    }

    public void setSelectedVehicle(long vehicleId) {
        selectedVehicleId.setValue(vehicleId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}