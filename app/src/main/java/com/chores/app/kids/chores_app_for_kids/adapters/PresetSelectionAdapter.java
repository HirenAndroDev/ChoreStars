package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.TaskPreset;

import java.util.List;

public class PresetSelectionAdapter extends RecyclerView.Adapter<PresetSelectionAdapter.PresetViewHolder> {

    private List<TaskPreset> presetList;
    private Context context;
    private OnPresetSelectedListener listener;

    public interface OnPresetSelectedListener {
        void onPresetSelected(TaskPreset preset);
    }

    public PresetSelectionAdapter(List<TaskPreset> presetList, Context context, OnPresetSelectedListener listener) {
        this.presetList = presetList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preset_selection, parent, false);
        return new PresetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresetViewHolder holder, int position) {
        TaskPreset preset = presetList.get(position);

        holder.tvPresetName.setText(preset.getName());
        holder.tvPresetDescription.setText(preset.getDescription());
        holder.tvStarReward.setText(String.valueOf(preset.getStarReward()));

        // Load preset icon
        if (preset.getIconUrl() != null && !preset.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(preset.getIconUrl())
                    .placeholder(R.drawable.ic_task_default)
                    .error(R.drawable.ic_task_default)
                    .into(holder.ivPresetIcon);
        } else {
            holder.ivPresetIcon.setImageResource(R.drawable.ic_task_default);
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPresetSelected(preset);
            }
        });
    }

    @Override
    public int getItemCount() {
        return presetList.size();
    }

    public static class PresetViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivPresetIcon;
        TextView tvPresetName;
        TextView tvPresetDescription;
        TextView tvStarReward;

        public PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_preset);
            ivPresetIcon = itemView.findViewById(R.id.iv_preset_icon);
            tvPresetName = itemView.findViewById(R.id.tv_preset_name);
            tvPresetDescription = itemView.findViewById(R.id.tv_preset_description);
            tvStarReward = itemView.findViewById(R.id.tv_star_reward);
        }
    }
}