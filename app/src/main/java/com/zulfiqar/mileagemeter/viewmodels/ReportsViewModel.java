package com.zulfiqar.mileagemeter.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

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
import com.zulfiqar.mileagemeter.utils.ChartDataGenerator;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.PieData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportsViewModel extends AndroidViewModel {
    private final FillUpDao fillUpDao;
    private final VehicleDao vehicleDao;
    private final ExecutorService executorService;
    private final MutableLiveData<Long> selectedVehicleId = new MutableLiveData<>();
    private final MediatorLiveData<LineData> mileageLineData = new MediatorLiveData<>();
    private final MediatorLiveData<PieData> fuelPieData = new MediatorLiveData<>();
    private final MutableLiveData<ExportResult> exportResult = new MutableLiveData<>();

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        fillUpDao = db.fillUpDao();
        vehicleDao = db.vehicleDao();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize with first vehicle if available
        vehicleDao.getAllVehicles().observeForever(vehicles -> {
            if (vehicles != null && !vehicles.isEmpty() && selectedVehicleId.getValue() == null) {
                selectedVehicleId.setValue(vehicles.get(0).getId());
            }
        });

        // Update charts when vehicle changes
        selectedVehicleId.observeForever(vehicleId -> {
            if (vehicleId != null) {
                setupChartObservers(vehicleId);
            }
        });
    }

    private void setupChartObservers(long vehicleId) {
        // Mileage line chart
        LiveData<List<FillUp>> fillUpsForLine = fillUpDao.getFillUpsForReports(vehicleId);
        mileageLineData.addSource(fillUpsForLine, fillUps -> {
            if (fillUps != null) {
                LineData data = ChartDataGenerator.generateMileageLineData(getApplication(), fillUps);
                mileageLineData.setValue(data);
            }
        });

        // Fuel consumption pie chart
        LiveData<List<FillUp>> fillUpsForPie = fillUpDao.getLastSixMonthsFillUps(vehicleId);
        fuelPieData.addSource(fillUpsForPie, fillUps -> {
            if (fillUps != null) {
                PieData data = ChartDataGenerator.generateFuelPieData(getApplication(), fillUps);
                fuelPieData.setValue(data);
            }
        });
    }

    public void exportToCsv() {
        Long vehicleId = selectedVehicleId.getValue();
        if (vehicleId == null) {
            exportResult.setValue(new ExportResult(false, null));
            return;
        }

        executorService.execute(() -> {
            try {
                // Get vehicle details
                Vehicle vehicle = vehicleDao.getVehicleByIdSync(vehicleId);
                if (vehicle == null) {
                    exportResult.postValue(new ExportResult(false, null));
                    return;
                }

                // Get fill-ups
                List<FillUp> fillUps = fillUpDao.getFillUpsForExport(vehicleId);
                String csvData = ChartDataGenerator.generateCsvData(fillUps);

                // Create file
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = String.format("mileage_%s_%s.csv", 
                        vehicle.getNumberPlate().toLowerCase(Locale.getDefault()), timestamp);
                File file = new File(downloadsDir, fileName);

                // Write data
                FileWriter writer = new FileWriter(file);
                writer.write(csvData);
                writer.close();

                exportResult.postValue(new ExportResult(true, file.getAbsolutePath()));
            } catch (IOException e) {
                exportResult.postValue(new ExportResult(false, null));
            }
        });
    }

    public LiveData<List<Vehicle>> getVehicles() {
        return vehicleDao.getAllVehicles();
    }

    public void setSelectedVehicle(long vehicleId) {
        selectedVehicleId.setValue(vehicleId);
    }

    public LiveData<Long> getSelectedVehicleId() {
        return selectedVehicleId;
    }

    public LiveData<LineData> getMileageLineData() {
        return mileageLineData;
    }

    public LiveData<PieData> getFuelPieData() {
        return fuelPieData;
    }

    public LiveData<ExportResult> getExportResult() {
        return exportResult;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public static class ExportResult {
        private final boolean success;
        private final String filePath;

        public ExportResult(boolean success, String filePath) {
            this.success = success;
            this.filePath = filePath;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
