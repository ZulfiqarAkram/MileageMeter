package com.zulfiqar.mileagemeter.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.zulfiqar.mileagemeter.R;
import com.zulfiqar.mileagemeter.databinding.ItemVehicleBinding;
import com.zulfiqar.mileagemeter.models.Vehicle;
import com.zulfiqar.mileagemeter.models.VehicleType;

import java.util.Locale;

public class VehicleListAdapter extends ListAdapter<Vehicle, VehicleListAdapter.ViewHolder> {
    private final OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onEditClick(Vehicle vehicle);
        void onDeleteClick(Vehicle vehicle);
    }

    public VehicleListAdapter(OnVehicleClickListener listener) {
        super(new DiffUtil.ItemCallback<Vehicle>() {
            @Override
            public boolean areItemsTheSame(@NonNull Vehicle oldItem, @NonNull Vehicle newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Vehicle oldItem, @NonNull Vehicle newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(
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
        private final ItemVehicleBinding binding;

        ViewHolder(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getItem(position));
                }
            });

            binding.deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }

        void bind(Vehicle vehicle) {
            binding.vehicleName.setText(vehicle.getName());
            binding.vehicleNumberPlate.setText(vehicle.getNumberPlate());
            binding.vehicleCapacity.setText(String.format(
                    Locale.getDefault(),
                    "%.1fL Tank Capacity",
                    vehicle.getFuelTankCapacity()
            ));

            // Set vehicle icon based on type
            binding.vehicleIcon.setImageResource(
                    vehicle.getType() == VehicleType.CAR ? R.drawable.ic_car : R.drawable.ic_bike
            );

            // Apply alpha to the whole item if inactive
            float alpha = vehicle.isActive() ? 1.0f : 0.6f;
            binding.vehicleIcon.setAlpha(alpha);
            binding.vehicleName.setAlpha(alpha);
            binding.vehicleNumberPlate.setAlpha(alpha);
            binding.vehicleCapacity.setAlpha(alpha);
        }
    }
}
