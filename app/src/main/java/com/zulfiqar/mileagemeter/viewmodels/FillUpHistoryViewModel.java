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
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.FillUpWithMileage;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.utils.MileageCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FillUpHistoryViewModel extends AndroidViewModel {
    private final FillUpDao fillUpDao;
    private final ExecutorService executorService;
    private final MediatorLiveData<List<FillUpWithMileage>> fillUpsWithMileage;

    public FillUpHistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        fillUpDao = db.fillUpDao();
        executorService = Executors.newSingleThreadExecutor();
        fillUpsWithMileage = new MediatorLiveData<>();

        // Observe fill-ups and calculate mileage
        LiveData<List<FillUp>> fillUps = fillUpDao.getAllFillUps();
        fillUpsWithMileage.addSource(fillUps, this::calculateMileageForFillUps);
    }

    public LiveData<List<FillUpWithMileage>> getFillUpsWithMileage() {
        return fillUpsWithMileage;
    }

    public void refreshFillUps() {
        // The Room LiveData will automatically refresh when the database changes
        // but we'll provide this method for explicit refresh requests
        executorService.execute(() -> {
            // This will trigger the LiveData to update
            fillUpsWithMileage.postValue(fillUpsWithMileage.getValue());
        });
    }

    private void calculateMileageForFillUps(List<FillUp> fillUps) {
        if (fillUps == null || fillUps.isEmpty()) {
            fillUpsWithMileage.setValue(new ArrayList<>());
            return;
        }

        executorService.execute(() -> {
            List<FillUpWithMileage> result = new ArrayList<>();

            for (FillUp fillUp : fillUps) {
                // Get the vehicle for this fill-up
                Vehicle vehicle = fillUpDao.getVehicleForFillUp(fillUp.getId());
                
                // Get the previous fill-up for mileage calculation
                FillUp previousFillUp = fillUpDao.getPreviousFillUp(
                        fillUp.getVehicleId(),
                        fillUp.getId()
                );

                // Calculate mileage
                String mileage = MileageCalculator.calculateMileage(fillUp, previousFillUp);

                result.add(new FillUpWithMileage(fillUp, mileage, vehicle));
            }

            fillUpsWithMileage.postValue(result);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
