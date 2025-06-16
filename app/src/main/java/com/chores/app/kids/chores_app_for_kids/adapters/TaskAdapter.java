package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Context context;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskStatusChanged(Task task, boolean isCompleted);
    }

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskName.setText(task.getName());
        holder.tvStarReward.setText(String.valueOf(task.getStarReward()));

        // Set task icon - check both iconUrl and iconName
        if (task.getIconUrl() != null && !task.getIconUrl().isEmpty()) {
            // Use Glide to load icon from URL
            Glide.with(context)
                    .load(task.getIconUrl())
                    .placeholder(R.drawable.ic_task_default)
                    .error(R.drawable.ic_task_default)
                    .into(holder.ivTaskIcon);
        } else if (task.getIconName() != null && !task.getIconName().isEmpty()) {
            // Use drawable resource
            int iconResId = context.getResources().getIdentifier(task.getIconName(), "drawable", context.getPackageName());
            if (iconResId != 0) {
                holder.ivTaskIcon.setImageResource(iconResId);
            } else {
                holder.ivTaskIcon.setImageResource(R.drawable.ic_task_default);
            }
        } else {
            // Default icon
            holder.ivTaskIcon.setImageResource(R.drawable.ic_task_default);
        }

        // Set completion status
        boolean isCompleted = task.isCompleted();
        holder.cbCompleted.setChecked(isCompleted);

        // Show assigned kids count
        if (task.getAssignedKids() != null && !task.getAssignedKids().isEmpty()) {
            holder.tvAssignedKids.setVisibility(View.VISIBLE);
            holder.tvAssignedKids.setText(task.getAssignedKids().size() + " kid(s) assigned");
        } else {
            holder.tvAssignedKids.setVisibility(View.GONE);
        }

        // Show created date
        if (task.getCreatedTimestamp() > 0) {
            String dateStr = new SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(new Date(task.getCreatedTimestamp()));
            holder.tvCreatedDate.setText("Created " + dateStr);
            holder.tvCreatedDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvCreatedDate.setVisibility(View.GONE);
        }

        // Set up click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        holder.cbCompleted.setOnCheckedChangeListener(null); // Clear previous listener
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                task.setCompleted(isChecked);
                listener.onTaskStatusChanged(task, isChecked);
            }
        });

        // Visual feedback for completed tasks
        if (isCompleted) {
            holder.cardView.setAlpha(0.7f);
            holder.tvTaskName.setAlpha(0.7f);
        } else {
            holder.cardView.setAlpha(1.0f);
            holder.tvTaskName.setAlpha(1.0f);
        }

        // Show repeat type indicator
        if (task.getRepeatType() != null && !task.getRepeatType().isEmpty()) {
            holder.tvRepeatType.setVisibility(View.VISIBLE);
            switch (task.getRepeatType()) {
                case "daily":
                    holder.tvRepeatType.setText("📅 Daily");
                    break;
                case "weekly":
                    holder.tvRepeatType.setText("📆 Weekly");
                    break;
                case "weekdays":
                    holder.tvRepeatType.setText("💼 Weekdays");
                    break;
                case "weekends":
                    holder.tvRepeatType.setText("🏖️ Weekends");
                    break;
                default:
                    holder.tvRepeatType.setText("🔄 " + task.getRepeatType());
            }
        } else {
            holder.tvRepeatType.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivTaskIcon;
        TextView tvTaskName;
        TextView tvStarReward;
        TextView tvAssignedKids;
        TextView tvCreatedDate;
        TextView tvRepeatType;
        CheckBox cbCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_task);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvStarReward = itemView.findViewById(R.id.tv_star_reward);
            cbCompleted = itemView.findViewById(R.id.cb_completed);

            // Add these TextViews to the layout if they don't exist
            tvAssignedKids = itemView.findViewById(R.id.tv_assigned_kids);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvRepeatType = itemView.findViewById(R.id.tv_repeat_type);
        }
    }
}
