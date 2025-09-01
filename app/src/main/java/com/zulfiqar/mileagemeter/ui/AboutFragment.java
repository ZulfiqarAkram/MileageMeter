package com.zulfiqar.mileagemeter.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zulfiqar.mileagemeter.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {
    private FragmentAboutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVersion();
        setupButtons();
    }

    private void setupVersion() {
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            binding.versionText.setText(getString(com.zulfiqar.mileagemeter.R.string.version_format, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            binding.versionText.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        // No buttons to setup anymore
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
