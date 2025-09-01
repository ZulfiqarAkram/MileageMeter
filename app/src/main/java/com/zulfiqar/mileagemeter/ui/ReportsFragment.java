package com.zulfiqar.mileagemeter.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.FragmentReportsBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.viewmodels.ReportsViewModel;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {
    private FragmentReportsBinding binding;
    private ReportsViewModel viewModel;
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    exportData();
                } else {
                    Toast.makeText(requireContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        setupCharts();
        setupVehicleSelector();
        setupExportButton();
        observeChartData();
        observeExportResult();
    }

    private void setupCharts() {
        // Setup Line Chart
        binding.mileageChart.setTouchEnabled(true);
        binding.mileageChart.setDragEnabled(true);
        binding.mileageChart.setScaleEnabled(true);
        binding.mileageChart.setPinchZoom(true);
        binding.mileageChart.getAxisLeft().setSpaceTop(15f);
        binding.mileageChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.mileageChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("Day %d", (int) value);
            }
        });
        Description lineDesc = new Description();
        lineDesc.setText("");
        binding.mileageChart.setDescription(lineDesc);

        // Setup Pie Chart
        binding.fuelChart.setUsePercentValues(false);
        binding.fuelChart.getDescription().setEnabled(false);
        binding.fuelChart.setDrawHoleEnabled(true);
        binding.fuelChart.setHoleColor(android.R.color.transparent);
        binding.fuelChart.setTransparentCircleRadius(0f);
        binding.fuelChart.setDrawEntryLabels(true);
        binding.fuelChart.setEntryLabelTextSize(12f);
    }

    private void setupVehicleSelector() {
        viewModel.getVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            vehicleMap.clear();
            List<String> vehicleNames = new ArrayList<>();

            for (Vehicle vehicle : vehicles) {
                String displayName = vehicle.getName() + " (" + vehicle.getNumberPlate() + ")";
                vehicleNames.add(displayName);
                vehicleMap.put(displayName, vehicle);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    vehicleNames
            );

            AutoCompleteTextView dropdown = binding.vehicleDropdown;
            dropdown.setAdapter(adapter);

            // Show/hide UI based on vehicle availability
            if (vehicles.isEmpty()) {
                binding.noVehicleText.setVisibility(View.VISIBLE);
                binding.chartsContainer.setVisibility(View.GONE);
                binding.vehicleLayout.setVisibility(View.GONE);
                binding.exportFab.setVisibility(View.GONE);
            } else {
                binding.noVehicleText.setVisibility(View.GONE);
                binding.chartsContainer.setVisibility(View.VISIBLE);
                binding.vehicleLayout.setVisibility(View.VISIBLE);
                binding.exportFab.setVisibility(View.VISIBLE);

                // Select first vehicle if none selected
                if (dropdown.getText().toString().isEmpty()) {
                    dropdown.setText(vehicleNames.get(0), false);
                    viewModel.setSelectedVehicle(vehicles.get(0).getId());
                }
            }
        });

        binding.vehicleDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = parent.getItemAtPosition(position).toString();
            Vehicle selectedVehicle = vehicleMap.get(selectedName);
            if (selectedVehicle != null) {
                viewModel.setSelectedVehicle(selectedVehicle.getId());
            }
        });
    }

    private void setupExportButton() {
        binding.exportFab.setOnClickListener(v -> checkPermissionAndExport());
    }

    private void checkPermissionAndExport() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED) {
            exportData();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void exportData() {
        viewModel.exportToCsv();
    }

    private void observeChartData() {
        // Line Chart Data
        viewModel.getMileageLineData().observe(getViewLifecycleOwner(), lineData -> {
            binding.mileageChart.setData(lineData);
            binding.mileageChart.invalidate();
        });

        // Pie Chart Data
        viewModel.getFuelPieData().observe(getViewLifecycleOwner(), pieData -> {
            binding.fuelChart.setData(pieData);
            binding.fuelChart.invalidate();
        });
    }

    private void observeExportResult() {
        viewModel.getExportResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(requireContext(), 
                        getString(R.string.export_success), 
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), 
                        getString(R.string.export_failed), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
