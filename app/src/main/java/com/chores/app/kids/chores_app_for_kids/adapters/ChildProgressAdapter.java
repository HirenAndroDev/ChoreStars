package com.chores.app.kids.chores_app_for_kids.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.ChildProgress;
import java.util.List;

public class ChildProgressAdapter extends RecyclerView.Adapter<ChildProgressAdapter.ViewHolder> {

    private static final String TAG = "ChildProgressAdapter";
    private List<ChildProgress> childProgressList;
    private Context context;

    public ChildProgressAdapter(List<ChildProgress> childProgressList, Context context) {
        this.childProgressList = childProgressList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChildProgress progress = childProgressList.get(position);

        Log.d(TAG, "Binding child: " + progress.getChildName() +
                " - " + progress.getCompletedTasks() + "/" + progress.getTotalTasks() +
                " tasks (" + progress.getProgressPercentage() + "%)");

        holder.tvChildName.setText(progress.getChildName());
        holder.tvTaskProgress.setText(progress.getCompletedTasks() + "/" + progress.getTotalTasks() + " tasks completed");
        holder.tvStarCount.setText(String.valueOf(progress.getTotalStars()));
        holder.tvStarsToday.setText("+" + progress.getEarnedStarsToday() + " today");

        // Configure progress bar
        holder.progressBar.setMax(100);
        // Set progress with animation
        animateProgressUpdate(holder.progressBar, progress.getProgressPercentage());
        holder.tvProgressPercentage.setText(progress.getProgressPercentage() + "%");

        // Load profile image
        if (progress.getProfileImageUrl() != null && !progress.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(progress.getProfileImageUrl())
                    .placeholder(R.drawable.ic_child)
                    .circleCrop()
                    .into(holder.ivProfileImage);
        } else {
            holder.ivProfileImage.setImageResource(R.drawable.ic_child);
        }

        // Set completion status indicator
        updateStatusIndicator(holder, progress.getProgressPercentage());
    }

    @Override
    public int getItemCount() {
        return childProgressList.size();
    }

    // Method to update specific child progress with animation
    public void updateChildProgress(String childId, ChildProgress newProgress) {
        for (int i = 0; i < childProgressList.size(); i++) {
            if (childProgressList.get(i).getChildId().equals(childId)) {
                ChildProgress oldProgress = childProgressList.get(i);
                childProgressList.set(i, newProgress);

                Log.d(TAG, "Updating progress for child: " + newProgress.getChildName() +
                        " from " + oldProgress.getProgressPercentage() + "% to " + newProgress.getProgressPercentage() + "%");

                // Notify specific item changed with animation
                notifyItemChanged(i, newProgress);
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            // Default binding
            onBindViewHolder(holder, position);
        } else {
            // Update with animation for specific changes
            ChildProgress progress = (ChildProgress) payloads.get(0);

            Log.d(TAG, "Animating update for: " + progress.getChildName() +
                    " - " + progress.getProgressPercentage() + "%");

            // Update text fields
            holder.tvTaskProgress.setText(progress.getCompletedTasks() + "/" + progress.getTotalTasks() + " tasks completed");
            holder.tvStarsToday.setText("+" + progress.getEarnedStarsToday() + " today");
            holder.tvProgressPercentage.setText(progress.getProgressPercentage() + "%");

            // Animate progress bar
            animateProgressUpdate(holder.progressBar, progress.getProgressPercentage());

            // Update status indicator
            updateStatusIndicator(holder, progress.getProgressPercentage());
        }
    }

    // Method to animate progress bar updates
    private void animateProgressUpdate(ProgressBar progressBar, int newProgress) {
        int currentProgress = progressBar.getProgress();

        if (currentProgress != newProgress) {
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, newProgress);
            animation.setDuration(800); // Smooth 800ms animation
            animation.start();

            Log.d(TAG, "Animating progress from " + currentProgress + "% to " + newProgress + "%");
        }
    }

    // Method to update status indicator based on progress
    private void updateStatusIndicator(ViewHolder holder, int progressPercentage) {
        if (progressPercentage == 100) {
            holder.ivStatusIndicator.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatusIndicator.setColorFilter(context.getColor(R.color.success_color));
        } else if (progressPercentage >= 50) {
            holder.ivStatusIndicator.setImageResource(R.drawable.ic_clock);
            holder.ivStatusIndicator.setColorFilter(context.getColor(R.color.warning_color));
        } else {
            holder.ivStatusIndicator.setImageResource(R.drawable.ic_edit);
            holder.ivStatusIndicator.setColorFilter(context.getColor(R.color.error_color));
        }
    }

    // Method to clear all data
    public void clearData() {
        int size = childProgressList.size();
        childProgressList.clear();
        notifyItemRangeRemoved(0, size);
        Log.d(TAG, "Cleared all child progress data");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChildName, tvTaskProgress, tvStarCount, tvStarsToday, tvProgressPercentage;
        ImageView ivProfileImage, ivStatusIndicator;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tv_child_name);
            tvTaskProgress = itemView.findViewById(R.id.tv_task_progress);
            tvStarCount = itemView.findViewById(R.id.tv_star_count);
            tvStarsToday = itemView.findViewById(R.id.tv_stars_today);
            tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            ivStatusIndicator = itemView.findViewById(R.id.iv_status_indicator);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
