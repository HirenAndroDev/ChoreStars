package com.chores.app.kids.chores_app_for_kids.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.WeekDay;

import java.util.List;

public class DayPagerAdapter extends RecyclerView.Adapter<DayPagerAdapter.DayViewHolder> {

    private List<WeekDay> weekDays;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(int position);
    }

    public DayPagerAdapter(List<WeekDay> weekDays, OnDayClickListener listener) {
        this.weekDays = weekDays;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_pager, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        WeekDay day = weekDays.get(position);
        holder.tvDayName.setText(day.getDayName());
        holder.tvDayNumber.setText(String.valueOf(day.getDayNumber()));

        if (day.isSelected()) {
            holder.tvDayNumber.setBackgroundResource(R.drawable.bg_day_circle_selected);
            holder.tvDayNumber.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        } else {
            holder.tvDayNumber.setBackgroundResource(R.drawable.bg_day_circle_normal);
            holder.tvDayNumber.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weekDays.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}