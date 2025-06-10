package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import java.util.List;

public class KidTaskAdapter extends RecyclerView.Adapter<KidTaskAdapter.KidTaskViewHolder> {

    private List<Task> taskList;
    private Context context;
    private OnTaskCompletedListener listener;

    public interface OnTaskCompletedListener {
        void onTaskCompleted(Task task);
    }

    public KidTaskAdapter(List<Task> taskList, Context context, OnTaskCompletedListener listener) {
        this.taskList = taskList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KidTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kid_task, parent, false);
        return new KidTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidTaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskName.setText(task.getName());
        holder.tvStarReward.setText("+" + task.getStarReward() + " ⭐");

        // Set large task icon
        int iconResId = context.getResources().getIdentifier(task.getIconName(), "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivTaskIcon.setImageResource(iconResId);
        } else {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_task_default);
        }

        // Check if task is already completed
        boolean isCompleted = "completed".equals(task.getStatus());
        if (isCompleted) {
            holder.btnComplete.setText("Completed!");
            holder.btnComplete.setEnabled(false);
            holder.cardView.setAlpha(0.7f);
            holder.ivTaskIcon.setAlpha(0.7f);
        } else {
            holder.btnComplete.setText("Mark Done");
            holder.btnComplete.setEnabled(true);
            holder.cardView.setAlpha(1.0f);
            holder.ivTaskIcon.setAlpha(1.0f);
        }

        // Set up completion listener
        holder.btnComplete.setOnClickListener(v -> {
            if (!isCompleted && listener != null) {
                listener.onTaskCompleted(task);
                // Update UI immediately
                task.setStatus("completed");
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class KidTaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivTaskIcon;
        TextView tvTaskName;
        TextView tvStarReward;
        Button btnComplete;

        public KidTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_kid_task);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon_large);
            tvTaskName = itemView.findViewById(R.id.tv_task_name_large);
            tvStarReward = itemView.findViewById(R.id.tv_star_reward_large);
            btnComplete = itemView.findViewById(R.id.btn_complete_task);
        }
    }
}
