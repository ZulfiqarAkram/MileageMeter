package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zulfiqar.mileagemeter.data.AppDatabase;
import com.zulfiqar.mileagemeter.data.VehicleDao;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.models.VehicleType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VehicleSettingsViewModel extends AndroidViewModel {
    private final VehicleDao vehicleDao;
    private final ExecutorService executorService;
    private final MutableLiveData<Long> vehicleId = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> operationResult = new MutableLiveData<>();

    public VehicleSettingsViewModel(@NonNull Application application) {
        super(application);
        vehicleDao = AppDatabase.getInstance(application).vehicleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void setVehicleId(long id) {
        vehicleId.setValue(id);
    }

    public LiveData<Vehicle> getVehicle() {
        return vehicleDao.getVehicleById(vehicleId.getValue());
    }

    public void updateVehicle(String name, String numberPlate, VehicleType type, double fuelCapacity) {
        Long id = vehicleId.getValue();
        if (id == null) {
            operationResult.setValue(new OperationResult(false, "Invalid vehicle ID"));
            return;
        }

        executorService.execute(() -> {
            try {
                // Check if number plate exists (excluding current vehicle)
                Vehicle existing = vehicleDao.getVehicleByNumberPlateExcluding(numberPlate, id);
                if (existing != null) {
                    operationResult.postValue(new OperationResult(false, "duplicate_plate"));
                    return;
                }

                // Update vehicle
                Vehicle vehicle = vehicleDao.getVehicleByIdSync(id);
                if (vehicle != null) {
                    vehicle.setName(name);
                    vehicle.setNumberPlate(numberPlate);
                    vehicle.setType(type);
                    vehicle.setFuelTankCapacity(fuelCapacity);
                    vehicleDao.update(vehicle);
                    operationResult.postValue(new OperationResult(true, null));
                } else {
                    operationResult.postValue(new OperationResult(false, "Vehicle not found"));
                }
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, e.getMessage()));
            }
        });
    }

    public void deleteVehicleData() {
        Long id = vehicleId.getValue();
        if (id == null) {
            operationResult.setValue(new OperationResult(false, "Invalid vehicle ID"));
            return;
        }

        executorService.execute(() -> {
            try {
                // Delete all fill-ups for this vehicle
                vehicleDao.deleteAllFillUpsForVehicle(id);
                operationResult.postValue(new OperationResult(true, "reset_success"));
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, e.getMessage()));
            }
        });
    }

    public void deleteVehicle() {
        Long id = vehicleId.getValue();
        if (id == null) {
            operationResult.setValue(new OperationResult(false, "Invalid vehicle ID"));
            return;
        }

        executorService.execute(() -> {
            try {
                Vehicle vehicle = vehicleDao.getVehicleByIdSync(id);
                if (vehicle != null) {
                    // This will cascade delete fill-ups due to foreign key constraint
                    vehicleDao.delete(vehicle);
                    operationResult.postValue(new OperationResult(true, "delete_success"));
                } else {
                    operationResult.postValue(new OperationResult(false, "Vehicle not found"));
                }
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, e.getMessage()));
            }
        });
    }

    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public static class OperationResult {
        private final boolean success;
        private final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
