package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.parent.CreateTaskActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.TaskAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.ConfirmationDialog;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class ParentTasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private static final String TAG = "ParentTasksFragment";

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewTasks;
    private TextView tvEmptyState;
    private View layoutEmptyState;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipActive, chipCompleted, chipDaily, chipWeekly;
    private MaterialButton btnCreateFirstTask;

    // Summary Cards
    private TextView tvTotalTasks, tvActiveTasks, tvCompletedToday;

    // Data
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private List<Task> filteredTasks;
    private String currentFilter = "all";

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    public ParentTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initManagers();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFilterChips();
        setupEmptyState();
        loadTasks();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        btnCreateFirstTask = view.findViewById(R.id.btn_create_first_task);

        // Filter chips
        chipAll = view.findViewById(R.id.chip_all);
        chipActive = view.findViewById(R.id.chip_active);
        chipCompleted = view.findViewById(R.id.chip_completed);
        chipDaily = view.findViewById(R.id.chip_daily);
        chipWeekly = view.findViewById(R.id.chip_weekly);

        // Summary cards
        tvTotalTasks = view.findViewById(R.id.tv_total_tasks);
        tvActiveTasks = view.findViewById(R.id.tv_active_tasks);
        tvCompletedToday = view.findViewById(R.id.tv_completed_today);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(requireContext());
        allTasks = new ArrayList<>();
        filteredTasks = new ArrayList<>();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(filteredTasks, this, false); // false for parent view
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadTasks);
        swipeRefreshLayout.setColorSchemeResources(R.color.green_primary);
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> filterTasks("all"));
        chipActive.setOnClickListener(v -> filterTasks("active"));
        chipCompleted.setOnClickListener(v -> filterTasks("completed"));
        chipDaily.setOnClickListener(v -> filterTasks("daily"));
        chipWeekly.setOnClickListener(v -> filterTasks("weekly"));

        // Set initial selection
        chipAll.setChecked(true);
    }

    private void setupEmptyState() {
        btnCreateFirstTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        showLoading(true);
        String familyId = prefManager.getFamilyId();

        if (familyId != null) {
            firebaseManager.getFamilyTasks(familyId, task -> {
                showLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Task> tasks = task.getResult().toObjects(Task.class);
                    updateTasksList(tasks);
                } else {
                    showError("Failed to load tasks");
                }
            });
        } else {
            showLoading(false);
            showError("Family not found");
        }
    }

    private void updateTasksList(List<Task> tasks) {
        allTasks.clear();
        allTasks.addAll(tasks);
        updateSummaryCards();
        filterTasks(currentFilter);
        updateEmptyState();
    }

    private void updateSummaryCards() {
        int totalTasks = allTasks.size();
        int activeTasks = 0;
        int completedToday = 5; // This would be calculated from completion records

        for (Task task : allTasks) {
            if (task.isActive()) {
                activeTasks++;
            }
        }

        tvTotalTasks.setText(String.valueOf(totalTasks));
        tvActiveTasks.setText(String.valueOf(activeTasks));
        tvCompletedToday.setText(String.valueOf(completedToday));
    }

    private void filterTasks(String filter) {
        currentFilter = filter;
        filteredTasks.clear();

        switch (filter) {
            case "all":
                filteredTasks.addAll(allTasks);
                updateChipSelection(chipAll);
                break;
            case "active":
                for (Task task : allTasks) {
                    if (task.isActive()) {
                        filteredTasks.add(task);
                    }
                }
                updateChipSelection(chipActive);
                break;
            case "completed":
                for (Task task : allTasks) {
                    if (!task.isActive()) {
                        filteredTasks.add(task);
                    }
                }
                updateChipSelection(chipCompleted);
                break;
            case "daily":
                for (Task task : allTasks) {
                    if ("daily".equals(task.getRepeatFrequency()) && task.isActive()) {
                        filteredTasks.add(task);
                    }
                }
                updateChipSelection(chipDaily);
                break;
            case "weekly":
                for (Task task : allTasks) {
                    if ("weekly".equals(task.getRepeatFrequency()) && task.isActive()) {
                        filteredTasks.add(task);
                    }
                }
                updateChipSelection(chipWeekly);
                break;
        }

        taskAdapter.updateTasks(filteredTasks);
        updateEmptyState();
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(selectedChip == chipAll);
        chipActive.setChecked(selectedChip == chipActive);
        chipCompleted.setChecked(selectedChip == chipCompleted);
        chipDaily.setChecked(selectedChip == chipDaily);
        chipWeekly.setChecked(selectedChip == chipWeekly);
    }

    private void updateEmptyState() {
        if (filteredTasks.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        recyclerViewTasks.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);

        String emptyMessage;
        switch (currentFilter) {
            case "active":
                emptyMessage = "No active tasks found.\nCreate your first task to get started!";
                break;
            case "completed":
                emptyMessage = "No completed tasks yet.\nCompleted tasks will appear here.";
                break;
            case "daily":
                emptyMessage = "No daily tasks found.\nCreate daily routine tasks for your kids!";
                break;
            case "weekly":
                emptyMessage = "No weekly tasks found.\nCreate weekly tasks for bigger goals!";
                break;
            default:
                emptyMessage = "No tasks created yet.\nTap the + button to create your first task!";
                break;
        }
        tvEmptyState.setText(emptyMessage);
    }

    private void hideEmptyState() {
        recyclerViewTasks.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // TaskAdapter.OnTaskClickListener implementation
    @Override
    public void onTaskClick(Task task) {
        // Navigate to task details or edit
        Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
        intent.putExtra("task_id", task.getTaskId());
        intent.putExtra("edit_mode", true);
        startActivity(intent);
    }

    @Override
    public void onTaskComplete(Task task) {
        // Not applicable for parent view
    }

    @Override
    public void onTaskEdit(Task task) {
        onTaskClick(task); // Same as task click
    }

    public void deleteTask(Task task) {
        ConfirmationDialog dialog = new ConfirmationDialog(
                requireContext(),
                "Delete Task",
                "Are you sure you want to delete '" + task.getName() + "'? This action cannot be undone.",
                "Delete",
                "Cancel",
                new ConfirmationDialog.OnConfirmationListener() {
                    @Override
                    public void onConfirm() {
                        performTaskDeletion(task);
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing
                    }
                }
        );
        dialog.show();
    }

    private void performTaskDeletion(Task task) {
        // Mark task as inactive instead of deleting
        task.setActive(false);
        task.setUpdatedAt(System.currentTimeMillis());

        firebaseManager.createTask(task, deleteTask -> {
            if (deleteTask.isSuccessful()) {
                Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                loadTasks(); // Refresh the list
            } else {
                Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Public method to refresh data (called from parent activity)
    public void refreshData() {
        loadTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks(); // Refresh when fragment becomes visible
    }
}