package com.zulfiqar.mileagemeter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zulfiqar.mileagemeter.databinding.FragmentDashboardBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.ui.adapters.VehicleStatsAdapter;
import com.zulfiqar.mileagemeter.viewmodels.DashboardViewModel;

public class FirstFragment extends Fragment implements VehicleStatsAdapter.OnVehicleClickListener {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private VehicleStatsAdapter adapter;

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupButtons();
        observeVehicleStats();
    }

    private void setupRecyclerView() {
        adapter = new VehicleStatsAdapter(this);
        binding.vehicleStatsRecyclerView.setAdapter(adapter);
        binding.vehicleStatsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupButtons() {
        binding.addVehicleButton.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navigation_dashboard_to_vehicleSetupFragment)
        );
    }

    private void observeVehicleStats() {
        viewModel.getVehicleStats().observe(getViewLifecycleOwner(), stats -> {
            adapter.submitList(stats);
            
            // Show/hide empty state
            if (stats.isEmpty()) {
                binding.emptyStateContainer.setVisibility(View.VISIBLE);
                binding.vehicleStatsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateContainer.setVisibility(View.GONE);
                binding.vehicleStatsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        // Navigate to history tab with pre-selected vehicle
        Bundle args = new Bundle();
        args.putLong("selectedVehicleId", vehicle.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.navigation_history, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}