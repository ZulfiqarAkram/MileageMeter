package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zulfiqar.mileagemeter.data.AppDatabase;
import com.zulfiqar.mileagemeter.data.FillUpDao;
import com.zulfiqar.mileagemeter.data.VehicleDao;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FillUpEntryViewModel extends AndroidViewModel {
    private final FillUpDao fillUpDao;
    private final VehicleDao vehicleDao;
    private final ExecutorService executorService;

    public FillUpEntryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        fillUpDao = db.fillUpDao();
        vehicleDao = db.vehicleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Vehicle>> getAllVehicles() {
        return vehicleDao.getActiveVehicles();
    }

    public LiveData<Vehicle> getVehicleById(long vehicleId) {
        return vehicleDao.getVehicleById(vehicleId);
    }

    public LiveData<Result<Void>> saveFillUp(FillUp fillUp) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                // Get the latest fill-up for validation
                FillUp latestFillUp = fillUpDao.getLatestFillUpForVehicle(fillUp.getVehicleId());
                
                // Validate odometer reading if there's a previous fill-up
                if (latestFillUp != null && fillUp.getOdometerReading() <= latestFillUp.getOdometerReading()) {
                    result.postValue(new Result<>(false, "odometer_lower_than_previous"));
                    return;
                }

                // Save the fill-up
                fillUpDao.insert(fillUp);
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
