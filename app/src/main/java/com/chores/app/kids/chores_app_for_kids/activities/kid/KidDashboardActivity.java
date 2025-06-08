package com.chores.app.kids.chores_app_for_kids.activities.kid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.LandingActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.TaskAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.TaskCompletionDialog;
import com.chores.app.kids.chores_app_for_kids.models.Kid;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.TaskCompletion;
import com.chores.app.kids.chores_app_for_kids.utils.AuthManager;
import com.chores.app.kids.chores_app_for_kids.utils.DateTimeUtils;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class KidDashboardActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private static final String TAG = "KidDashboardActivity";

    // Views
    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvStarBalance;
    private MaterialCardView cardStarBalance;
    private RecyclerView recyclerViewTasks;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigation;
    private View layoutNoTasks;
    private TextView tvNoTasksMessage;
    private ImageView ivCelebration;

    // Data
    private TaskAdapter taskAdapter;
    private List<Task> tasks;
    private Kid currentKid;

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_dashboard);

        initViews();
        initManagers();
        setupRecyclerView();
        setupSwipeRefresh();
        setupBottomNavigation();
        loadData();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        tvDate = findViewById(R.id.tv_date);
        tvStarBalance = findViewById(R.id.tv_star_balance);
        cardStarBalance = findViewById(R.id.card_star_balance);
        recyclerViewTasks = findViewById(R.id.recycler_view_tasks);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        layoutNoTasks = findViewById(R.id.layout_no_tasks);
        tvNoTasksMessage = findViewById(R.id.tv_no_tasks_message);
        ivCelebration = findViewById(R.id.iv_celebration);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        authManager = AuthManager.getInstance(this);
        tasks = new ArrayList<>();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(tasks, this, true); // true for kid view
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        swipeRefreshLayout.setColorSchemeResources(R.color.green_primary);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                // Already on tasks, scroll to top
                recyclerViewTasks.smoothScrollToPosition(0);
                return true;
            } else if (itemId == R.id.nav_rewards) {
                Intent intent = new Intent(this, RewardRedemptionActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, StarBalanceActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Set initial selection
        bottomNavigation.setSelectedItemId(R.id.nav_tasks);
    }

    private void loadData() {
        showLoading(true);
        loadKidProfile();
    }

    private void loadKidProfile() {
        String kidId = prefManager.getKidId();
        if (kidId != null) {
            firebaseManager.getKid(kidId, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    currentKid = task.getResult().toObject(Kid.class);
                    if (currentKid != null) {
                        updateUIWithKidData();
                        loadTodaysTasks();
                    }
                } else {
                    showError("Failed to load profile data");
                    showLoading(false);
                }
            });
        } else {
            showError("Kid profile not found");
            showLoading(false);
        }
    }

    private void updateUIWithKidData() {
        if (currentKid != null) {
            // Set greeting
            String greeting = getTimeBasedGreeting() + ", " + currentKid.getName() + "!";
            tvGreeting.setText(greeting);

            // Set star balance
            tvStarBalance.setText(String.valueOf(currentKid.getStarBalance()));

            // Set date
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
            tvDate.setText("Today, " + dateFormat.format(calendar.getTime()));
        }
    }

    private String getTimeBasedGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Good morning";
        } else if (hour < 17) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }

    private void loadTodaysTasks() {
        String kidId = prefManager.getKidId();
        if (kidId != null) {
            firebaseManager.getKidTasks(kidId, task -> {
                showLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Task> allTasks = task.getResult().toObjects(Task.class);

                    // Filter for today's tasks (you can implement more sophisticated filtering)
                    List<Task> todaysTasks = filterTodaysTasks(allTasks);

                    updateTasksList(todaysTasks);
                } else {
                    showError("Failed to load tasks");
                }
            });
        }
    }

    private List<Task> filterTodaysTasks(List<Task> allTasks) {
        List<Task> todaysTasks = new ArrayList<>();
        long todayStart = DateTimeUtils.getStartOfDay(System.currentTimeMillis());
        long todayEnd = DateTimeUtils.getEndOfDay(System.currentTimeMillis());

        for (Task task : allTasks) {
            // For daily tasks, show them every day
            // For other frequencies, implement appropriate logic
            if ("daily".equals(task.getRepeatFrequency()) ||
                    (task.getStartDate() >= todayStart && task.getStartDate() <= todayEnd)) {
                todaysTasks.add(task);
            }
        }

        return todaysTasks;
    }

    private void updateTasksList(List<Task> todaysTasks) {
        tasks.clear();
        tasks.addAll(todaysTasks);
        taskAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (tasks.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        recyclerViewTasks.setVisibility(View.GONE);
        layoutNoTasks.setVisibility(View.VISIBLE);

        // Check if all tasks are completed
        if (areAllTasksCompleted()) {
            tvNoTasksMessage.setText(getString(R.string.congratulations));
            ivCelebration.setVisibility(View.VISIBLE);
        } else {
            tvNoTasksMessage.setText(getString(R.string.no_tasks_today));
            ivCelebration.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        recyclerViewTasks.setVisibility(View.VISIBLE);
        layoutNoTasks.setVisibility(View.GONE);
    }

    private boolean areAllTasksCompleted() {
        // This would check against completed tasks for today
        // For now, return false to show "no tasks" message
        return false;
    }

    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // TaskAdapter.OnTaskClickListener implementation
    @Override
    public void onTaskClick(Task task) {
        // Show task details or do nothing for kids
    }

    @Override
    public void onTaskComplete(Task task) {
        showTaskCompletionDialog(task);
    }

    @Override
    public void onTaskEdit(Task task) {
        // Not applicable for kids
    }

    private void showTaskCompletionDialog(Task task) {
        TaskCompletionDialog dialog = new TaskCompletionDialog(this, task, this::completeTask);
        dialog.show();
    }

    private void completeTask(Task task) {
        if (currentKid == null) return;

        // Create task completion record
        String completionId = firebaseManager.getInstance().hashCode() + "_" + System.currentTimeMillis();
        TaskCompletion completion = new TaskCompletion(
                completionId,
                task.getTaskId(),
                currentKid.getKidId(),
                currentKid.getFamilyId(),
                task.getStarsPerCompletion()
        );

        // Save completion and update star balance
        firebaseManager.completeTask(completion, completionTask -> {
            if (completionTask.isSuccessful()) {
                // Update local star balance
                int newBalance = currentKid.getStarBalance() + task.getStarsPerCompletion();
                currentKid.setStarBalance(newBalance);

                // Update star balance in Firestore
                firebaseManager.updateKidStarBalance(currentKid.getKidId(), newBalance, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        updateUIWithKidData();
                        showTaskCompletedSuccess(task);
                        loadTodaysTasks(); // Refresh tasks
                    } else {
                        showError("Failed to update star balance");
                    }
                });
            } else {
                showError("Failed to complete task");
            }
        });
    }

    private void showTaskCompletedSuccess(Task task) {
        String message = getString(R.string.success_task_completed, task.getStarsPerCompletion());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // You can add celebration animation here
        animateStarBalance();
    }

    private void animateStarBalance() {
        // Add a simple scale animation to the star balance card
        cardStarBalance.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> {
                    cardStarBalance.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadData();

        // Reset bottom navigation selection
        bottomNavigation.setSelectedItemId(R.id.nav_tasks);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    // Logout and go to landing
                    logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        prefManager.clearAll();
        authManager.signOut(task -> {
            Intent intent = new Intent(this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}