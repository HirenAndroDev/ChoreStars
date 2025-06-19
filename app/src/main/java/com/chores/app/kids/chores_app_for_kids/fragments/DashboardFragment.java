package com.chores.app.kids.chores_app_for_kids.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.ChildProgressAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.FamilyStatsAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.RecentActivityAdapter;
import com.chores.app.kids.chores_app_for_kids.models.ChildProgress;
import com.chores.app.kids.chores_app_for_kids.models.DashboardStats;
import com.chores.app.kids.chores_app_for_kids.models.RecentActivity;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.views.CircularProgressView;
import com.chores.app.kids.chores_app_for_kids.views.WeeklyChartView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView nestedScrollView;

    // Header Stats
    private TextView tvTotalStars, tvTotalTasks, tvActiveKids, tvCompletionRate;
    private CircularProgressView progressOverall;
    private WeeklyChartView weeklyChart;

    // RecyclerViews
    private RecyclerView rvChildProgress, rvRecentActivity, rvFamilyStats;

    // Adapters
    private ChildProgressAdapter childProgressAdapter;
    private RecentActivityAdapter recentActivityAdapter;
    private FamilyStatsAdapter familyStatsAdapter;

    // Data
    private List<ChildProgress> childProgressList;
    private List<RecentActivity> recentActivities;
    private DashboardStats dashboardStats;
    private String familyId;
    private List<User> familyChildren; // Store children list to avoid duplicates

    // Firebase listeners for real-time updates
    private List<ValueEventListener> taskListeners;
    private List<DatabaseReference> taskReferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerViews();
        setupSwipeRefresh();
        loadDashboardData();
    }

    private void initializeViews(View view) {
        // Swipe refresh and scroll view
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        nestedScrollView = view.findViewById(R.id.nested_scroll_view);

        // Header stats
        tvTotalStars = view.findViewById(R.id.tv_total_stars);
        tvTotalTasks = view.findViewById(R.id.tv_total_tasks);
        tvActiveKids = view.findViewById(R.id.tv_active_kids);
        tvCompletionRate = view.findViewById(R.id.tv_completion_rate);
        progressOverall = view.findViewById(R.id.progress_overall);
        weeklyChart = view.findViewById(R.id.weekly_chart);

        // RecyclerViews
        rvChildProgress = view.findViewById(R.id.rv_child_progress);
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity);
        rvFamilyStats = view.findViewById(R.id.rv_family_stats);

        // Initialize data lists
        childProgressList = new ArrayList<>();
        recentActivities = new ArrayList<>();
        familyChildren = new ArrayList<>();
        dashboardStats = new DashboardStats();

        // Initialize Firebase listeners lists
        taskListeners = new ArrayList<>();
        taskReferences = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // Child Progress RecyclerView
        childProgressAdapter = new ChildProgressAdapter(childProgressList, getContext());
        rvChildProgress.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildProgress.setAdapter(childProgressAdapter);
        rvChildProgress.setNestedScrollingEnabled(false);

        // Recent Activity RecyclerView
        recentActivityAdapter = new RecentActivityAdapter(recentActivities, getContext());
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentActivity.setAdapter(recentActivityAdapter);
        rvRecentActivity.setNestedScrollingEnabled(false);

        // Family Stats RecyclerView (Horizontal)
        familyStatsAdapter = new FamilyStatsAdapter(getContext());
        rvFamilyStats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFamilyStats.setAdapter(familyStatsAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.colorAccent,
                R.color.star_orange
        );

        swipeRefreshLayout.setOnRefreshListener(this::refreshDashboard);
    }

    private void loadDashboardData() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                familyId = user.getFamilyId();
                if (familyId != null) {
                    Log.d(TAG, "Loading dashboard data for family: " + familyId);
                    loadAllDashboardData();
                } else {
                    Log.e(TAG, "User has no family ID");
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get current user: " + error);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshDashboard() {
        Log.d(TAG, "Refreshing dashboard data");
        // Clear existing data to avoid duplicates
        childProgressList.clear();
        recentActivities.clear();
        familyChildren.clear();
        childProgressAdapter.notifyDataSetChanged();
        recentActivityAdapter.notifyDataSetChanged();

        loadDashboardData();
    }

    private void loadAllDashboardData() {
        // Load all data simultaneously
        loadFamilyChildren();
        loadFamilyStats();
        loadRecentActivity();
        loadWeeklyData();
    }

    private void loadFamilyChildren() {
        FirebaseHelper.getFamilyChildren(familyId, new FirebaseHelper.FamilyChildrenCallback() {
            @Override
            public void onChildrenLoaded(List<User> children) {
                Log.d(TAG, "Loaded " + children.size() + " children from Firebase");

                // Clear existing data to prevent duplicates
                familyChildren.clear();
                childProgressList.clear();

                // Remove existing listeners before setting up new ones
                removeTaskListeners();

                if (children.isEmpty()) {
                    Log.d(TAG, "No children found");
                    childProgressAdapter.notifyDataSetChanged();
                    checkLoadingComplete();
                    return;
                }

                // Store children list
                familyChildren.addAll(children);

                // Load progress for each child with counter to track completion
                AtomicInteger loadedCount = new AtomicInteger(0);
                int totalChildren = children.size();

                for (User child : children) {
                    loadChildProgress(child, () -> {
                        int completed = loadedCount.incrementAndGet();
                        Log.d(TAG, "Loaded progress for child " + completed + "/" + totalChildren);

                        if (completed == totalChildren) {
                            // All children progress loaded
                            childProgressAdapter.notifyDataSetChanged();
                            updateDashboardStats();
                            // Set up real-time listeners after initial load
                            setupTaskCompletionListeners();
                            checkLoadingComplete();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load family children: " + error);
                checkLoadingComplete();
            }
        });
    }

    private void loadChildProgress(User child, Runnable onComplete) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Log.d(TAG, "Loading progress for child: " + child.getName() + " (" + child.getUserId() + ")");

        FirebaseHelper.getTasksForDate(child.getUserId(), today, new FirebaseHelper.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<com.chores.app.kids.chores_app_for_kids.models.Task> tasks) {
                // Calculate progress
                int totalTasks = tasks.size();
                int completedTasks = 0;
                int totalStars = 0;
                int earnedStars = 0;

                for (com.chores.app.kids.chores_app_for_kids.models.Task task : tasks) {
                    totalStars += task.getStarReward();
                    if (task.isCompleted()) {
                        completedTasks++;
                        earnedStars += task.getStarReward();
                    }
                }

                Log.d(TAG, child.getName() + " progress: " + completedTasks + "/" + totalTasks + " tasks, " + earnedStars + " stars earned today");

                // Create ChildProgress object
                ChildProgress progress = new ChildProgress();
                progress.setChildId(child.getUserId());
                progress.setChildName(child.getName());
                progress.setProfileImageUrl(child.getProfileImageUrl());
                progress.setTotalTasks(totalTasks);
                progress.setCompletedTasks(completedTasks);
                progress.setTotalStars(child.getStarBalance());
                progress.setEarnedStarsToday(earnedStars);
                progress.setProgressPercentage(totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0);

                // Check if child already exists in list (prevent duplicates)
                boolean childExists = false;
                for (int i = 0; i < childProgressList.size(); i++) {
                    if (childProgressList.get(i).getChildId().equals(child.getUserId())) {
                        // Update existing entry
                        childProgressList.set(i, progress);
                        childExists = true;
                        break;
                    }
                }

                if (!childExists) {
                    childProgressList.add(progress);
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load tasks for child " + child.getName() + ": " + error);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void loadFamilyStats() {
        FirebaseHelper.getFamilyStats(familyId, new FirebaseHelper.FamilyStatsCallback() {
            @Override
            public void onStatsLoaded(FirebaseHelper.FamilyStats stats) {
                Log.d(TAG, "Loaded family stats: " + stats.getTasksCompletedThisWeek() + " tasks, " + stats.getStarsEarnedThisWeek() + " stars");

                dashboardStats.setTasksCompletedThisWeek(stats.getTasksCompletedThisWeek());
                dashboardStats.setStarsEarnedThisWeek(stats.getStarsEarnedThisWeek());
                dashboardStats.setTotalStarBalance(stats.getTotalStarBalance());
                dashboardStats.setChildCount(stats.getChildCount());

                familyStatsAdapter.updateStats(dashboardStats);
                updateHeaderStats();
                checkLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load family stats: " + error);
                checkLoadingComplete();
            }
        });
    }

    private void loadRecentActivity() {
        // Load recent star transactions for the family
        FirebaseHelper.getRecentFamilyTransactions(familyId, 10, new FirebaseHelper.StarTransactionsCallback() {
            @Override
            public void onTransactionsLoaded(List<com.chores.app.kids.chores_app_for_kids.models.StarTransaction> transactions) {
                Log.d(TAG, "Loaded " + transactions.size() + " recent transactions");

                recentActivities.clear();

                // Convert transactions to recent activities
                for (com.chores.app.kids.chores_app_for_kids.models.StarTransaction transaction : transactions) {
                    RecentActivity activity = new RecentActivity();
                    activity.setUserId(transaction.getUserId());
                    activity.setDescription(transaction.getDescription());
                    activity.setStarAmount(transaction.getAmount());
                    activity.setTimestamp(transaction.getTimestamp());
                    activity.setType(transaction.getType());

                    // Find user name from family children
                    for (User child : familyChildren) {
                        if (child.getUserId().equals(transaction.getUserId())) {
                            activity.setUserName(child.getName());
                            break;
                        }
                    }

                    recentActivities.add(activity);
                }

                recentActivityAdapter.notifyDataSetChanged();
                checkLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load recent activity: " + error);
                checkLoadingComplete();
            }
        });
    }

    private void loadWeeklyData() {
        // Get data for the last 7 days
        Calendar calendar = Calendar.getInstance();
        List<Integer> weeklyData = new ArrayList<>();

        // For now, using dummy data - you can implement actual weekly data loading
        for (int i = 6; i >= 0; i--) {
            weeklyData.add((int) (Math.random() * 20));
        }

        weeklyChart.setData(weeklyData);
        checkLoadingComplete();
    }

    private void updateDashboardStats() {
        if (childProgressList.isEmpty()) return;

        int totalTasks = 0;
        int completedTasks = 0;
        int totalStars = 0;

        for (ChildProgress progress : childProgressList) {
            totalTasks += progress.getTotalTasks();
            completedTasks += progress.getCompletedTasks();
            totalStars += progress.getTotalStars();
        }

        dashboardStats.setTotalTasksToday(totalTasks);
        dashboardStats.setCompletedTasksToday(completedTasks);
        dashboardStats.setTotalStarsEarned(totalStars);
        dashboardStats.setActiveChildren(childProgressList.size());

        Log.d(TAG, "Updated dashboard stats: " + totalTasks + " total tasks, " + completedTasks + " completed, " + totalStars + " stars");

        updateHeaderStats();
    }

    private void updateHeaderStats() {
        if (getActivity() == null) return;

        // Animate counter updates
        animateCounterUpdate(tvTotalStars, dashboardStats.getTotalStarsEarned());
        animateCounterUpdate(tvTotalTasks, dashboardStats.getTotalTasksToday());
        animateCounterUpdate(tvActiveKids, dashboardStats.getActiveChildren());

        // Update completion rate
        int completionRate = dashboardStats.getTotalTasksToday() > 0 ?
                (dashboardStats.getCompletedTasksToday() * 100) / dashboardStats.getTotalTasksToday() : 0;

        tvCompletionRate.setText(completionRate + "%");
        progressOverall.setProgress(completionRate);

        Log.d(TAG, "Updated header stats: completion rate = " + completionRate + "%");
    }

    private void animateCounterUpdate(TextView textView, int targetValue) {
        int currentValue = 0;
        try {
            String currentText = textView.getText().toString().replaceAll("[^0-9]", "");
            if (!currentText.isEmpty()) {
                currentValue = Integer.parseInt(currentText);
            }
        } catch (NumberFormatException e) {
            currentValue = 0;
        }

        ValueAnimator animator = ObjectAnimator.ofInt(currentValue, targetValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(animatedValue));
        });
        animator.start();
    }

    private void checkLoadingComplete() {
        // This method ensures we only stop the refresh indicator when all data is loaded
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Set up real-time listeners for task completion updates
     */
    private void setupTaskCompletionListeners() {
        if (familyChildren.isEmpty()) {
            return;
        }

        Log.d(TAG, "Setting up task completion listeners for " + familyChildren.size() + " children");

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (User child : familyChildren) {
            // Listen to both tasks and completions for comprehensive updates
            DatabaseReference taskRef = FirebaseDatabase.getInstance()
                    .getReference("tasks")
                    .child(child.getUserId())
                    .child(today);

            DatabaseReference completionRef = FirebaseDatabase.getInstance()
                    .getReference("task_completions")
                    .child(child.getUserId())
                    .child(today);

            // Task listener
            ValueEventListener taskListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Task data changed for child: " + child.getName());
                    // Update this specific child's progress
                    updateChildProgressRealTime(child);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to listen for task changes for child " + child.getName() + ": " + databaseError.getMessage());
                }
            };

            // Completion listener
            ValueEventListener completionListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Task completion data changed for child: " + child.getName());
                    // Update this specific child's progress
                    updateChildProgressRealTime(child);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to listen for completion changes for child " + child.getName() + ": " + databaseError.getMessage());
                }
            };

            taskRef.addValueEventListener(taskListener);
            completionRef.addValueEventListener(completionListener);

            // Store references for cleanup
            taskListeners.add(taskListener);
            taskReferences.add(taskRef);
            taskListeners.add(completionListener);
            taskReferences.add(completionRef);
        }
    }

    /**
     * Update a specific child's progress in real-time
     */
    private void updateChildProgressRealTime(User child) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Log.d(TAG, "Updating real-time progress for child: " + child.getName() + " (" + child.getUserId() + ")");

        FirebaseHelper.getTasksForDate(child.getUserId(), today, new FirebaseHelper.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<com.chores.app.kids.chores_app_for_kids.models.Task> tasks) {
                // Calculate updated progress
                int totalTasks = tasks.size();
                int completedTasks = 0;
                int totalStars = 0;
                int earnedStars = 0;

                for (com.chores.app.kids.chores_app_for_kids.models.Task task : tasks) {
                    totalStars += task.getStarReward();
                    if (task.isCompleted()) {
                        completedTasks++;
                        earnedStars += task.getStarReward();
                    }
                }

                Log.d(TAG, child.getName() + " REAL-TIME UPDATE: " + completedTasks + "/" + totalTasks +
                        " tasks, " + earnedStars + " stars earned today");

                // Update the child's progress in the list
                ChildProgress updatedProgress = updateChildInProgressList(child, totalTasks, completedTasks, earnedStars);

                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update specific child in adapter
                        if (updatedProgress != null && childProgressAdapter != null) {
                            Log.d(TAG, "Updating adapter for child: " + child.getName() +
                                    " with progress: " + updatedProgress.getProgressPercentage() + "%");
                            childProgressAdapter.updateChildProgress(child.getUserId(), updatedProgress);
                        }

                        // Update dashboard stats and header
                        updateDashboardStats();
                        updateHeaderStats();

                        Log.d(TAG, "Dashboard UI updated for child: " + child.getName());
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load updated tasks for child " + child.getName() + ": " + error);
            }
        });
    }

    /**
     * Update a specific child's data in the progress list
     */
    private ChildProgress updateChildInProgressList(User child, int totalTasks, int completedTasks, int earnedStars) {
        // Find and update the child's progress
        for (int i = 0; i < childProgressList.size(); i++) {
            ChildProgress progress = childProgressList.get(i);
            if (progress.getChildId().equals(child.getUserId())) {
                // Store old values for comparison
                int oldCompletedTasks = progress.getCompletedTasks();
                int oldProgressPercentage = progress.getProgressPercentage();

                // Update values
                progress.setTotalTasks(totalTasks);
                progress.setCompletedTasks(completedTasks);
                progress.setEarnedStarsToday(earnedStars);
                progress.setProgressPercentage(totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0);

                Log.d(TAG, "Updated progress for " + child.getName() + " in list at position " + i +
                        " - Progress: " + oldProgressPercentage + "% -> " + progress.getProgressPercentage() + "%" +
                        " - Tasks: " + oldCompletedTasks + "/" + totalTasks + " -> " + completedTasks + "/" + totalTasks);

                return progress;
            }
        }

        Log.w(TAG, "Child not found in progress list: " + child.getName() + " (" + child.getUserId() + ")");
        return null;
    }

    /**
     * Remove all task completion listeners
     */
    private void removeTaskListeners() {
        Log.d(TAG, "Removing " + taskListeners.size() + " task listeners");

        for (int i = 0; i < taskListeners.size(); i++) {
            if (i < taskReferences.size() && taskReferences.get(i) != null && taskListeners.get(i) != null) {
                taskReferences.get(i).removeEventListener(taskListeners.get(i));
            }
        }

        taskListeners.clear();
        taskReferences.clear();
    }

    /**
     * Public method to manually refresh when task completion happens elsewhere
     */
    public void onTaskCompleted(String childId) {
        Log.d(TAG, "Task completed notification received for child: " + childId);

        // Find the child and update their progress
        for (User child : familyChildren) {
            if (child.getUserId().equals(childId)) {
                updateChildProgressRealTime(child);
                break;
            }
        }
    }

    // Public method to refresh dashboard from outside (e.g., when a task is completed)
    public void refreshDashboardData() {
        Log.d(TAG, "External refresh requested");
        refreshDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (familyId != null) {
            Log.d(TAG, "Fragment resumed, refreshing data");
            refreshDashboard();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clean up Firebase listeners to prevent memory leaks
        removeTaskListeners();

        // Clean up data to prevent memory leaks
        childProgressList.clear();
        recentActivities.clear();
        familyChildren.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Additional cleanup
        removeTaskListeners();
    }
}
