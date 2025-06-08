package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Task;

public class TaskCompletionDialog extends Dialog {
    private Task task;
    private OnTaskCompletionListener listener;

    private TextView tvTaskName;
    private TextView tvStarsEarned;
    private ImageView ivTaskIcon;
    private Button btnComplete;
    private Button btnCancel;

    public interface OnTaskCompletionListener {
        void onTaskCompleted(Task task);
    }

    public TaskCompletionDialog(@NonNull Context context, Task task, OnTaskCompletionListener listener) {
        super(context);
        this.task = task;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task_completion);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        tvTaskName = findViewById(R.id.tv_task_name);
        tvStarsEarned = findViewById(R.id.tv_stars_earned);
        ivTaskIcon = findViewById(R.id.iv_task_icon);
        btnComplete = findViewById(R.id.btn_complete);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupData() {
        tvTaskName.setText(task.getName());
        tvStarsEarned.setText("You will earn " + task.getStarsPerCompletion() + " ⭐");

        // Set task icon (you can implement this based on task type)
        ivTaskIcon.setImageResource(R.drawable.ic_task_default);
    }

    private void setupClickListeners() {
        btnComplete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskCompleted(task);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
