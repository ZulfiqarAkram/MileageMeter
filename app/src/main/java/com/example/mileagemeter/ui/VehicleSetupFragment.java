package com.example.mileagemeter.ui;

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

import com.example.mileagemeter.R;
import com.example.mileagemeter.databinding.FragmentVehicleSetupBinding;
import com.example.mileagemeter.models.Vehicle;
import com.example.mileagemeter.models.VehicleType;
import com.example.mileagemeter.viewmodels.VehicleSetupViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class VehicleSetupFragment extends Fragment {
    private FragmentVehicleSetupBinding binding;
    private VehicleSetupViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VehicleSetupViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentVehicleSetupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVehicleTypeDropdown();
        setupSaveButton();
    }

    private void setupVehicleTypeDropdown() {
        String[] vehicleTypes = new String[]{
                getString(R.string.vehicle_type_car),
                getString(R.string.vehicle_type_bike)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );

        AutoCompleteTextView dropdown = binding.vehicleTypeDropdown;
        dropdown.setAdapter(adapter);
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> validateAndSaveVehicle());
    }

    private void validateAndSaveVehicle() {
        clearErrors();

        String name = binding.vehicleNameInput.getText().toString().trim();
        String numberPlate = binding.numberPlateInput.getText().toString().trim();
        String vehicleTypeStr = binding.vehicleTypeDropdown.getText().toString().trim();
        String fuelCapacityStr = binding.fuelCapacityInput.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            binding.vehicleNameLayout.setError(getString(R.string.error_vehicle_name_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(numberPlate)) {
            binding.numberPlateLayout.setError(getString(R.string.error_number_plate_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(vehicleTypeStr)) {
            binding.vehicleTypeLayout.setError(getString(R.string.error_vehicle_type_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(fuelCapacityStr)) {
            binding.fuelCapacityLayout.setError(getString(R.string.error_fuel_capacity_required));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        double fuelCapacity;
        try {
            fuelCapacity = Double.parseDouble(fuelCapacityStr);
            if (fuelCapacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            binding.fuelCapacityLayout.setError(getString(R.string.error_fuel_capacity_invalid));
            return;
        }

        VehicleType vehicleType = vehicleTypeStr.equals(getString(R.string.vehicle_type_car))
                ? VehicleType.CAR : VehicleType.BIKE;

        Vehicle vehicle = new Vehicle(name, numberPlate, vehicleType, fuelCapacity);
        
        viewModel.saveVehicle(vehicle).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                // Navigate back or to next screen
                Navigation.findNavController(requireView()).navigateUp();
            } else {
                // Show error message
                String error = result.getError();
                if (error.equals("duplicate_plate")) {
                    binding.numberPlateLayout.setError(getString(R.string.error_number_plate_exists));
                } else {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearErrors() {
        binding.vehicleNameLayout.setError(null);
        binding.numberPlateLayout.setError(null);
        binding.vehicleTypeLayout.setError(null);
        binding.fuelCapacityLayout.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
