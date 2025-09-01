package com.zulfiqar.mileagemeter.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;

import com.zulfiqar.mileagemeter.MainActivity;
import com.zulfiqar.mileagemeter.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zulfiqar.mileagemeter.databinding.FragmentVehiclesBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.ui.adapters.VehicleListAdapter;
import com.zulfiqar.mileagemeter.viewmodels.VehiclesViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class VehiclesFragment extends Fragment implements VehicleListAdapter.OnVehicleClickListener {
    private static final String TAG = "VehiclesFragment";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private FragmentVehiclesBinding binding;
    private VehiclesViewModel viewModel;
    private VehicleListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VehiclesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentVehiclesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupAddButton();
        observeVehicles();
    }

    private void setupRecyclerView() {
        adapter = new VehicleListAdapter(this);
        binding.vehiclesRecyclerView.setAdapter(adapter);
        binding.vehiclesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupAddButton() {
        // Use the global FAB from MainActivity
        MainActivity activity = (MainActivity) requireActivity();
        activity.setFabAction(() -> 
            Navigation.findNavController(requireView()).navigate(
                R.id.action_navigation_vehicles_to_vehicleSetupFragment
            )
        );
    }

    private void observeVehicles() {
        viewModel.getVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            adapter.submitList(vehicles);
            
            // Show/hide empty state
            if (vehicles.isEmpty()) {
                binding.emptyStateText.setVisibility(View.VISIBLE);
                binding.vehiclesRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
                binding.vehiclesRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEditClick(Vehicle vehicle) {
        Bundle args = new Bundle();
        args.putLong("vehicleId", vehicle.getId());
        Navigation.findNavController(requireView()).navigate(
                R.id.action_navigation_vehicles_to_vehicleSetupFragment,
                args
        );
    }

    @Override
    public void onDeleteClick(Vehicle vehicle) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to delete this vehicle? This will also delete all associated fill-up records.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteVehicle(vehicle))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            if (requireContext().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                }, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            // Android 12 and below
            if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        // If we have permissions, proceed with export
        startExport();
    }

    private void startExport() {
        Log.d(TAG, "Attempting to export data...");
        
        // Show loading indicator
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Exporting Data");
        builder.setMessage("Please wait while your data is being exported...");
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Create a handler to dismiss the dialog after export
        Handler handler = new Handler(Looper.getMainLooper());
        viewModel.exportToCSV(requireContext());
        handler.postDelayed(dialog::dismiss, 1000); // Dismiss after 1 second minimum
    }

    public void exportData() {
        checkAndRequestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Log.d(TAG, "All permissions granted by user");
                startExport();
            } else {
                Log.d(TAG, "Some permissions denied by user");
                Toast.makeText(requireContext(),
                    "Storage permissions are required to export data. Please grant them from App Settings.",
                    Toast.LENGTH_LONG).show();
                
                // Show dialog to open app settings
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Permissions Required")
                    .setMessage("Storage permissions are required to export data. Would you like to open App Settings to grant them?")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
