package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.zulfiqar.mileagemeter.data.AppDatabase;
import com.zulfiqar.mileagemeter.data.FillUpDao;
import com.zulfiqar.mileagemeter.data.VehicleDao;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final VehicleDao vehicleDao;
    private final FillUpDao fillUpDao;
    private static final MutableLiveData<Long> selectedVehicleId = new MutableLiveData<>();
    private final LiveData<List<FillUp>> fillUps;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        vehicleDao = db.vehicleDao();
        fillUpDao = db.fillUpDao();

        // Transform selected vehicle ID into fill-ups
        fillUps = Transformations.switchMap(selectedVehicleId,
                vehicleId -> vehicleId != null ? fillUpDao.getFillUpsForReports(vehicleId) : null);
    }

    public LiveData<List<Vehicle>> getVehicles() {
        return vehicleDao.getActiveVehicles();
    }

    public void setSelectedVehicle(long vehicleId) {
        selectedVehicleId.setValue(vehicleId);
    }

    public LiveData<List<FillUp>> getFillUps() {
        return fillUps;
    }

    public LiveData<Long> getSelectedVehicleId() {
        return selectedVehicleId;
    }
}
