package com.example.mileagemeter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mileagemeter.data.AppDatabase;
import com.example.mileagemeter.data.VehicleDao;
import com.example.mileagemeter.models.Vehicle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VehicleSetupViewModel extends AndroidViewModel {
    private final VehicleDao vehicleDao;
    private final ExecutorService executorService;

    public VehicleSetupViewModel(@NonNull Application application) {
        super(application);
        vehicleDao = AppDatabase.getInstance(application).vehicleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Result<Void>> saveVehicle(Vehicle vehicle) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                // Check if vehicle with same number plate exists
                Vehicle existing = vehicleDao.getVehicleByNumberPlate(vehicle.getNumberPlate());
                if (existing != null) {
                    result.postValue(new Result<>(false, "duplicate_plate"));
                    return;
                }

                // Save the vehicle
                vehicleDao.insert(vehicle);
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
