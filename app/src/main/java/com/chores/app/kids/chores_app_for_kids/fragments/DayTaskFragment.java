package com.chores.app.kids.chores_app_for_kids.fragments;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.KidTaskAdapter;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayTaskFragment extends Fragment {

    private static final String TAG = "DayTaskFragment";
    private LocalDate date;
    private String childId;
    private RecyclerView rvTasks;
    private KidTaskAdapter taskAdapter;
    private List<Task> tasks;
    private List<Task> completedTasks;
    private FirebaseHelper firebaseHelper;
    private TaskManageFragment parentFragment;
    private int currentStarBalance = 0;

    public DayTaskFragment() {
        // Required empty public constructor
    }

    public static DayTaskFragment newInstance(LocalDate date) {
        DayTaskFragment fragment = new DayTaskFragment();
        Bundle args = new Bundle();
        if (date != null) {
            args.putString("date", date.toString());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static DayTaskFragment newInstance(LocalDate date, String childId) {
        DayTaskFragment fragment = new DayTaskFragment();
        Bundle args = new Bundle();
        if (date != null) {
            args.putString("date", date.toString());
        }
        if (childId != null) {
            args.putString("childId", childId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String dateStr = getArguments().getString("date");
            if (dateStr != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                date = LocalDate.parse(dateStr);
            }
            childId = getArguments().getString("childId");
        }

        firebaseHelper = new FirebaseHelper();
        tasks = new ArrayList<>();
        completedTasks = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_task, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        setupRecyclerView();

        // Get parent fragment reference
        if (getParentFragment() instanceof TaskManageFragment) {
            parentFragment = (TaskManageFragment) getParentFragment();
        }

        loadTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Get selected kid from parent fragment when resuming
        if (parentFragment != null) {
            ChildProfile selectedKid = parentFragment.getSelectedKid();
            if (selectedKid != null) {
                if (!selectedKid.getChildId().equals(childId)) {
                    childId = selectedKid.getChildId();
                    loadTasks();
                }
                currentStarBalance = selectedKid.getStarBalance();
            }
        }
    }

    private void setupRecyclerView() {
        taskAdapter = new KidTaskAdapter(tasks, requireContext(), new KidTaskAdapter.OnTaskInteractionListener() {
            @Override
            public void onTaskCompleted(Task task) {
                handleTaskCompletion(task);
            }

            @Override
            public void onTaskClicked(Task task) {
                showTaskDetailDialog(task);
            }
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);

        // Add item spacing for better visual separation
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        rvTasks.addItemDecoration(new SpacingItemDecoration(spacing));
    }

    private void loadTasks() {
        if (childId == null) return;

        String dateStr = null;
        if (date != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        firebaseHelper.getTasksForDate(childId, dateStr, new FirebaseHelper.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> loadedTasks) {
                loadTodaysCompletedTasks(loadedTasks);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading tasks: " + error);
            }
        });
    }

    private void loadTodaysCompletedTasks(List<Task> activeTasks) {
        String dateStr = null;
        if (date != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            // Fallback to today's date
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                dateStr = sdf.format(new java.util.Date());
            }
        }

        final String finalDateStr = dateStr;

        // Check completion status for each task for the specific date
        for (Task task : activeTasks) {
            if (task.getTaskId() != null) {
                getTaskCompletionForSpecificDate(childId, task.getTaskId(), finalDateStr, isCompleted -> {
                    if (isCompleted) {
                        task.setStatus("completed");
                    } else {
                        task.setStatus("active");
                    }
                    updateTaskListAfterCompletionCheck(activeTasks);
                });
            }
        }

        // If no tasks have IDs, just update with active tasks
        if (activeTasks.stream().noneMatch(task -> task.getTaskId() != null)) {
            updateTaskList(activeTasks);
        }
    }

    private void updateTaskListAfterCompletionCheck(List<Task> allTasks) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updateTaskList(allTasks);
            });
        }
    }

    private void updateTaskList(List<Task> taskList) {
        tasks.clear();
        completedTasks.clear();

        for (Task task : taskList) {
            if ("completed".equals(task.getStatus())) {
                completedTasks.add(task);
            } else {
                tasks.add(task);
            }
        }

        // Sort both active and completed tasks
        sortTasksByTime(tasks);
        sortTasksByTime(completedTasks);

        // Add completed tasks to the end of the list for display
        tasks.addAll(completedTasks);
        taskAdapter.notifyDataSetChanged();
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

    private void handleTaskCompletion(Task task) {
        boolean isCurrentlyCompleted = "completed".equals(task.getStatus());

        // Get the specific date for this fragment
        String dateStr = null;
        if (date != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            // Fallback to today's date
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                dateStr = sdf.format(new java.util.Date());
            }
        }

        final String finalDateStr = dateStr;

        if (isCurrentlyCompleted) {
            // Mark as incomplete (reverse completion)
            task.setStatus("uncompleting");
            taskAdapter.notifyDataSetChanged();

            // Remove task completion from Firebase for specific date
            uncompleteTaskForSpecificDate(task.getTaskId(), childId, finalDateStr, success -> {
                handleTaskCompletionResult(task, success, false);
            });

        } else {
            // Mark as complete
            task.setStatus("completing");
            taskAdapter.notifyDataSetChanged();

            // Complete task in Firebase for specific date
            completeTaskForSpecificDate(task.getTaskId(), childId, finalDateStr, task.getStarReward(), success -> {
                handleTaskCompletionResult(task, success, true);
            });
        }
    }

    private void handleTaskCompletionResult(Task task, boolean success, boolean wasCompleting) {
        if (success) {
            // Task completion/uncompletion successful
            task.setStatus(wasCompleting ? "completed" : "active");
            taskAdapter.notifyDataSetChanged();

            // Update star balance in parent fragment
            if (parentFragment != null) {
                ChildProfile selectedKid = parentFragment.getSelectedKid();
                if (selectedKid != null) {
                    int newBalance = wasCompleting ?
                            selectedKid.getStarBalance() + task.getStarReward() :
                            selectedKid.getStarBalance() - task.getStarReward();

                    selectedKid.setStarBalance(newBalance);
                    parentFragment.updateKidProfileUI();
                    currentStarBalance = newBalance;
                }
            }

            // Play appropriate sound
            if (wasCompleting) {
                SoundHelper.playTaskCompleteSound(getContext());
            } else {
                SoundHelper.playClickSound(getContext());
            }

        } else {
            // Task completion/uncompletion failed
            task.setStatus(wasCompleting ? "active" : "completed");
            taskAdapter.notifyDataSetChanged();
            SoundHelper.playErrorSound(getContext());
        }
    }

    public void refreshTasks() {
        loadTasks();
    }

    public void setChildId(String childId) {
        if (childId != null && !childId.equals(this.childId)) {
            this.childId = childId;
            loadTasks();
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

    private void getTaskCompletionForSpecificDate(String userId, String taskId, String date, TaskCompletionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taskCompletions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isCompleted = !task.getResult().isEmpty();
                        callback.onCompletionStatusReceived(isCompleted);
                    } else {
                        callback.onCompletionStatusReceived(false);
                    }
                });
    }

    private interface TaskCompletionCallback {
        void onCompletionStatusReceived(boolean isCompleted);
    }

    private void completeTaskForSpecificDate(String taskId, String userId, String date, int starReward, TaskCompletionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taskCompletions")
                .add(new HashMap<String, Object>() {{
                    put("taskId", taskId);
                    put("userId", userId);
                    put("date", date);
                    put("starReward", starReward);
                    put("timestamp", System.currentTimeMillis());
                }})
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onCompletionStatusReceived(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onCompletionStatusReceived(false);
                    }
                });
    }

    private void uncompleteTaskForSpecificDate(String taskId, String userId, String date, TaskCompletionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taskCompletions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (callback != null) {
                                            callback.onCompletionStatusReceived(true);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (callback != null) {
                                            callback.onCompletionStatusReceived(false);
                                        }
                                    });
                        }
                    } else {
                        if (callback != null) {
                            callback.onCompletionStatusReceived(false);
                        }
                    }
                });
    }

    private static class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        SpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = spacing;

            // Add top spacing for first item
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing;
            }
        }
    }
}
