package com.zulfiqar.mileagemeter.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.zulfiqar.mileagemeter.databinding.ItemFillUpTimelineBinding;
import com.zulfiqar.mileagemeter.models.FillUp;
import com.zulfiqar.mileagemeter.utils.StatsCalculator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FillUpTimelineAdapter extends ListAdapter<FillUp, FillUpTimelineAdapter.ViewHolder> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.0");

    public FillUpTimelineAdapter() {
        super(new DiffUtil.ItemCallback<FillUp>() {
            @Override
            public boolean areItemsTheSame(@NonNull FillUp oldItem, @NonNull FillUp newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull FillUp oldItem, @NonNull FillUp newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFillUpTimelineBinding binding = ItemFillUpTimelineBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FillUp current = getItem(position);
        // For mileage calculation, we need the next fill-up (which is actually older)
        FillUp next = position < getItemCount() - 1 ? getItem(position + 1) : null;
        holder.bind(current, next, position == 0, position == getItemCount() - 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFillUpTimelineBinding binding;

        ViewHolder(ItemFillUpTimelineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FillUp fillUp, FillUp nextFillUp, boolean isFirst, boolean isLast) {
            // Set date
            binding.dateText.setText(DATE_FORMAT.format(fillUp.getDate()));

            // Set odometer
            binding.odometerText.setText(formatDistance(fillUp.getOdometerReading()));

            // Set fuel
            binding.litersText.setText(formatFuel(fillUp.getLiters()));

            // Set mileage - calculate using the next (older) fill-up
            String mileage = StatsCalculator.calculateMileage(fillUp, nextFillUp);
            binding.mileageText.setText(mileage);

            // Set timeline visibility - reversed since we're showing newest first
            binding.lineTop.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
            binding.lineBottom.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
        }

        private String formatDistance(double distance) {
            return String.format(Locale.getDefault(), "%s km", NUMBER_FORMAT.format(distance));
        }

        private String formatFuel(double fuel) {
            return String.format(Locale.getDefault(), "%s L", NUMBER_FORMAT.format(fuel));
        }
    }
}
