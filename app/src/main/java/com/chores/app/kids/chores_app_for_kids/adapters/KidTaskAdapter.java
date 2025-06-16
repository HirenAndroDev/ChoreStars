package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import java.util.List;

public class KidTaskAdapter extends RecyclerView.Adapter<KidTaskAdapter.KidTaskViewHolder> {

    private List<Task> taskList;
    private Context context;
    private OnTaskInteractionListener listener;

    public interface OnTaskInteractionListener {
        void onTaskCompleted(Task task);

        void onTaskClicked(Task task);
    }

    public KidTaskAdapter(List<Task> taskList, Context context, OnTaskInteractionListener listener) {
        this.taskList = taskList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KidTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kid_task_new, parent, false);
        return new KidTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidTaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set task name
        holder.tvTaskName.setText(task.getName());

        // Set star reward
        holder.tvStarReward.setText(String.valueOf(task.getStarReward()));

        // Set task notes if available
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            holder.tvTaskNotes.setText(task.getNotes());
            holder.tvTaskNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvTaskNotes.setVisibility(View.GONE);
        }

        // Set reminder time if available
        if (task.getReminderTime() != null && !task.getReminderTime().trim().isEmpty()) {
            holder.tvReminderTime.setText(task.getReminderTime());
            holder.layoutReminderTime.setVisibility(View.VISIBLE);
        } else {
            holder.layoutReminderTime.setVisibility(View.GONE);
        }

        // Set task icon
        String iconName = task.getIconName();
        if (iconName != null && !iconName.isEmpty()) {
            if (task.getIconUrl() != null && !task.getIconUrl().isEmpty()) {
                // Load from URL
                Glide.with(context)
                        .load(task.getIconUrl())
                        .placeholder(R.drawable.ic_task_default)
                        .error(R.drawable.ic_task_default)
                        .into(holder.ivTaskIcon);
            } else {
                // Try to load from drawable
                int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                if (iconResId != 0) {
                    holder.ivTaskIcon.setImageResource(iconResId);
                } else {
                    holder.ivTaskIcon.setImageResource(R.drawable.ic_task_default);
                }
            }
        } else {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_task_default);
        }

        // Set completion status
        boolean isCompleted = "completed".equals(task.getStatus());
        updateTaskAppearance(holder, isCompleted);

        // Set click listeners
        holder.ivTaskCheckbox.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskCompleted(task);
            }
        });

        holder.layoutTaskContent.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClicked(task);
            }
        });
    }

    private void updateTaskAppearance(KidTaskViewHolder holder, boolean isCompleted) {
        String status = taskList.get(holder.getAdapterPosition()).getStatus();

        if ("completing".equals(status)) {
            // Show loading state for completing
            holder.itemView.setBackgroundResource(R.drawable.bg_task_item_incomplete);
            holder.ivTaskCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked);
            holder.ivTaskCheckbox.setBackgroundResource(R.drawable.bg_checkbox_circle);
            holder.ivTaskCheckbox.setAlpha(0.5f);

            // Slightly transparent to show loading
            holder.tvTaskName.setAlpha(0.7f);
            holder.tvStarReward.setAlpha(0.7f);
            holder.tvTaskNotes.setAlpha(0.7f);
            holder.tvReminderTime.setAlpha(0.7f);

        } else if ("uncompleting".equals(status)) {
            // Show loading state for uncompleting
            holder.itemView.setBackgroundResource(R.drawable.bg_task_item_complete);
            holder.ivTaskCheckbox.setImageResource(R.drawable.ic_checkbox_checked);
            holder.ivTaskCheckbox.setBackgroundResource(R.drawable.bg_checkbox_circle_checked);
            holder.ivTaskCheckbox.setAlpha(0.5f);

            // Slightly transparent to show loading
            holder.tvTaskName.setAlpha(0.7f);
            holder.tvStarReward.setAlpha(0.7f);
            holder.tvTaskNotes.setAlpha(0.7f);
            holder.tvReminderTime.setAlpha(0.7f);

        } else if (isCompleted) {
            // Completed task appearance
            holder.itemView.setBackgroundResource(R.drawable.bg_task_item_complete);
            holder.ivTaskCheckbox.setImageResource(R.drawable.ic_checkbox_checked);
            holder.ivTaskCheckbox.setBackgroundResource(R.drawable.bg_checkbox_circle_checked);
            holder.ivTaskCheckbox.setAlpha(1.0f);

            // Make text slightly transparent
            holder.tvTaskName.setAlpha(0.8f);
            holder.tvStarReward.setAlpha(0.8f);
            holder.tvTaskNotes.setAlpha(0.8f);
            holder.tvReminderTime.setAlpha(0.8f);
        } else {
            // Incomplete task appearance
            holder.itemView.setBackgroundResource(R.drawable.bg_task_item_incomplete);
            holder.ivTaskCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked);
            holder.ivTaskCheckbox.setBackgroundResource(R.drawable.bg_checkbox_circle);
            holder.ivTaskCheckbox.setAlpha(1.0f);

            // Full opacity
            holder.tvTaskName.setAlpha(1.0f);
            holder.tvStarReward.setAlpha(1.0f);
            holder.tvTaskNotes.setAlpha(1.0f);
            holder.tvReminderTime.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class KidTaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTaskCheckbox;
        LinearLayout layoutTaskContent;
        ImageView ivTaskIcon;
        TextView tvTaskName;
        TextView tvTaskNotes;
        TextView tvStarReward;
        LinearLayout layoutReminderTime;
        TextView tvReminderTime;

        public KidTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTaskCheckbox = itemView.findViewById(R.id.iv_task_checkbox);
            layoutTaskContent = itemView.findViewById(R.id.layout_task_content);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvTaskNotes = itemView.findViewById(R.id.tv_task_notes);
            tvStarReward = itemView.findViewById(R.id.tv_star_reward);
            layoutReminderTime = itemView.findViewById(R.id.layout_reminder_time);
            tvReminderTime = itemView.findViewById(R.id.tv_reminder_time);
        }
    }
}
