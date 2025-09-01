package com.zulfiqar.mileagemeter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.ItemVehicleStatsBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.models.VehicleType;
import com.zulfiqar.mileagemeter.utils.StatsCalculator;

import java.text.DecimalFormat;
import java.util.Locale;

public class VehicleStatsAdapter extends ListAdapter<VehicleStatsAdapter.VehicleStats, VehicleStatsAdapter.ViewHolder> {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.0");
    private final OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    public VehicleStatsAdapter(OnVehicleClickListener listener) {
        super(new DiffUtil.ItemCallback<VehicleStats>() {
            @Override
            public boolean areItemsTheSame(@NonNull VehicleStats oldItem, @NonNull VehicleStats newItem) {
                return oldItem.vehicle.getId() == newItem.vehicle.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull VehicleStats oldItem, @NonNull VehicleStats newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleStatsBinding binding = ItemVehicleStatsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVehicleStatsBinding binding;

        ViewHolder(ItemVehicleStatsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VehicleStats stats) {
            // Set click listener
            binding.getRoot().setOnClickListener(v -> listener.onVehicleClick(stats.vehicle));

            // Set vehicle info
            binding.vehicleNameText.setText(stats.vehicle.getName());
            binding.vehicleNumberText.setText(stats.vehicle.getNumberPlate());
            
            // Set vehicle type icon
            binding.vehicleTypeIcon.setImageResource(
                stats.vehicle.getType() == VehicleType.CAR ? R.drawable.ic_car : R.drawable.ic_bike
            );

            // Set mileage stats
            binding.lastMileageText.setText(stats.lastMileage);
            binding.averageMileageText.setText(stats.averageMileage);

            // Set distance and fuel
            binding.totalDistanceText.setText(formatDistance(stats.totalDistance));
            binding.monthlyFuelText.setText(formatFuel(stats.monthlyFuel));
        }

        private String formatDistance(double distance) {
            return String.format(Locale.getDefault(), "%s km", NUMBER_FORMAT.format(distance));
        }

        private String formatFuel(double fuel) {
            return String.format(Locale.getDefault(), "%s L", NUMBER_FORMAT.format(fuel));
        }
    }

    public static class VehicleStats {
        public final Vehicle vehicle;
        public final String lastMileage;
        public final String averageMileage;
        public final double totalDistance;
        public final double monthlyFuel;

        public VehicleStats(Vehicle vehicle, String lastMileage, String averageMileage,
                          double totalDistance, double monthlyFuel) {
            this.vehicle = vehicle;
            this.lastMileage = lastMileage;
            this.averageMileage = averageMileage;
            this.totalDistance = totalDistance;
            this.monthlyFuel = monthlyFuel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VehicleStats that = (VehicleStats) o;
            return Double.compare(that.totalDistance, totalDistance) == 0 &&
                    Double.compare(that.monthlyFuel, monthlyFuel) == 0 &&
                    vehicle.equals(that.vehicle) &&
                    lastMileage.equals(that.lastMileage) &&
                    averageMileage.equals(that.averageMileage);
        }
    }
}
