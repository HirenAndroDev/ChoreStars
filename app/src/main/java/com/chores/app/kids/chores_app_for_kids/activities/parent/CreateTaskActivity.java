package com.chores.app.kids.chores_app_for_kids.activities.parent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.KidSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Kid;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.DateTimeUtils;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CreateTaskActivity extends AppCompatActivity implements KidSelectionAdapter.OnKidSelectionListener {
    private static final String TAG = "CreateTaskActivity";

    // Views
    private Toolbar toolbar;
    private TextInputLayout tilTaskName;
    private TextInputEditText etTaskName;
    private TextInputLayout tilTaskDescription;
    private TextInputEditText etTaskDescription;
    private ChipGroup chipGroupIcons;
    private RecyclerView recyclerViewKids;
    private TextView tvStarsValue;
    private MaterialButton btnDecreaseStars, btnIncreaseStars;
    private TextView tvStartDate;
    private LinearLayout layoutStartDate;
    private ChipGroup chipGroupRepeat;
    private TextView tvReminderTime;
    private LinearLayout layoutReminderTime;
    private Switch switchPhotoProof;
    private FloatingActionButton fabSave;

    // Data
    private KidSelectionAdapter kidAdapter;
    private List<Kid> familyKids;
    private List<String> selectedKidIds;
    private String selectedIcon = Constants.ICON_BRUSH_TEETH;
    private int starsPerCompletion = 1;
    private long startDate = System.currentTimeMillis();
    private String repeatFrequency = Constants.REPEAT_DAILY;
    private String reminderTime = "";
    private boolean photoProofRequired = false;

    // Edit mode
    private boolean isEditMode = false;
    private String taskId = null;
    private Task existingTask = null;

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        initViews();
        initManagers();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        checkEditMode();
        loadFamilyKids();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilTaskName = findViewById(R.id.til_task_name);
        etTaskName = findViewById(R.id.et_task_name);
        tilTaskDescription = findViewById(R.id.til_task_description);
        etTaskDescription = findViewById(R.id.et_task_description);
        chipGroupIcons = findViewById(R.id.chip_group_icons);
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        tvStarsValue = findViewById(R.id.tv_stars_value);
        btnDecreaseStars = findViewById(R.id.btn_decrease_stars);
        btnIncreaseStars = findViewById(R.id.btn_increase_stars);
        layoutStartDate = findViewById(R.id.layout_start_date);
        tvStartDate = findViewById(R.id.tv_start_date);
        chipGroupRepeat = findViewById(R.id.chip_group_repeat);
        layoutReminderTime = findViewById(R.id.layout_reminder_time);
        tvReminderTime = findViewById(R.id.tv_reminder_time);
        switchPhotoProof = findViewById(R.id.switch_photo_proof);
        fabSave = findViewById(R.id.fab_save);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        familyKids = new ArrayList<>();
        selectedKidIds = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        kidAdapter = new KidSelectionAdapter(familyKids, selectedKidIds, this);
        recyclerViewKids.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewKids.setAdapter(kidAdapter);
    }

    private void setupClickListeners() {
        // Icon selection
        setupIconChips();

        // Stars selection
        btnDecreaseStars.setOnClickListener(v -> {
            if (starsPerCompletion > 1) {
                starsPerCompletion--;
                updateStarsDisplay();
            }
        });

        btnIncreaseStars.setOnClickListener(v -> {
            if (starsPerCompletion < 10) {
                starsPerCompletion++;
                updateStarsDisplay();
            }
        });

        // Start date selection
        layoutStartDate.setOnClickListener(v -> showDatePicker());

        // Repeat frequency selection
        setupRepeatChips();

        // Reminder time selection
        layoutReminderTime.setOnClickListener(v -> showTimePicker());

        // Photo proof toggle
        switchPhotoProof.setOnCheckedChangeListener((buttonView, isChecked) -> {
            photoProofRequired = isChecked;
        });

        // Save button
        fabSave.setOnClickListener(v -> saveTask());
    }

    private void setupIconChips() {
        String[] iconNames = {
                Constants.ICON_BRUSH_TEETH,
                Constants.ICON_PUT_AWAY_TOYS,
                Constants.ICON_FOLD_CLOTHES,
                Constants.ICON_DO_LAUNDRY
        };

        String[] iconLabels = {
                getString(R.string.brush_teeth),
                getString(R.string.put_away_toys),
                getString(R.string.fold_clothes),
                getString(R.string.do_laundry)
        };

        int[] iconResources = {
                R.drawable.ic_brush_teeth,
                R.drawable.ic_toys,
                R.drawable.ic_clothes,
                R.drawable.ic_laundry
        };

        for (int i = 0; i < iconNames.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(iconLabels[i]);
            chip.setChipIcon(getDrawable(iconResources[i]));
            chip.setCheckable(true);
            chip.setChecked(iconNames[i].equals(selectedIcon));

            final String iconName = iconNames[i];
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedIcon = iconName;
                    // Uncheck other chips
                    for (int j = 0; j < chipGroupIcons.getChildCount(); j++) {
                        Chip otherChip = (Chip) chipGroupIcons.getChildAt(j);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                }
            });

            chipGroupIcons.addView(chip);
        }
    }

    private void setupRepeatChips() {
        String[] frequencies = {Constants.REPEAT_DAILY, "weekly", "monthly"};
        String[] labels = {"Daily", "Weekly", "Monthly"};

        for (int i = 0; i < frequencies.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(frequencies[i].equals(repeatFrequency));

            final String frequency = frequencies[i];
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    repeatFrequency = frequency;
                    // Uncheck other chips
                    for (int j = 0; j < chipGroupRepeat.getChildCount(); j++) {
                        Chip otherChip = (Chip) chipGroupRepeat.getChildAt(j);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                }
            });

            chipGroupRepeat.addView(chip);
        }
    }

    private void checkEditMode() {
        taskId = getIntent().getStringExtra("task_id");
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);

        if (isEditMode && taskId != null) {
            setTitle("Edit Task");
            loadExistingTask();
        } else {
            setTitle("New Task");
            updateInitialValues();
        }
    }

    private void loadExistingTask() {
        // Load task from Firebase
        firebaseManager.getTaskById(taskId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                existingTask = task.getResult().toObject(Task.class);
                if (existingTask != null) {
                    populateFields();
                }
            } else {
                showError("Failed to load task");
                finish();
            }
        });
    }

    private void populateFields() {
        etTaskName.setText(existingTask.getName());
        etTaskDescription.setText(existingTask.getDescription());
        selectedIcon = existingTask.getIcon();
        starsPerCompletion = existingTask.getStarsPerCompletion();
        startDate = existingTask.getStartDate();
        repeatFrequency = existingTask.getRepeatFrequency();
        reminderTime = existingTask.getReminderTime();
        photoProofRequired = existingTask.isPhotoProofRequired();
        selectedKidIds.clear();
        selectedKidIds.addAll(existingTask.getAssignedKids());

        updateAllDisplays();
    }

    private void updateInitialValues() {
        updateStarsDisplay();
        updateStartDateDisplay();
        updateReminderTimeDisplay();
    }

    private void updateAllDisplays() {
        updateStarsDisplay();
        updateStartDateDisplay();
        updateReminderTimeDisplay();
        switchPhotoProof.setChecked(photoProofRequired);

        // Update chip selections
        updateIconChipSelection();
        updateRepeatChipSelection();

        // Update kid selection
        kidAdapter.updateSelectedKids(selectedKidIds);
    }

    private void updateIconChipSelection() {
        for (int i = 0; i < chipGroupIcons.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupIcons.getChildAt(i);
            // You'd need to store the icon name as a tag or implement proper checking
            chip.setChecked(false); // Reset and set correct one based on selectedIcon
        }
    }

    private void updateRepeatChipSelection() {
        for (int i = 0; i < chipGroupRepeat.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRepeat.getChildAt(i);
            // You'd need to store the frequency as a tag or implement proper checking
            chip.setChecked(false); // Reset and set correct one based on repeatFrequency
        }
    }

    private void loadFamilyKids() {
        String familyId = prefManager.getFamilyId();
        if (familyId != null) {
            firebaseManager.getFamilyKids(familyId, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    familyKids.clear();
                    familyKids.addAll(task.getResult().toObjects(Kid.class));
                    kidAdapter.notifyDataSetChanged();
                } else {
                    showError("Failed to load family kids");
                }
            });
        }
    }

    private void updateStarsDisplay() {
        tvStarsValue.setText(String.valueOf(starsPerCompletion));
    }

    private void updateStartDateDisplay() {
        tvStartDate.setText(DateTimeUtils.formatDate(startDate));
    }

    private void updateReminderTimeDisplay() {
        if (reminderTime.isEmpty()) {
            tvReminderTime.setText("Set reminder time");
            tvReminderTime.setTextColor(getColor(R.color.text_hint));
        } else {
            tvReminderTime.setText(reminderTime);
            tvReminderTime.setTextColor(getColor(R.color.text_primary));
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDate);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    startDate = selectedDate.getTimeInMillis();
                    updateStartDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderTime = String.format("%02d:%02d", hourOfDay, minute);
                    updateReminderTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );

        timePicker.show();
    }

    private void saveTask() {
        if (!validateInputs()) {
            return;
        }

        String taskName = etTaskName.getText().toString().trim();
        String taskDescription = etTaskDescription.getText().toString().trim();

        if (isEditMode && existingTask != null) {
            updateExistingTask(taskName, taskDescription);
        } else {
            createNewTask(taskName, taskDescription);
        }
    }

    private boolean validateInputs() {
        String taskName = etTaskName.getText().toString().trim();

        if (taskName.isEmpty()) {
            tilTaskName.setError(getString(R.string.validation_task_name_required));
            return false;
        } else {
            tilTaskName.setError(null);
        }

        if (selectedKidIds.isEmpty()) {
            showError("Please select at least one kid");
            return false;
        }

        return true;
    }

    private void createNewTask(String taskName, String taskDescription) {
        String newTaskId = UUID.randomUUID().toString();
        String familyId = prefManager.getFamilyId();
        String userId = prefManager.getUserId();

        Task newTask = new Task(newTaskId, taskName, familyId, userId);
        newTask.setDescription(taskDescription);
        newTask.setIcon(selectedIcon);
        newTask.setAssignedKids(new ArrayList<>(selectedKidIds));
        newTask.setStarsPerCompletion(starsPerCompletion);
        newTask.setStartDate(startDate);
        newTask.setRepeatFrequency(repeatFrequency);
        newTask.setReminderTime(reminderTime);
        newTask.setPhotoProofRequired(photoProofRequired);

        showLoading(true);
        firebaseManager.createTask(newTask, task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                showSuccess(getString(R.string.success_task_created));
                finish();
            } else {
                showError(getString(R.string.error_task_creation));
            }
        });
    }

    private void updateExistingTask(String taskName, String taskDescription) {
        existingTask.setName(taskName);
        existingTask.setDescription(taskDescription);
        existingTask.setIcon(selectedIcon);
        existingTask.setAssignedKids(new ArrayList<>(selectedKidIds));
        existingTask.setStarsPerCompletion(starsPerCompletion);
        existingTask.setStartDate(startDate);
        existingTask.setRepeatFrequency(repeatFrequency);
        existingTask.setReminderTime(reminderTime);
        existingTask.setPhotoProofRequired(photoProofRequired);
        existingTask.setUpdatedAt(System.currentTimeMillis());

        showLoading(true);
        firebaseManager.createTask(existingTask, task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                showSuccess("Task updated successfully");
                finish();
            } else {
                showError("Failed to update task");
            }
        });
    }

    @Override
    public void onKidSelectionChanged(String kidId, boolean isSelected) {
        if (isSelected) {
            if (!selectedKidIds.contains(kidId)) {
                selectedKidIds.add(kidId);
            }
        } else {
            selectedKidIds.remove(kidId);
        }
    }

    private void showLoading(boolean show) {
        fabSave.setEnabled(!show);
        // You can show a progress dialog here
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
