package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.CreateTaskActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.TaskAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private static final String TAG = "TasksFragment";

    private RecyclerView recyclerViewTasks;
    private FloatingActionButton fabAddTask;
    private ImageButton btnSearch;
    private TextView tvActiveTasks;
    private TextView tvCompletedToday;
    private LinearLayout layoutEmptyState;

    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadCurrentUser();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        fabAddTask = view.findViewById(R.id.fab_add_task);
        btnSearch = view.findViewById(R.id.btn_search);
        tvActiveTasks = view.findViewById(R.id.tv_active_tasks);
        tvCompletedToday = view.findViewById(R.id.tv_completed_today);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, getContext());
        taskAdapter.setOnTaskClickListener(this);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            // TODO: Implement search functionality
            Toast.makeText(getContext(), "Search functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCurrentUser() {
        Log.d(TAG, "Loading current user...");
        FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                Log.d(TAG, "Current user loaded: " + user.getName() + " (ID: " + user.getUserId() + ", FamilyID: " + user.getFamilyId() + ")");
                currentUser = user;
                loadTasks();
                loadTaskStatistics();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load current user: " + error);
                showEmptyState();
            }
        });
    }

    private void loadTasks() {
        if (currentUser == null || currentUser.getFamilyId() == null) {
            Log.w(TAG, "Cannot load tasks - currentUser is null or familyId is null");
            showEmptyState();
            return;
        }

        Log.d(TAG, "Loading tasks for family: " + currentUser.getFamilyId());

        // For now, let's just show ALL tasks to confirm Firebase is working
        FirebaseHelper.getAllFamilyTasksForDebug(currentUser.getFamilyId(), new FirebaseHelper.TasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                Log.d(TAG, "All tasks loaded: " + tasks.size() + " tasks");
                taskList.clear();
                if (tasks != null && !tasks.isEmpty()) {
                    taskList.addAll(tasks);
                    showTaskList();

                    // Log each task details
                    for (Task task : tasks) {
                        Log.d(TAG, "Task: " + task.getName() + " - Status: " + task.getStatus() + " - FamilyID: " + task.getFamilyId());
                    }
                } else {
                    showEmptyState();
                }
                taskAdapter.notifyDataSetChanged();
                updateActiveTasksCount();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load tasks: " + error);
                showEmptyState();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load tasks: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadTaskStatistics() {
        if (currentUser == null || currentUser.getFamilyId() == null) {
            return;
        }

        // Get today's completed tasks count
        getTodaysCompletedTasksCount();
    }

    private void getTodaysCompletedTasksCount() {
        if (currentUser == null || currentUser.getFamilyId() == null) {
            tvCompletedToday.setText("0");
            return;
        }

        FirebaseHelper.getTodaysTaskCompletions(currentUser.getFamilyId(), completedCount -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> tvCompletedToday.setText(String.valueOf(completedCount)));
            }
        });
    }

    private void updateActiveTasksCount() {
        int activeCount = 0;
        for (Task task : taskList) {
            if (!"completed".equals(task.getStatus()) && !"deleted".equals(task.getStatus())) {
                activeCount++;
            }
        }
        tvActiveTasks.setText(String.valueOf(activeCount));
    }

    private void showTaskList() {
        recyclerViewTasks.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerViewTasks.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskClick(Task task) {
        // Handle task click - could open task details
        if (getContext() != null) {
            Toast.makeText(getContext(), "Task: " + task.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
        if (currentUser == null) {
            return;
        }

        if (isCompleted) {
            // Complete the task
            FirebaseHelper.completeTask(task.getTaskId(), currentUser.getUserId(), taskResult -> {
                if (taskResult.isSuccessful()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Task completed! Stars awarded!", Toast.LENGTH_SHORT).show();
                    }
                    // Refresh statistics
                    loadTaskStatistics();
                    updateActiveTasksCount();
                } else {
                    Log.e(TAG, "Failed to complete task", taskResult.getException());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to complete task", Toast.LENGTH_SHORT).show();
                    }
                    // Revert checkbox state
                    task.setStatus("active");
                    taskAdapter.notifyDataSetChanged();
                }
            });
        } else {
            // If task is unchecked, we could implement "undo completion" logic here
            // For now, just update the status
            task.setStatus("active");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (currentUser != null) {
            loadTasks();
            loadTaskStatistics();
        } else {
            loadCurrentUser();
        }
    }
}
