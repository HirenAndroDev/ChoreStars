package com.chores.app.kids.chores_app_for_kids.fragments;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.KidDashboardActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.KidTaskAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.KidProfilesDialog;
import com.chores.app.kids.chores_app_for_kids.models.KidProfile;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.adapters.KidProfileDialogAdapter;
import com.chores.app.kids.chores_app_for_kids.utils.KidProfileManager;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KidTasksFragment extends Fragment {

    private RecyclerView recyclerViewTasks;
    private TextView tvKidName;
    private TextView tvStarBalance;
    private TextView tvEmptyMessage;
    private LinearLayout layoutEmptyState;
    private CircleImageView ivKidProfile;
    private ImageView ivStarIcon;

    
    private KidTaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> completedTasks;
    private String childId;
    private String familyId;
    private int currentStarBalance = 0;
    private String childName;

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
        tvKidName = view.findViewById(R.id.tv_kid_name);
        tvStarBalance = view.findViewById(R.id.tv_star_balance);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        ivKidProfile = view.findViewById(R.id.iv_kid_profile);
        ivStarIcon = view.findViewById(R.id.iv_star_icon);

        // Set click listener for kid profile to show profiles dialog
        LinearLayout layoutKidProfile = view.findViewById(R.id.layout_kid_profile_section);
        if (layoutKidProfile != null) {
            layoutKidProfile.setOnClickListener(v -> showKidProfilesDialog());
        } else {
            // If no layout found, set click listener on profile image itself
            ivKidProfile.setOnClickListener(v -> showKidProfilesDialog());
        }
       
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        completedTasks = new ArrayList<>();

        taskAdapter = new KidTaskAdapter(taskList, getContext(), new KidTaskAdapter.OnTaskInteractionListener() {
            @Override
            public void onTaskCompleted(Task task) {
                handleTaskCompletion(task);
            }

            @Override
            public void onTaskClicked(Task task) {
                showTaskDetailDialog(task);
            }

            @Override
            public void onTaskLongClicked(Task task) {
                // Handle long click - show task detail dialog for now
                showTaskDetailDialog(task);
            }
        });

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);

        // Add item spacing for better visual separation
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        recyclerViewTasks.addItemDecoration(new SpacingItemDecoration(spacing));
    }

    private void loadUserData() {
        // Try both methods to get child ID
        childId = AuthHelper.getChildId(getContext());
        if (childId == null || childId.isEmpty()) {
            childId = AuthHelper.getCurrentUserId(getContext());
        }

        familyId = AuthHelper.getFamilyId(getContext());

        // Debug logs
        System.out.println("KidTasksFragment - childId: " + childId + ", familyId: " + familyId);

        // Load child user data
        if (childId != null && !childId.isEmpty()) {
            FirebaseHelper.getUserById(childId, new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateKidProfileUI(user);
                            // Update familyId if it was null
                            if (familyId == null || familyId.isEmpty()) {
                                familyId = user.getFamilyId();
                                System.out.println("Updated familyId from user: " + familyId);
                            }
                            // Reload tasks now that we have proper familyId
                            loadTasks();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    System.out.println("Error loading user: " + error);
                    // Still try to load tasks with what we have
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadTasks();
                        });
                    }
                }
            });

            // Load star balance
            FirebaseHelper.getUserStarBalanceById(childId, balance -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentStarBalance = balance;
                        tvStarBalance.setText(String.valueOf(balance));
                    });
                }
            });
        } else {
            System.out.println("No valid childId found, showing empty state");
            showEmptyState("Please log in again.");
        }
    }
    private void updateKidProfileUI(User user) {
        if (user != null) {
            tvKidName.setText(user.getName());
            childName = user.getName();

            // Load profile image
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImageUrl())
                        .circleCrop()
                        .into(ivKidProfile);
            } else {
                ivKidProfile.setImageResource(R.drawable.default_avatar);
            }
        }
    }

    private void loadTasks() {
        // Check if we have the required data
        if (childId == null || childId.isEmpty()) {
            System.out.println("No childId available, cannot load tasks");
            showEmptyState("Please log in again.");
            return;
        }

        // Get current date string
        String currentDate = getCurrentDateString();

        // Debug logs
        System.out.println("Loading tasks for childId: " + childId + ", familyId: " + familyId + ", currentDate: " + currentDate);

        // Load tasks for current date only
        FirebaseHelper.getTasksForDate(childId, currentDate, new FirebaseHelper.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println("Tasks loaded: " + (tasks != null ? tasks.size() : 0) + " tasks");

                        if (tasks == null || tasks.isEmpty()) {
                            showEmptyState("No tasks today! Great job! 🎉");
                        } else {
                            // Check completion status for each task
                            loadTodaysCompletedTasks(tasks);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println("Error loading tasks: " + error);
                        showEmptyState("Let's try loading your tasks again!");
                    });
                }
            }
        });
    }
    private void loadTodaysCompletedTasks(List<Task> activeTasks) {
        if (activeTasks.isEmpty()) {
            updateTaskList(activeTasks);
            return;
        }

        final int[] completionChecksRemaining = {activeTasks.size()};

        // Check completion status for each task
        for (Task task : activeTasks) {
            if (task.getTaskId() != null) {
                FirebaseHelper.getTaskCompletionForToday(childId, task.getTaskId(), isCompleted -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isCompleted) {
                                task.setStatus("completed");
                            } else {
                                task.setStatus("active");
                            }

                            completionChecksRemaining[0]--;

                            // Update UI only after all completion checks are done
                            if (completionChecksRemaining[0] == 0) {
                                updateTaskList(activeTasks);
                                checkForCompletedTasks(activeTasks);
                            }
                        });
                    }
                });
            } else {
                // Task has no ID, mark as active
                task.setStatus("active");
                completionChecksRemaining[0]--;

                // Update UI if this was the last check
                if (completionChecksRemaining[0] == 0) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateTaskList(activeTasks);
                            checkForCompletedTasks(activeTasks);
                        });
                    }
                }
            }
        }
    }

    private String getCurrentDateString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
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

        // Sort both active and completed tasks
        sortTasksByTime(taskList);
        sortTasksByTime(completedTasks);

        // Add completed tasks to the end of the list for display
        taskList.addAll(completedTasks);
        taskAdapter.notifyDataSetChanged();

        // Show appropriate view
        if (taskList.isEmpty()) {
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

    private void showTaskDetailDialog(Task task) {
        Dialog dialog = new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_detail, null);
        dialog.setContentView(dialogView);

        // Fix dialog window size and appearance
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            // Set dialog width to 90% of screen width
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int dialogWidth = (int) (displayMetrics.widthPixels * 0.9);

            // Set layout parameters
            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);

            // Add animation
            window.getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }

        // Set task details
        TextView tvDialogTaskName = dialogView.findViewById(R.id.tv_dialog_task_name);
        TextView tvDialogTaskNotes = dialogView.findViewById(R.id.tv_dialog_task_notes);
        TextView tvDialogStarReward = dialogView.findViewById(R.id.tv_dialog_star_reward);
        TextView tvDialogReminderTime = dialogView.findViewById(R.id.tv_dialog_reminder_time);
        LinearLayout layoutDialogReminderTime = dialogView.findViewById(R.id.layout_dialog_reminder_time);
        ImageView ivDialogTaskIcon = dialogView.findViewById(R.id.iv_dialog_task_icon);
        androidx.appcompat.widget.AppCompatButton btnDialogAction = dialogView.findViewById(R.id.btn_dialog_action);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close_dialog);

        tvDialogTaskName.setText(task.getName());
        tvDialogStarReward.setText(String.valueOf(task.getStarReward()));

        // Set task notes
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            tvDialogTaskNotes.setText(task.getNotes());
            tvDialogTaskNotes.setVisibility(View.VISIBLE);
        } else {
            tvDialogTaskNotes.setVisibility(View.GONE);
        }

        // Set reminder time
        if (task.getReminderTime() != null && !task.getReminderTime().trim().isEmpty()) {
            tvDialogReminderTime.setText(task.getReminderTime());
            layoutDialogReminderTime.setVisibility(View.VISIBLE);
        } else {
            layoutDialogReminderTime.setVisibility(View.GONE);
        }

        // Set task icon
        String iconName = task.getIconName();
        if (iconName != null && !iconName.isEmpty()) {
            if (task.getIconUrl() != null && !task.getIconUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(task.getIconUrl())
                        .placeholder(R.drawable.ic_task_default)
                        .error(R.drawable.ic_task_default)
                        .into(ivDialogTaskIcon);
            } else {
                int iconResId = getContext().getResources().getIdentifier(iconName, "drawable", getContext().getPackageName());
                if (iconResId != 0) {
                    ivDialogTaskIcon.setImageResource(iconResId);
                } else {
                    ivDialogTaskIcon.setImageResource(R.drawable.ic_task_default);
                }
            }
        } else {
            ivDialogTaskIcon.setImageResource(R.drawable.ic_task_default);
        }

        // Set button based on completion status
        boolean isCompleted = "completed".equals(task.getStatus());
        if (isCompleted) {
            btnDialogAction.setText("Mark as Incomplete");
            btnDialogAction.setBackgroundResource(R.drawable.bg_button_incomplete);
            btnDialogAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checkbox_unchecked, 0, 0, 0);
        } else {
            btnDialogAction.setText("Mark as Complete");
            btnDialogAction.setBackgroundResource(R.drawable.bg_button_complete);
            btnDialogAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checkbox_checked, 0, 0, 0);
        }

        // Set click listeners
        btnDialogAction.setOnClickListener(v -> {
            dialog.dismiss();
            handleTaskCompletion(task);
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());

        // Make dialog cancelable by tapping outside
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();
    }
    private void handleTaskCompletion(Task task) {
        boolean isCurrentlyCompleted = "completed".equals(task.getStatus());

        if (isCurrentlyCompleted) {
            // Mark as incomplete (reverse completion)
            task.setStatus("uncompleting");
            taskAdapter.notifyDataSetChanged();

            // Remove task completion from Firebase
            FirebaseHelper.uncompleteTask(task.getTaskId(), childId, result -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (result.isSuccessful()) {
                            // Task uncompleted successfully
                            task.setStatus("active");
                            taskAdapter.notifyDataSetChanged();

                            // Update star balance
                            int newBalance = currentStarBalance - task.getStarReward();
                            updateStarBalance(newBalance);

                            // Play sound
                            SoundHelper.playClickSound(getContext());

                            // Notify dashboard of task status change
                            notifyDashboardOfTaskChange();

                        } else {
                            // Task uncompletion failed
                            task.setStatus("completed");
                            taskAdapter.notifyDataSetChanged();
                            SoundHelper.playErrorSound(getContext());
                        }
                    });
                }
            });

        } else {
            // Mark as complete
            task.setStatus("completing");
            taskAdapter.notifyDataSetChanged();

            // Complete task in Firebase
            FirebaseHelper.completeTask(task.getTaskId(), childId, result -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (result.isSuccessful()) {
                            // Task completed successfully
                            task.setStatus("completed");
                            taskAdapter.notifyDataSetChanged();

                            // Update star balance
                            int newBalance = currentStarBalance + task.getStarReward();
                            updateStarBalance(newBalance);

                            // Celebrate task completion
                            celebrateTaskCompletion(task);

                            // Check if all tasks are completed
                            if (areAllTasksCompleted()) {
                                showAllTasksCompletedCelebration();
                            }

                            // Notify dashboard of task status change
                            notifyDashboardOfTaskChange();

                        } else {
                            // Task completion failed
                            task.setStatus("active");
                            taskAdapter.notifyDataSetChanged();
                            SoundHelper.playErrorSound(getContext());
                        }
                    });
                }
            });
        }
    }

    private void updateStarBalance(int newBalance) {
        int previousBalance = currentStarBalance;

        currentStarBalance = newBalance;
        tvStarBalance.setText(String.valueOf(newBalance));

        // Notify parent activity
        if (getActivity() instanceof KidDashboardActivity) {
            ((KidDashboardActivity) getActivity()).updateStarBalance(newBalance);
        }

        // Animate star icon if balance increased
        if (newBalance > previousBalance) {
            animateStarIncrease(newBalance - previousBalance);
        }
    }

    private boolean areAllTasksCompleted() {
        for (Task task : taskList) {
            if (!"completed".equals(task.getStatus())) {
                return false;
            }
        }
        return !taskList.isEmpty();
    }

    private void celebrateTaskCompletion(Task task) {
        // Play celebration sound
        SoundHelper.playTaskCompleteSound(getContext());


    }

    private void showAllTasksCompletedCelebration() {
        Dialog dialog = new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_congratulations, null);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Calculate total stars earned from all completed tasks today
        int totalStarsEarned = 0;
        for (Task task : taskList) {
            if ("completed".equals(task.getStatus())) {
                totalStarsEarned += task.getStarReward();
            }
        }

        TextView tvCongratulationsStars = dialogView.findViewById(R.id.tv_congratulations_stars);
        tvCongratulationsStars.setText(String.valueOf(totalStarsEarned));

        androidx.appcompat.widget.AppCompatButton btnContinue = dialogView.findViewById(R.id.btn_congratulations_continue);
        btnContinue.setOnClickListener(v -> dialog.dismiss());

        // Play celebration sound
        SoundHelper.playCelebrationSound(getContext());

        // Announce completion


        dialog.show();
    }

    private void showEmptyState(String message) {
        recyclerViewTasks.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh tasks when fragment becomes visible
        loadUserData();
        loadTasks();


    }

    // Method to show kid profiles dialog
    private void showKidProfilesDialog() {
        // Get all saved kid profiles
        List<KidProfile> savedKidProfiles = AuthHelper.getSavedKidProfiles(getContext());
        List<ChildProfile> childProfiles = new ArrayList<>();

        // Convert KidProfile to ChildProfile for dialog
        for (KidProfile kidProfile : savedKidProfiles) {
            ChildProfile childProfile = new ChildProfile();
            childProfile.setChildId(kidProfile.getKidId());
            childProfile.setName(kidProfile.getName());
            childProfile.setFamilyId(kidProfile.getFamilyId());
            childProfile.setProfileImageUrl(kidProfile.getProfileImageUrl());
            childProfile.setStarBalance(kidProfile.getStarBalance());
            childProfiles.add(childProfile);
        }

        // If no saved profiles, add current kid
        if (childProfiles.isEmpty()) {
            ChildProfile currentKid = new ChildProfile();
            currentKid.setChildId(AuthHelper.getChildId(getContext()));
            currentKid.setName(AuthHelper.getUserName(getContext()));
            currentKid.setFamilyId(AuthHelper.getFamilyId(getContext()));
            currentKid.setProfileImageUrl("");
            currentKid.setStarBalance(currentStarBalance);
            childProfiles.add(currentKid);
        }

        String selectedKidId = AuthHelper.getChildId(getContext());

        KidProfilesDialog dialog = new KidProfilesDialog(getContext(), childProfiles, selectedKidId);
        dialog.setOnKidSelectedListener(childProfile -> {
            // Switch to selected kid using AuthHelper
            AuthHelper.switchToKidProfile(getContext(), childProfile.getChildId());

            // Update current fragment data
            childId = childProfile.getChildId();
            familyId = childProfile.getFamilyId();

            // Load user data and star balance for the new kid
            loadUserData();
            loadTasks();
        });
        dialog.show();
    }

    private void showLogoutConfirmation() {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear kid profiles and navigate back to main activity
                    KidProfileManager kidProfileManager = new KidProfileManager(getContext());
                    kidProfileManager.clearAllKidProfiles();

                    // Finish current activity and go back to main
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void notifyDashboardOfTaskChange() {
        // Send broadcast to notify parent dashboard
        if (getContext() != null) {
            Intent intent = new Intent("com.chores.app.TASK_COMPLETED");
            intent.putExtra("childId", childId);
            intent.putExtra("familyId", familyId);
            getContext().sendBroadcast(intent);

            // Log the notification for debugging
            System.out.println("Sent task completion broadcast for child: " + childId);
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

    private void animateStarIncrease(int starsEarned) {
        // Scale animation for star icon
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivStarIcon, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivStarIcon, "scaleY", 1f, 1.5f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.start();
        scaleY.start();

        // Rotation animation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(ivStarIcon, "rotation", 0f, 360f);
        rotation.setDuration(800);
        rotation.start();

        // Play sound
        SoundHelper.playStarEarnedSound(getContext());

        // Announce star gain

    }

    /**
     * Sort tasks by reminder time first, then by creation time
     */
    private void sortTasksByTime(List<Task> taskList) {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                String reminderTime1 = task1.getReminderTime();
                String reminderTime2 = task2.getReminderTime();

                // Check if both tasks have reminder times
                boolean hasReminder1 = reminderTime1 != null && !reminderTime1.trim().isEmpty();
                boolean hasReminder2 = reminderTime2 != null && !reminderTime2.trim().isEmpty();

                if (hasReminder1 && hasReminder2) {
                    // Both have reminder times - sort by reminder time
                    int timeComparison = compareReminderTimes(reminderTime1, reminderTime2);
                    if (timeComparison != 0) {
                        return timeComparison;
                    }
                    // If reminder times are equal, sort by creation time
                    return Long.compare(task1.getCreatedTimestamp(), task2.getCreatedTimestamp());

                } else if (hasReminder1) {
                    // Only task1 has reminder time - it comes first
                    return -1;

                } else if (hasReminder2) {
                    // Only task2 has reminder time - it comes first
                    return 1;

                } else {
                    // Neither has reminder time - sort by creation time
                    return Long.compare(task1.getCreatedTimestamp(), task2.getCreatedTimestamp());
                }
            }
        });
    }

    /**
     * Compare reminder times in HH:MM format
     * Returns negative if time1 is earlier, positive if time1 is later, 0 if equal
     */
    private int compareReminderTimes(String time1, String time2) {
        try {
            // Parse time strings (assuming format like "09:30" or "9:30 AM")
            int minutes1 = parseTimeToMinutes(time1);
            int minutes2 = parseTimeToMinutes(time2);

            return Integer.compare(minutes1, minutes2);
        } catch (Exception e) {
            // If parsing fails, fall back to string comparison
            return time1.compareTo(time2);
        }
    }

    /**
     * Convert time string to minutes from midnight for easy comparison
     */
    private int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return Integer.MAX_VALUE; // Put invalid times at the end
        }

        timeStr = timeStr.trim().toUpperCase();
        boolean isPM = timeStr.contains("PM");
        boolean isAM = timeStr.contains("AM");

        // Remove AM/PM markers
        timeStr = timeStr.replace("AM", "").replace("PM", "").trim();

        String[] parts = timeStr.split(":");
        if (parts.length != 2) {
            return Integer.MAX_VALUE; // Invalid format
        }

        try {
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());

            // Convert to 24-hour format if needed
            if (isPM && hours != 12) {
                hours += 12;
            } else if (isAM && hours == 12) {
                hours = 0;
            }

            // Validate ranges
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                return Integer.MAX_VALUE;
            }

            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // Invalid format
        }
    }
}
