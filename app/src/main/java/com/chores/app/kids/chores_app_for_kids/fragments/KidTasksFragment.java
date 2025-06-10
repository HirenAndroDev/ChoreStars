package com.chores.app.kids.chores_app_for_kids.fragments;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.KidDashboardActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.KidTaskAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class KidTasksFragment extends Fragment {

    private RecyclerView recyclerViewTasks;
    private TextView tvTasksTitle;
    private TextView tvTasksCompleted;
    private TextView tvEmptyMessage;
    private LinearLayout layoutProgress;
    private LinearLayout layoutEmptyState;
    private ImageView ivProgressIcon;

    private KidTaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> completedTasks;
    private String childId;
    private String familyId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kid_tasks, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadUserData();
        loadTasks();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewTasks = view.findViewById(R.id.recycler_view_kid_tasks);
        tvTasksTitle = view.findViewById(R.id.tv_tasks_title);
        tvTasksCompleted = view.findViewById(R.id.tv_tasks_completed);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        layoutProgress = view.findViewById(R.id.layout_progress);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        ivProgressIcon = view.findViewById(R.id.iv_progress_icon);

        // Set time-based greeting
        setTimeBasedTitle();
    }

    private void setTimeBasedTitle() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String title;
        if (hour < 12) {
            title = "Good Morning Tasks! 🌅";
        } else if (hour < 17) {
            title = "Afternoon Adventures! ☀️";
        } else {
            title = "Evening Activities! 🌙";
        }

        tvTasksTitle.setText(title);
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        completedTasks = new ArrayList<>();

        taskAdapter = new KidTaskAdapter(taskList, getContext(), new KidTaskAdapter.OnTaskCompletedListener() {
            @Override
            public void onTaskCompleted(Task task) {
                handleTaskCompletion(task);
            }
        });

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);

        // Add item spacing for better visual separation
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        recyclerViewTasks.addItemDecoration(new SpacingItemDecoration(spacing));
    }

    private void loadUserData() {
        childId = AuthHelper.getCurrentUserId();
        familyId = AuthHelper.getFamilyId(getContext());
    }

    private void loadTasks() {
        if (childId == null || familyId == null) {
            showEmptyState("Oops! Let's reload your tasks.");
            return;
        }

        FirebaseHelper.getTasksForChild(childId, familyId, new FirebaseHelper.TasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateTaskList(tasks);
                        checkForCompletedTasks(tasks);
                        updateProgressDisplay();
                        announceTaskStatus();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showEmptyState("Let's try loading your tasks again!");
                    });
                }
            }
        });
    }

    private void updateTaskList(List<Task> tasks) {
        taskList.clear();
        completedTasks.clear();

        for (Task task : tasks) {
            if ("completed".equals(task.getStatus())) {
                completedTasks.add(task);
            } else {
                taskList.add(task);
            }
        }

        taskAdapter.notifyDataSetChanged();

        // Show appropriate view
        if (taskList.isEmpty() && completedTasks.isEmpty()) {
            showEmptyState("No tasks today! Great job! 🎉");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }
    }

    private void checkForCompletedTasks(List<Task> allTasks) {
        // Check if all tasks are completed
        boolean allCompleted = !allTasks.isEmpty() &&
                allTasks.stream().allMatch(task -> "completed".equals(task.getStatus()));

        if (allCompleted) {
            showAllTasksCompletedCelebration();
        }
    }

    private void updateProgressDisplay() {
        int totalTasks = taskList.size() + completedTasks.size();
        int completed = completedTasks.size();

        if (totalTasks > 0) {
            layoutProgress.setVisibility(View.VISIBLE);
            tvTasksCompleted.setText(String.format("%d of %d tasks completed!", completed, totalTasks));

            // Update progress icon based on completion
            float progress = (float) completed / totalTasks;
            updateProgressIcon(progress);
        } else {
            layoutProgress.setVisibility(View.GONE);
        }
    }

    private void updateProgressIcon(float progress) {
        if (progress == 0f) {
            ivProgressIcon.setImageResource(R.drawable.ic_star_empty);
            ivProgressIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
        } else if (progress < 0.5f) {
            ivProgressIcon.setImageResource(R.drawable.ic_star_half);
            ivProgressIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.star_yellow)));
        } else if (progress < 1.0f) {
            ivProgressIcon.setImageResource(R.drawable.ic_star_three_quarter);
            ivProgressIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.star_yellow)));
        } else {
            ivProgressIcon.setImageResource(R.drawable.ic_star_full);
            ivProgressIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.star_yellow)));
            animateProgressIcon();
        }
    }

    private void animateProgressIcon() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivProgressIcon, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivProgressIcon, "scaleY", 1f, 1.3f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(ivProgressIcon, "rotation", 0f, 360f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.setDuration(1000);
        animatorSet.start();
    }

    private void handleTaskCompletion(Task task) {
        // Show loading state on the task
        task.setStatus("completing");
        taskAdapter.notifyDataSetChanged();

        // Complete task in Firebase
        FirebaseHelper.completeTask(task.getTaskId(), childId, result -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (result.isSuccessful()) {
                        // Task completed successfully
                        task.setStatus("completed");

                        // Move task to completed list
                        taskList.remove(task);
                        completedTasks.add(task);
                        taskAdapter.notifyDataSetChanged();

                        // Update progress and celebrate
                        updateProgressDisplay();
                        celebrateTaskCompletion(task);

                        // Notify parent activity
                        if (getActivity() instanceof KidDashboardActivity) {
                            ((KidDashboardActivity) getActivity()).onTaskCompleted(task);
                        }

                        // Check if all tasks are completed
                        if (taskList.isEmpty() && !completedTasks.isEmpty()) {
                            showAllTasksCompletedCelebration();
                        }

                    } else {
                        // Task completion failed
                        task.setStatus("pending");
                        taskAdapter.notifyDataSetChanged();
                        SoundHelper.playErrorSound(getContext());

                        if (getActivity() instanceof KidDashboardActivity) {
                            ((KidDashboardActivity) getActivity()).announceIfEnabled(
                                    "Oops! Something went wrong. Let's try again!");
                        }
                    }
                });
            }
        });
    }

    private void celebrateTaskCompletion(Task task) {
        // Play celebration sound
        SoundHelper.playTaskCompleteSound(getContext());

        // Show celebration animation
        showTaskCompletionAnimation(task);

        // Announce completion
        if (getActivity() instanceof KidDashboardActivity) {
            ((KidDashboardActivity) getActivity()).announceTaskCompletion(task.getName(), task.getStarReward());
        }
    }

    private void showTaskCompletionAnimation(Task task) {
        // Create floating star animation
        View celebrationView = LayoutInflater.from(getContext())
                .inflate(R.layout.task_completion_celebration, null);

        TextView tvTaskName = celebrationView.findViewById(R.id.tv_completed_task_name);
        TextView tvStarsEarned = celebrationView.findViewById(R.id.tv_stars_earned);

        tvTaskName.setText(task.getName());
        tvStarsEarned.setText(String.format("+%d ⭐", task.getStarReward()));

        // Add to parent view with animation
        if (getActivity() != null && getActivity().findViewById(R.id.fragment_container_kid) != null) {
            ViewGroup parent = getActivity().findViewById(R.id.fragment_container_kid);
            parent.addView(celebrationView);

            // Animate celebration view
            celebrationView.setAlpha(0f);
            celebrationView.setScaleX(0.5f);
            celebrationView.setScaleY(0.5f);

            celebrationView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        // Remove after delay
                        celebrationView.postDelayed(() -> {
                            celebrationView.animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .withEndAction(() -> parent.removeView(celebrationView))
                                    .start();
                        }, 2000);
                    })
                    .start();
        }
    }

    private void showAllTasksCompletedCelebration() {
        // Big celebration for completing all tasks
        SoundHelper.playCelebrationSound(getContext());

        if (getActivity() instanceof KidDashboardActivity) {
            ((KidDashboardActivity) getActivity()).announceIfEnabled(
                    "Amazing! You completed all your tasks today! You're a superstar!");
        }

        // Show special all-complete animation
        showAllCompleteAnimation();
    }

    private void showAllCompleteAnimation() {
        // Create confetti or fireworks animation
        View allCompleteView = LayoutInflater.from(getContext())
                .inflate(R.layout.all_tasks_complete_celebration, null);

        if (getActivity() != null) {
            ViewGroup parent = getActivity().findViewById(android.R.id.content);
            parent.addView(allCompleteView);

            // Auto-remove after celebration
            allCompleteView.postDelayed(() -> {
                allCompleteView.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> parent.removeView(allCompleteView))
                        .start();
            }, 3000);
        }
    }

    private void showEmptyState(String message) {
        recyclerViewTasks.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    private void announceTaskStatus() {
        if (getActivity() instanceof KidDashboardActivity) {
            KidDashboardActivity dashboard = (KidDashboardActivity) getActivity();
            int totalTasks = taskList.size() + completedTasks.size();

            if (totalTasks == 0) {
                dashboard.announceIfEnabled("You have no tasks today! Enjoy your free time!");
            } else if (taskList.isEmpty()) {
                dashboard.announceIfEnabled("Fantastic! You completed all your tasks today!");
            } else {
                dashboard.announceIfEnabled(String.format("You have %d %s to complete today!",
                        taskList.size(),
                        taskList.size() == 1 ? "task" : "tasks"));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh tasks when fragment becomes visible
        loadTasks();

        // Announce page change
        if (getActivity() instanceof KidDashboardActivity) {
            ((KidDashboardActivity) getActivity()).announceIfEnabled("Tasks page");
        }
    }

    // Custom ItemDecoration for spacing
    private static class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        SpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = spacing;

            // Add top spacing for first item
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing;
            }
        }
    }
}
