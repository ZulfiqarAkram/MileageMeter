package com.zulfiqar.mileagemeter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.zulfiqar.mileagemeter.databinding.ItemFillUpBinding;
import com.zulfiqar.mileagemeter.models.FillUpWithMileage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FillUpHistoryAdapter extends ListAdapter<FillUpWithMileage, FillUpHistoryAdapter.ViewHolder> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.0");

    public FillUpHistoryAdapter() {
        super(new DiffUtil.ItemCallback<FillUpWithMileage>() {
            @Override
            public boolean areItemsTheSame(@NonNull FillUpWithMileage oldItem, @NonNull FillUpWithMileage newItem) {
                return oldItem.getFillUp().getId() == newItem.getFillUp().getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull FillUpWithMileage oldItem, @NonNull FillUpWithMileage newItem) {
                return oldItem.getFillUp().equals(newItem.getFillUp()) &&
                        oldItem.getMileage().equals(newItem.getMileage());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFillUpBinding binding = ItemFillUpBinding.inflate(
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFillUpBinding binding;

        ViewHolder(ItemFillUpBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FillUpWithMileage item) {
            // Set vehicle name and number plate
            String vehicleDisplay = item.getVehicle().getName() + " (" + 
                    item.getVehicle().getNumberPlate() + ")";
            binding.vehicleNameText.setText(vehicleDisplay);

            // Set mileage
            binding.mileageText.setText(item.getMileage());

            // Set date
            binding.dateText.setText(DATE_FORMAT.format(item.getFillUp().getDate()));

            // Set odometer reading
            String odometer = NUMBER_FORMAT.format(item.getFillUp().getOdometerReading()) + " km";
            binding.odometerText.setText(odometer);

            // Set liters filled
            String liters = NUMBER_FORMAT.format(item.getFillUp().getLiters()) + " L";
            binding.litersText.setText(liters);
        }
    }
}
