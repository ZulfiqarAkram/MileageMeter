package com.zulfiqar.mileagemeter.ui;

import android.app.AlertDialog;
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
import com.zulfiqar.mileagemeter.databinding.FragmentVehicleSettingsBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.models.VehicleType;
import com.zulfiqar.mileagemeter.viewmodels.VehicleSettingsViewModel;

public class VehicleSettingsFragment extends Fragment {
    private FragmentVehicleSettingsBinding binding;
    private VehicleSettingsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VehicleSettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentVehicleSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get vehicle ID from arguments
        long vehicleId = VehicleSettingsFragmentArgs.fromBundle(getArguments()).getVehicleId();
        viewModel.setVehicleId(vehicleId);

        setupVehicleTypeDropdown();
        setupButtons();
        observeVehicle();
        observeOperationResult();
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

    private void setupButtons() {
        binding.saveButton.setOnClickListener(v -> validateAndSaveVehicle());
        binding.resetDataButton.setOnClickListener(v -> showResetConfirmation());
        binding.deleteVehicleButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void observeVehicle() {
        viewModel.getVehicle().observe(getViewLifecycleOwner(), vehicle -> {
            if (vehicle != null) {
                binding.vehicleNameInput.setText(vehicle.getName());
                binding.numberPlateInput.setText(vehicle.getNumberPlate());
                binding.vehicleTypeDropdown.setText(
                        vehicle.getType() == VehicleType.CAR ?
                                getString(R.string.vehicle_type_car) :
                                getString(R.string.vehicle_type_bike),
                        false
                );
                binding.fuelCapacityInput.setText(
                        String.format("%.1f", vehicle.getFuelTankCapacity())
                );
            }
        });
    }

    private void observeOperationResult() {
        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                if ("reset_success".equals(result.getMessage())) {
                    Toast.makeText(requireContext(), R.string.reset_success, Toast.LENGTH_SHORT).show();
                } else if ("delete_success".equals(result.getMessage())) {
                    Toast.makeText(requireContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Navigation.findNavController(requireView()).navigateUp();
                }
            } else {
                String error = result.getMessage();
                if ("duplicate_plate".equals(error)) {
                    binding.numberPlateLayout.setError(getString(R.string.error_number_plate_exists));
                } else {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        viewModel.updateVehicle(name, numberPlate, vehicleType, fuelCapacity);
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_fill_up_data)
                .setMessage(R.string.confirm_reset)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> viewModel.deleteVehicleData())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_vehicle)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> viewModel.deleteVehicle())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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
