package com.zulfiqar.mileagemeter.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.FragmentFillUpEntryBinding;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.viewmodels.FillUpEntryViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FillUpEntryFragment extends Fragment {
    private FragmentFillUpEntryBinding binding;
    private FillUpEntryViewModel viewModel;
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FillUpEntryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentFillUpEntryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVehicleDropdown();
        setupDatePicker();
        setupSaveButton();
        
        // Set default date to current date
        setDate(new Date());

        // Handle pre-selected vehicle if passed
        Bundle args = getArguments();
        if (args != null) {
            long selectedVehicleId = args.getLong("selectedVehicleId", -1);
            if (selectedVehicleId != -1) {
                viewModel.getVehicleById(selectedVehicleId).observe(getViewLifecycleOwner(), vehicle -> {
                    if (vehicle != null) {
                        String displayName = vehicle.getName() + " (" + vehicle.getNumberPlate() + ")";
                        binding.vehicleDropdown.setText(displayName, false);
                        vehicleMap.put(displayName, vehicle);
                    }
                });
            }
        }
    }

    private void setupVehicleDropdown() {
        viewModel.getAllVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            vehicleMap.clear();
            String[] vehicleNames = new String[vehicles.size()];
            
            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle vehicle = vehicles.get(i);
                String displayName = vehicle.getName() + " (" + vehicle.getNumberPlate() + ")";
                vehicleNames[i] = displayName;
                vehicleMap.put(displayName, vehicle);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    vehicleNames
            );

            AutoCompleteTextView dropdown = binding.vehicleDropdown;
            dropdown.setAdapter(adapter);
        });
    }

    private void setupDatePicker() {
        binding.dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        setDate(calendar.getTime());
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setDate(Date date) {
        calendar.setTime(date);
        binding.dateInput.setText(dateFormat.format(date));
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> validateAndSaveFillUp());
    }

    private void validateAndSaveFillUp() {
        clearErrors();

        String vehicleStr = binding.vehicleDropdown.getText().toString().trim();
        String odometerStr = binding.odometerInput.getText().toString().trim();
        String litersStr = binding.litersInput.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(vehicleStr) || !vehicleMap.containsKey(vehicleStr)) {
            binding.vehicleLayout.setError(getString(R.string.error_vehicle_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(odometerStr)) {
            binding.odometerLayout.setError(getString(R.string.error_odometer_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(litersStr)) {
            binding.litersLayout.setError(getString(R.string.error_liters_required));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        double odometer;
        try {
            odometer = Double.parseDouble(odometerStr);
            if (odometer < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            binding.odometerLayout.setError(getString(R.string.error_odometer_invalid));
            return;
        }

        double liters;
        try {
            liters = Double.parseDouble(litersStr);
            if (liters <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            binding.litersLayout.setError(getString(R.string.error_liters_invalid));
            return;
        }

        Vehicle selectedVehicle = vehicleMap.get(vehicleStr);
        FillUp fillUp = new FillUp(
                selectedVehicle.getId(),
                odometer,
                liters,
                calendar.getTime()
        );

        viewModel.saveFillUp(fillUp).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                // Navigate back
                Navigation.findNavController(requireView()).navigateUp();
            } else {
                // Show error message
                String error = result.getError();
                if ("odometer_lower_than_previous".equals(error)) {
                    binding.odometerLayout.setError(getString(R.string.error_odometer_lower_than_previous));
                } else {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearErrors() {
        binding.vehicleLayout.setError(null);
        binding.odometerLayout.setError(null);
        binding.litersLayout.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
