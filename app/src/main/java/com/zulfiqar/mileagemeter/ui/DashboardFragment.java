package com.zulfiqar.mileagemeter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.FragmentDashboardBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.ui.adapters.VehicleStatsAdapter;
import com.zulfiqar.mileagemeter.viewmodels.DashboardViewModel;

public class DashboardFragment extends Fragment implements VehicleStatsAdapter.OnVehicleClickListener {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private VehicleStatsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeVehicleStats();
    }

    private void setupRecyclerView() {
        adapter = new VehicleStatsAdapter(this);
        binding.vehicleStatsRecyclerView.setAdapter(adapter);
        binding.vehicleStatsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void observeVehicleStats() {
        viewModel.getVehicleStats().observe(getViewLifecycleOwner(), stats -> {
            adapter.submitList(stats);
            
            // Show/hide empty state
            if (stats.isEmpty()) {
                binding.emptyStateText.setVisibility(View.VISIBLE);
                binding.vehicleStatsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
                binding.vehicleStatsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        // Navigate to history tab with pre-selected vehicle
        Bundle args = new Bundle();
        args.putLong("selectedVehicleId", vehicle.getId());
        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        Navigation.findNavController(requireView())
                .navigate(R.id.navigation_history, args, navOptions);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
