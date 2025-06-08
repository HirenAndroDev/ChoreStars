package com.chores.app.kids.chores_app_for_kids.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskClickListener listener;
    private boolean isKidView;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskComplete(Task task);
        void onTaskEdit(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener, boolean isKidView) {
        this.tasks = tasks;
        this.listener = listener;
        this.isKidView = isKidView;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTaskIcon;
        private TextView tvTaskName;
        private TextView tvTaskDescription;
        private TextView tvStarReward;
        private Button btnComplete;
        private Button btnEdit;
        private View completionIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvStarReward = itemView.findViewById(R.id.tv_star_reward);
            btnComplete = itemView.findViewById(R.id.btn_complete);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            completionIndicator = itemView.findViewById(R.id.completion_indicator);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTaskClick(tasks.get(position));
                    }
                }
            });

            btnComplete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTaskComplete(tasks.get(position));
                    }
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTaskEdit(tasks.get(position));
                    }
                }
            });
        }

        public void bind(Task task) {
            tvTaskName.setText(task.getName());
            tvTaskDescription.setText(task.getDescription());
            tvStarReward.setText(String.valueOf(task.getStarsPerCompletion()));

            // Set task icon based on task type
            setTaskIcon(task.getIcon());

            // Configure view based on user type
            if (isKidView) {
                btnEdit.setVisibility(View.GONE);
                btnComplete.setVisibility(View.VISIBLE);
            } else {
                btnEdit.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.GONE);
            }

            // Set repeat frequency indicator
            setRepeatIndicator(task.getRepeatFrequency());
        }

        private void setTaskIcon(String iconName) {
            int iconResource = R.drawable.ic_task_default;

            if (iconName != null) {
                switch (iconName) {
                    case Constants.ICON_BRUSH_TEETH:
                        iconResource = R.drawable.ic_brush_teeth;
                        break;
                    case Constants.ICON_PUT_AWAY_TOYS:
                        iconResource = R.drawable.ic_toys;
                        break;
                    case Constants.ICON_FOLD_CLOTHES:
                        iconResource = R.drawable.ic_clothes;
                        break;
                    case Constants.ICON_DO_LAUNDRY:
                        iconResource = R.drawable.ic_laundry;
                        break;
                    default:
                        iconResource = R.drawable.ic_task_default;
                        break;
                }
            }

            ivTaskIcon.setImageResource(iconResource);
        }

        private void setRepeatIndicator(String repeatFrequency) {
            // You can add visual indicators for repeat frequency here
            // For now, we'll just store it for future use
        }
    }
}