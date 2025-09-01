package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zulfiqar.mileagemeter.data.AppDatabase;
import com.zulfiqar.mileagemeter.data.VehicleDao;
import com.zulfiqar.mileagemeter.models.Vehicle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VehicleSetupViewModel extends AndroidViewModel {
    private final VehicleDao vehicleDao;
    private final ExecutorService executorService;
    private final MutableLiveData<Vehicle> vehicle = new MutableLiveData<>();
    private Long currentVehicleId;

    public VehicleSetupViewModel(@NonNull Application application) {
        super(application);
        vehicleDao = AppDatabase.getInstance(application).vehicleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void loadVehicle(long vehicleId) {
        currentVehicleId = vehicleId;
        executorService.execute(() -> {
            Vehicle loadedVehicle = vehicleDao.getVehicleByIdSync(vehicleId);
            vehicle.postValue(loadedVehicle);
        });
    }

    public LiveData<Vehicle> getVehicle() {
        return vehicle;
    }

    public LiveData<Result<Void>> saveVehicle(Vehicle vehicle) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                // Check if vehicle with same number plate exists (excluding current vehicle if editing)
                Vehicle existing = currentVehicleId == null ?
                        vehicleDao.getVehicleByNumberPlate(vehicle.getNumberPlate()) :
                        vehicleDao.getVehicleByNumberPlateExcluding(vehicle.getNumberPlate(), currentVehicleId);

                if (existing != null) {
                    result.postValue(new Result<>(false, "duplicate_plate"));
                    return;
                }

                // Save or update the vehicle
                if (currentVehicleId != null) {
                    vehicle.setId(currentVehicleId);
                    vehicleDao.update(vehicle);
                    
                    // If vehicle is deactivated, clear it from selected vehicle
                    if (!vehicle.isActive()) {
                        executorService.execute(() -> {
                            Vehicle current = vehicleDao.getVehicleByIdSync(currentVehicleId);
                            if (current != null && !current.isActive()) {
                                // Clear any references to this vehicle in preferences or state
                                // This will trigger LiveData updates in other ViewModels
                                vehicleDao.clearSelectedVehicle(currentVehicleId);
                            }
                        });
                    }
                } else {
                    vehicleDao.insert(vehicle);
                }
                result.postValue(new Result<>(true, null));
            } catch (Exception e) {
                result.postValue(new Result<>(false, e.getMessage()));
            }
        });

        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public static class Result<T> {
        private final boolean success;
        private final String error;
        private final T data;

        public Result(boolean success, String error) {
            this(success, error, null);
        }

        public Result(boolean success, String error, T data) {
            this.success = success;
            this.error = error;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }

        public T getData() {
            return data;
        }
    }
}
