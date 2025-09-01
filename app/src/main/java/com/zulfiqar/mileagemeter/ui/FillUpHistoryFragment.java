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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.core.content.res.ResourcesCompat;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.FragmentFillUpHistoryBinding;
import com.zulfiqar.mileagemeter.ui.adapters.FillUpHistoryAdapter;
import com.zulfiqar.mileagemeter.viewmodels.FillUpHistoryViewModel;

public class FillUpHistoryFragment extends Fragment {
    private FragmentFillUpHistoryBinding binding;
    private FillUpHistoryViewModel viewModel;
    private FillUpHistoryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FillUpHistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentFillUpHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupFab();
        setupSwipeRefresh();
        observeFillUps();
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshFillUps();
        });
    }

    private void setupRecyclerView() {
        adapter = new FillUpHistoryAdapter();
        binding.fillUpsRecyclerView.setAdapter(adapter);

        // Add divider between items
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.list_divider, null));
        binding.fillUpsRecyclerView.addItemDecoration(divider);
    }

    private void setupFab() {
        binding.addFillUpFab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_fillUpHistoryFragment_to_fillUpEntryFragment)
        );
    }

    private void observeFillUps() {
        viewModel.getFillUpsWithMileage().observe(getViewLifecycleOwner(), fillUps -> {
            adapter.submitList(fillUps);
            binding.swipeRefreshLayout.setRefreshing(false);
            
            // Show/hide empty state
            if (fillUps.isEmpty()) {
                binding.emptyStateText.setVisibility(View.VISIBLE);
                binding.fillUpsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
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
