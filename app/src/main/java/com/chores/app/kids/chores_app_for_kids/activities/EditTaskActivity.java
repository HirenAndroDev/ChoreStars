package com.chores.app.kids.chores_app_for_kids.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.IconSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.KidSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.IconSelectionDialog;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.TaskIcon;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditTaskActivity extends AppCompatActivity {

    private static final String TAG = "EditTaskActivity";
    public static final String EXTRA_TASK_ID = "extra_task_id";

    // UI Components
    private ImageView btnClose, ivTaskIcon, ivEditIcon;
    private TextView btnDone, tvChildName, tvStarCount, tvRepeat, tvSelectedTime, btnDeleteTask;
    private EditText etTaskName, etNotes;
    private CircleImageView ivChildAvatar;
    private RecyclerView recyclerViewKids;
    private ImageView ivStarMinus, ivStarPlus;
    private LinearLayout layoutCustomDays;
    private Switch switchTime, switchPhotoProof;
    private TextView btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday;

    // Data
    private Task currentTask;
    private String taskId;
    private String familyId;
    private List<User> familyKids;
    private List<String> selectedKids;
    private KidSelectionAdapter kidAdapter;
    private String selectedIconUrl = "";
    private int starReward = 1;
    private String repeatType = "everyday";
    private Calendar selectedTime;
    private List<TextView> dayButtons;
    private List<Boolean> selectedDays;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // Get task ID from intent
        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Invalid task ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeData();
        initializeViews();
        setupClickListeners();
        loadTaskData();
    }

    private void initializeData() {
        firebaseHelper = new FirebaseHelper();
        familyId = AuthHelper.getFamilyId(this);
        selectedKids = new ArrayList<>();
        familyKids = new ArrayList<>();
        selectedTime = Calendar.getInstance();
        selectedTime.set(Calendar.HOUR_OF_DAY, 8);
        selectedTime.set(Calendar.MINUTE, 0);

        selectedDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            selectedDays.add(false);
        }
    }

    private void initializeViews() {
        btnClose = findViewById(R.id.btn_close);
        btnDone = findViewById(R.id.btn_done);
        ivTaskIcon = findViewById(R.id.iv_task_icon);
        ivEditIcon = findViewById(R.id.iv_edit_icon);
        tvChildName = findViewById(R.id.tv_child_name);
        ivChildAvatar = findViewById(R.id.iv_child_avatar);
        etTaskName = findViewById(R.id.et_task_name);
        etNotes = findViewById(R.id.et_notes);
        tvStarCount = findViewById(R.id.tv_star_count);
        ivStarMinus = findViewById(R.id.iv_star_minus);
        ivStarPlus = findViewById(R.id.iv_star_plus);
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        tvRepeat = findViewById(R.id.tv_repeat);
        layoutCustomDays = findViewById(R.id.layout_custom_days);
        switchTime = findViewById(R.id.switch_time);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        switchPhotoProof = findViewById(R.id.switch_photo_proof);
        btnDeleteTask = findViewById(R.id.btn_delete_task);

        // Day buttons
        btnSunday = findViewById(R.id.btn_sunday);
        btnMonday = findViewById(R.id.btn_monday);
        btnTuesday = findViewById(R.id.btn_tuesday);
        btnWednesday = findViewById(R.id.btn_wednesday);
        btnThursday = findViewById(R.id.btn_thursday);
        btnFriday = findViewById(R.id.btn_friday);
        btnSaturday = findViewById(R.id.btn_saturday);

        dayButtons = new ArrayList<>();
        dayButtons.add(btnSunday);
        dayButtons.add(btnMonday);
        dayButtons.add(btnTuesday);
        dayButtons.add(btnWednesday);
        dayButtons.add(btnThursday);
        dayButtons.add(btnFriday);
        dayButtons.add(btnSaturday);

        setupKidSelection();
        setupStarCounter();
        updateTimeDisplay();
    }

    private void setupKidSelection() {
        kidAdapter = new KidSelectionAdapter(familyKids, this, new KidSelectionAdapter.OnKidSelectionChangedListener() {
            @Override
            public void onKidSelectionChanged(List<String> selectedKidIds) {
                selectedKids = selectedKidIds;
                updateMainChildDisplay();
            }
        });

        recyclerViewKids.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewKids.setAdapter(kidAdapter);
    }

    private void setupStarCounter() {
        ivStarMinus.setOnClickListener(v -> {
            if (starReward > 1) {
                starReward--;
                tvStarCount.setText(String.valueOf(starReward));
            }
        });

        ivStarPlus.setOnClickListener(v -> {
            if (starReward < 10) {
                starReward++;
                tvStarCount.setText(String.valueOf(starReward));
            }
        });
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> updateTask());
        btnDeleteTask.setOnClickListener(v -> showDeleteConfirmation());

        ivEditIcon.setOnClickListener(v -> showIconSelectionDialog());

        tvRepeat.setOnClickListener(v -> showRepeatOptions());

        switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showTimePicker();
                tvSelectedTime.setVisibility(View.VISIBLE);
            } else {
                tvSelectedTime.setVisibility(View.GONE);
            }
        });

        // Setup day buttons
        for (int i = 0; i < dayButtons.size(); i++) {
            final int dayIndex = i;
            dayButtons.get(i).setOnClickListener(v -> {
                selectedDays.set(dayIndex, !selectedDays.get(dayIndex));
                updateDayButton(dayIndex);
            });
        }
    }

    private void loadTaskData() {
        // Get the task from Firestore
        firebaseHelper.getFamilyTasks(familyId, new FirebaseHelper.TasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                currentTask = null;
                for (Task task : tasks) {
                    if (taskId.equals(task.getTaskId())) {
                        currentTask = task;
                        break;
                    }
                }

                if (currentTask != null) {
                    populateTaskData();
                    loadFamilyKids();
                } else {
                    Toast.makeText(EditTaskActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading task: " + error);
                Toast.makeText(EditTaskActivity.this, "Error loading task: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateTaskData() {
        if (currentTask == null) return;

        // Populate task details
        etTaskName.setText(currentTask.getName());
        etNotes.setText(currentTask.getNotes() != null ? currentTask.getNotes() : "");

        starReward = currentTask.getStarReward();
        tvStarCount.setText(String.valueOf(starReward));

        selectedIconUrl = currentTask.getIconUrl() != null ? currentTask.getIconUrl() : "";
        updateTaskIcon();

        repeatType = currentTask.getRepeatType() != null ? currentTask.getRepeatType() : "everyday";
        updateRepeatDisplay();

        // Handle reminder time
        if (currentTask.getReminderTime() != null && !currentTask.getReminderTime().isEmpty()) {
            switchTime.setChecked(true);
            parseReminderTime(currentTask.getReminderTime());
            tvSelectedTime.setVisibility(View.VISIBLE);
            updateTimeDisplay();
        } else {
            switchTime.setChecked(false);
            tvSelectedTime.setVisibility(View.GONE);
        }

        switchPhotoProof.setChecked(currentTask.isPhotoProofRequired());

        // Handle custom days
        if ("specific".equals(repeatType) && currentTask.getCustomDays() != null) {
            layoutCustomDays.setVisibility(View.VISIBLE);
            selectedDays.clear();
            for (int i = 0; i < 7; i++) {
                boolean isSelected = currentTask.getCustomDays().contains(i + 1);
                selectedDays.add(isSelected);
                updateDayButton(i);
            }
        }

        selectedKids = new ArrayList<>(currentTask.getAssignedKids());
    }

    private void loadFamilyKids() {
        firebaseHelper.getFamilyChildren(familyId, new FirebaseHelper.FamilyChildrenCallback() {
            @Override
            public void onChildrenLoaded(List<User> children) {
                familyKids.clear();
                familyKids.addAll(children);
                kidAdapter.updateKidList(children);
                kidAdapter.setSelectedKids(selectedKids);
                updateMainChildDisplay();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading family kids: " + error);
                Toast.makeText(EditTaskActivity.this, "Error loading family members: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMainChildDisplay() {
        if (!selectedKids.isEmpty() && !familyKids.isEmpty()) {
            // Find the first selected kid
            for (User kid : familyKids) {
                if (selectedKids.contains(kid.getUserId())) {
                    tvChildName.setText(kid.getName());

                    // Load profile image
                    if (kid.getProfileImageUrl() != null && !kid.getProfileImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(kid.getProfileImageUrl())
                                .circleCrop()
                                .into(ivChildAvatar);
                    } else {
                        ivChildAvatar.setImageResource(R.drawable.default_avatar);
                    }
                    break;
                }
            }
        } else {
            tvChildName.setText("No child selected");
            ivChildAvatar.setImageResource(R.drawable.default_avatar);
        }
    }

    private void updateTaskIcon() {
        if (selectedIconUrl != null && !selectedIconUrl.isEmpty()) {
            if (selectedIconUrl.startsWith("http")) {
                // Load from URL
                Glide.with(this)
                        .load(selectedIconUrl)
                        .placeholder(R.drawable.ic_task_default)
                        .error(R.drawable.ic_task_default)
                        .into(ivTaskIcon);
            } else {
                // Load from drawable resource
                int iconResId = getResources().getIdentifier(selectedIconUrl, "drawable", getPackageName());
                if (iconResId != 0) {
                    ivTaskIcon.setImageResource(iconResId);
                } else {
                    ivTaskIcon.setImageResource(R.drawable.ic_task_default);
                }
            }
        } else {
            ivTaskIcon.setImageResource(R.drawable.ic_task_default);
        }
    }

    private void showIconSelectionDialog() {
        IconSelectionDialog dialog = IconSelectionDialog.newInstance();
        dialog.setOnIconSelectedListener(new IconSelectionDialog.OnIconSelectedListener() {
            @Override
            public void onIconSelected(TaskIcon icon) {
                selectedIconUrl = icon.getIconUrl();
                if (icon.getIconUrl() != null && !icon.getIconUrl().isEmpty()) {
                    selectedIconUrl = icon.getIconUrl();
                }
                updateTaskIcon();
            }
        });
        dialog.show(getSupportFragmentManager(), "IconSelectionDialog");
    }

    private void showRepeatOptions() {
        String[] options = {"Every Day", "Once", "Specific Days"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Repeat")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            repeatType = "everyday";
                            layoutCustomDays.setVisibility(View.GONE);
                            break;
                        case 1:
                            repeatType = "once";
                            layoutCustomDays.setVisibility(View.GONE);
                            break;
                        case 2:
                            repeatType = "specific";
                            layoutCustomDays.setVisibility(View.VISIBLE);
                            break;
                    }
                    updateRepeatDisplay();
                })
                .show();
    }

    private void updateRepeatDisplay() {
        switch (repeatType) {
            case "everyday":
                tvRepeat.setText("Every Day");
                break;
            case "once":
                tvRepeat.setText("Once");
                break;
            case "specific":
                tvRepeat.setText("Specific Days");
                break;
        }
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    updateTimeDisplay();
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        tvSelectedTime.setText(sdf.format(selectedTime.getTime()));
    }

    private void parseReminderTime(String reminderTime) {
        try {
            String[] parts = reminderTime.split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing reminder time: " + reminderTime, e);
        }
    }

    private void updateDayButton(int dayIndex) {
        TextView dayButton = dayButtons.get(dayIndex);
        boolean isSelected = selectedDays.get(dayIndex);

        dayButton.setSelected(isSelected);
        dayButton.setTextColor(isSelected ? getColor(android.R.color.white) : getColor(android.R.color.darker_gray));
    }

    private void updateTask() {
        if (!validateInput()) {
            return;
        }

        // Show loading state
        btnDone.setEnabled(false);
        btnDone.setText("Saving...");
        btnDone.setTextColor(getColor(android.R.color.darker_gray));

        // Update task object
        currentTask.setName(etTaskName.getText().toString().trim());
        currentTask.setNotes(etNotes.getText().toString().trim());
        currentTask.setIconUrl(selectedIconUrl);
        currentTask.setStarReward(starReward);
        currentTask.setAssignedKids(selectedKids);
        currentTask.setRepeatType(repeatType);
        currentTask.setPhotoProofRequired(switchPhotoProof.isChecked());

        if (switchTime.isChecked()) {
            currentTask.setReminderTime(selectedTime.get(Calendar.HOUR_OF_DAY) + ":" +
                    String.format("%02d", selectedTime.get(Calendar.MINUTE)));
        } else {
            currentTask.setReminderTime(null);
        }

        if (repeatType.equals("specific")) {
            List<Integer> days = new ArrayList<>();
            for (int i = 0; i < selectedDays.size(); i++) {
                if (selectedDays.get(i)) {
                    days.add(i + 1); // 1-7 for Sunday-Saturday
                }
            }
            currentTask.setCustomDays(days);
        } else {
            currentTask.setCustomDays(new ArrayList<>());
        }

        // Save to Firebase
        firebaseHelper.updateTask(currentTask, new FirebaseHelper.OnTaskUpdatedListener() {
            @Override
            public void onTaskUpdated() {
                runOnUiThread(() -> {
                    Toast.makeText(EditTaskActivity.this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnDone.setEnabled(true);
                    btnDone.setText("Done");
                    btnDone.setTextColor(getColor(R.color.blue_primary));
                    Toast.makeText(EditTaskActivity.this, "Error updating task: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validateInput() {
        String taskName = etTaskName.getText().toString().trim();

        if (TextUtils.isEmpty(taskName)) {
            etTaskName.setError("Task name is required");
            etTaskName.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidTaskName(taskName)) {
            etTaskName.setError("Please enter a valid task name");
            etTaskName.requestFocus();
            return false;
        }

        if (selectedKids.isEmpty()) {
            Toast.makeText(this, "Please select at least one child for this task", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (repeatType.equals("specific")) {
            boolean anyDaySelected = false;
            for (Boolean selected : selectedDays) {
                if (selected) {
                    anyDaySelected = true;
                    break;
                }
            }
            if (!anyDaySelected) {
                Toast.makeText(this, "Please select at least one day for the task", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void showDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask() {
        // Show loading state
        btnDeleteTask.setEnabled(false);
        btnDeleteTask.setText("Deleting...");

        firebaseHelper.deleteTask(taskId, task -> {
            runOnUiThread(() -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditTaskActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnDeleteTask.setEnabled(true);
                    btnDeleteTask.setText("Delete Task");
                    String error = task.getException() != null ? task.getException().getMessage() : "Failed to delete task";
                    Toast.makeText(EditTaskActivity.this, "Error deleting task: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}