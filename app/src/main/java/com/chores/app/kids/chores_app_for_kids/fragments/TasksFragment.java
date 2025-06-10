package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView recyclerViewTasks;
    private FloatingActionButton fabAddTask;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadTasks();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        fabAddTask = view.findViewById(R.id.fab_add_task);
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, getContext());
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        // TODO: Load tasks from Firebase
        // For now, add sample data
        addSampleTasks();
    }

    private void addSampleTasks() {
        Task task1 = new Task("Brush Teeth", "ic_brush_teeth", 2);
        Task task2 = new Task("Clean Room", "ic_toys", 5);
        Task task3 = new Task("Put Away Clothes", "ic_clothes", 3);

        taskList.add(task1);
        taskList.add(task2);
        taskList.add(task3);

        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh tasks when fragment becomes visible
        loadTasks();
    }
}
