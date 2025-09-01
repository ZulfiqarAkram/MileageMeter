package com.zulfiqar.mileagemeter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;

import com.zulfiqar.mileagemeter.ui.VehiclesFragment;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.zulfiqar.mileagemeter.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Runnable currentFabAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Keep the splash screen visible for a bit longer
        splashScreen.setKeepOnScreenCondition(() -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                splashScreen.setKeepOnScreenCondition(() -> false);
            }, 1000);
            return true;
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Enable the Up button and show title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        
        // Set up bottom navigation and configure top-level destinations
        Set<Integer> topLevelDestinations = new HashSet<>(Arrays.asList(
                R.id.navigation_dashboard, R.id.navigation_history,
                R.id.navigation_vehicles, R.id.navigation_about
        ));
        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                .build();

        // Set up the ActionBar with the NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        
        // Set up BottomNavigationView with NavController
        if (binding.navView != null) {
            NavigationUI.setupWithNavController(binding.navView, navController);
        }

        // Update ActionBar based on destination
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            try {
                boolean isTopLevel = topLevelDestinations.contains(destination.getId());
                getSupportActionBar().setDisplayHomeAsUpEnabled(!isTopLevel);
                getSupportActionBar().setDisplayShowHomeEnabled(!isTopLevel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.fab.setOnClickListener(view -> {
            if (currentFabAction != null) {
                currentFabAction.run();
            } else {
                // Default action - add fill-up
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_navigation_dashboard_to_fillUpEntryFragment);
            }
        });

        // Listen for navigation changes to reset FAB action
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            currentFabAction = null;
            if (destination.getId() == R.id.navigation_vehicles ||
                destination.getId() == R.id.navigation_dashboard ||
                destination.getId() == R.id.navigation_history) {
                binding.fab.show();
            } else {
                binding.fab.hide();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Make sure the menu is visible
        if (menu != null) {
            menu.findItem(R.id.action_export).setVisible(true);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            exportData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportData() {
        // First navigate to vehicles fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.navigation_vehicles);
        
        // Wait for fragment to be ready
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main)
                    .getChildFragmentManager()
                    .getFragments()
                    .get(0);

            if (currentFragment instanceof VehiclesFragment) {
                ((VehiclesFragment) currentFragment).exportData();
            }
        }, 300); // Small delay to ensure fragment is ready
    }

    public void setFabAction(Runnable action) {
        this.currentFabAction = action;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}