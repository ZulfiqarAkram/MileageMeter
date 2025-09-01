package com.zulfiqar.mileagemeter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zulfiqar.mileagemeter.MainActivity;
import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.FragmentHistoryBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.ui.adapters.FillUpTimelineAdapter;
import com.zulfiqar.mileagemeter.viewmodels.HistoryViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private FillUpTimelineAdapter adapter;
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupVehicleSelector();
        setupFab();
        observeFillUps();
        
        // Handle vehicle selection from arguments or saved state
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args != null) {
                long selectedVehicleId = args.getLong("selectedVehicleId", -1L);
                if (selectedVehicleId != -1L) {
                    viewModel.setSelectedVehicle(selectedVehicleId);
                }
            }
        }
    }

    private void setupFab() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.setFabAction(() -> {
            // Get the currently selected vehicle ID
            Long selectedVehicleId = viewModel.getSelectedVehicleId().getValue();
            if (selectedVehicleId != null) {
                // Create bundle with vehicle ID
                Bundle args = new Bundle();
                args.putLong("selectedVehicleId", selectedVehicleId);
                // Navigate to fill-up entry with the selected vehicle
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navigation_history_to_fillUpEntryFragment, args);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FillUpTimelineAdapter();
        binding.fillUpsRecyclerView.setAdapter(adapter);
        binding.fillUpsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                binding.emptyStateText.setVisibility(View.VISIBLE);
                binding.fillUpsRecyclerView.setVisibility(View.GONE);
                binding.vehicleLayout.setVisibility(View.GONE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
                binding.vehicleLayout.setVisibility(View.VISIBLE);

                // Select vehicle based on viewModel's selected ID or first vehicle
                Long selectedId = viewModel.getSelectedVehicleId().getValue();
                if (selectedId != null) {
                    // Find and select the vehicle with matching ID
                    for (Vehicle vehicle : vehicles) {
                        if (vehicle.getId() == selectedId) {
                            String displayName = vehicle.getName() + " (" + vehicle.getNumberPlate() + ")";
                            dropdown.setText(displayName, false);
                            break;
                        }
                    }
                } else if (dropdown.getText().toString().isEmpty()) {
                    // No vehicle selected, use first one
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

    private void observeFillUps() {
        viewModel.getFillUps().observe(getViewLifecycleOwner(), fillUps -> {
            adapter.submitList(fillUps);
            
            // Show/hide empty state
            if (fillUps.isEmpty()) {
                binding.noDataText.setVisibility(View.VISIBLE);
                binding.fillUpsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.noDataText.setVisibility(View.GONE);
                binding.fillUpsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
