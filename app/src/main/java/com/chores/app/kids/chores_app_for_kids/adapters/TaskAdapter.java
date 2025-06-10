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
import java.util.List;

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

        // Set task icon
        int iconResId = context.getResources().getIdentifier(task.getIconName(), "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivTaskIcon.setImageResource(iconResId);
        } else {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_task_default);
        }

        // Set completion status
        boolean isCompleted = "completed".equals(task.getStatus());
        holder.cbCompleted.setChecked(isCompleted);

        // Set up click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                task.setStatus(isChecked ? "completed" : "pending");
                listener.onTaskStatusChanged(task, isChecked);
            }
        });

        // Visual feedback for completed tasks
        if (isCompleted) {
            holder.cardView.setAlpha(0.7f);
        } else {
            holder.cardView.setAlpha(1.0f);
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
        CheckBox cbCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_task);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvStarReward = itemView.findViewById(R.id.tv_star_reward);
            cbCompleted = itemView.findViewById(R.id.cb_completed);
        }
    }
}

