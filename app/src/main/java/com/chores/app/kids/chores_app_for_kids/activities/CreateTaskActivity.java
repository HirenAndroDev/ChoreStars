package com.chores.app.kids.chores_app_for_kids.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.IconSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.KidSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.IconSelectionDialog;
import com.chores.app.kids.chores_app_for_kids.dialogs.PresetSelectionDialog;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.TaskIcon;
import com.chores.app.kids.chores_app_for_kids.models.TaskPreset;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnClose, ivTaskIcon;
    private TextView btnAdd, tvUsePreset, tvStartDate, tvRepeat, tvSelectedTime;
    private EditText etTaskName, etNotes;
    private CardView cardTaskIcon, cardEditIcon;
    private RecyclerView recyclerViewKids;
    private TextView tvStarCount;
    private ImageView ivStarMinus, ivStarPlus;
    private LinearLayout layoutCustomDays;
    private Switch switchTime, switchPhotoProof;

    // Day buttons
    private TextView btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday;
    private List<TextView> dayButtons;
    private List<Boolean> selectedDays;

    // Data
    private KidSelectionAdapter kidAdapter;
    private List<User> familyKids;
    private List<String> selectedKids;
    private String familyId;
    private String selectedIconUrl = "";
    private int starReward = 1;
    private String repeatType = "everyday";
    private Calendar selectedDate;
    private Calendar selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        initializeData();
        initializeViews();
        setupKidSelection();
        setupStarCounter();
        setupClickListeners();
        loadFamilyKids();
    }

    private void initializeData() {
        familyId = AuthHelper.getFamilyId(this);
        android.util.Log.d("CreateTaskActivity", "familyId from AuthHelper: '" + familyId + "'");

        // Debug: Check what's in SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("NeatKidPrefs", MODE_PRIVATE);
        String storedFamilyId = prefs.getString("family_id", "");
        String userRole = prefs.getString("user_role", "");
        String userName = prefs.getString("user_name", "");
        boolean isChildAccount = prefs.getBoolean("is_child_account", false);

        android.util.Log.d("CreateTaskActivity", "SharedPrefs - familyId: '" + storedFamilyId + "', role: '" + userRole + "', name: '" + userName + "', isChild: " + isChildAccount);

        selectedKids = new ArrayList<>();
        familyKids = new ArrayList<>();
        selectedDate = Calendar.getInstance();
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
        btnAdd = findViewById(R.id.btn_add);
        ivTaskIcon = findViewById(R.id.iv_task_icon);
        cardTaskIcon = findViewById(R.id.card_task_icon);
        cardEditIcon = findViewById(R.id.card_edit_icon);
        etTaskName = findViewById(R.id.et_task_name);
        tvUsePreset = findViewById(R.id.tv_use_preset);
        etNotes = findViewById(R.id.et_notes);
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        tvStarCount = findViewById(R.id.tv_star_count);
        ivStarMinus = findViewById(R.id.iv_star_minus);
        ivStarPlus = findViewById(R.id.iv_star_plus);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvRepeat = findViewById(R.id.tv_repeat);
        layoutCustomDays = findViewById(R.id.layout_custom_days);
        switchTime = findViewById(R.id.switch_time);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        switchPhotoProof = findViewById(R.id.switch_photo_proof);

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

        // Set initial values
        tvStarCount.setText(String.valueOf(starReward));
        updateStartDateDisplay();
        updateTimeDisplay();
    }

    private void setupKidSelection() {
        kidAdapter = new KidSelectionAdapter(familyKids, this, new KidSelectionAdapter.OnKidSelectionChangedListener() {
            @Override
            public void onKidSelectionChanged(List<String> selectedKidIds) {
                android.util.Log.d("CreateTaskActivity", "onKidSelectionChanged called with " + selectedKidIds.size() + " kids");
                for (String kidId : selectedKidIds) {
                    android.util.Log.d("CreateTaskActivity", "Selected kid ID: " + kidId);
                }
                selectedKids = selectedKidIds;
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
        btnAdd.setOnClickListener(v -> createTask());

        cardEditIcon.setOnClickListener(v -> showIconSelectionDialog());
        tvUsePreset.setOnClickListener(v -> showPresetSelectionDialog());

        tvStartDate.setOnClickListener(v -> showDatePicker());
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

    private void showIconSelectionDialog() {
        IconSelectionDialog dialog = IconSelectionDialog.newInstance();
        dialog.setOnIconSelectedListener(new IconSelectionDialog.OnIconSelectedListener() {
            @Override
            public void onIconSelected(TaskIcon icon) {
                selectedIconUrl = icon.getIconUrl();
                // Also store drawable name for default icons
                if (icon.getIconUrl() != null && !icon.getIconUrl().isEmpty()) {
                    selectedIconUrl = icon.getIconUrl(); // Use drawable name as identifier
                }
                updateTaskIcon(icon);
            }
        });
        dialog.show(getSupportFragmentManager(), "IconSelectionDialog");
    }

    private void showPresetSelectionDialog() {
        PresetSelectionDialog dialog = PresetSelectionDialog.newInstance();
        dialog.setOnPresetSelectedListener(new PresetSelectionDialog.OnPresetSelectedListener() {
            @Override
            public void onPresetSelected(TaskPreset preset) {
                applyPresetToTask(preset);
            }
        });
        dialog.show(getSupportFragmentManager(), "PresetSelectionDialog");
    }

    private void applyPresetToTask(TaskPreset preset) {
        // Set task name
        etTaskName.setText(preset.getName());

        // Set star reward
        starReward = preset.getStarReward();
        tvStarCount.setText(String.valueOf(starReward));

        // Set description in notes if available
        if (preset.getDescription() != null && !preset.getDescription().isEmpty()) {
            etNotes.setText(preset.getDescription());
        }

        // Set icon if available
        if (preset.getIconUrl() != null && !preset.getIconUrl().isEmpty()) {
            selectedIconUrl = preset.getIconUrl();

            // Load the preset icon into the task icon view
            Glide.with(this)
                    .load(preset.getIconUrl())
                    .placeholder(R.drawable.ic_task_default)
                    .error(R.drawable.ic_task_default)
                    .into(ivTaskIcon);
        }

        // Show confirmation toast
        Toast.makeText(this, "Preset applied: " + preset.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateStartDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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

    private void showRepeatOptions() {
        String[] options = {"Every Day", "Once", "Specific Days"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Repeat")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            repeatType = "everyday";
                            tvRepeat.setText("Every Day");
                            layoutCustomDays.setVisibility(View.GONE);
                            break;
                        case 1:
                            repeatType = "once";
                            tvRepeat.setText("Once");
                            layoutCustomDays.setVisibility(View.GONE);
                            break;
                        case 2:
                            repeatType = "specific";
                            tvRepeat.setText("Specific Days");
                            layoutCustomDays.setVisibility(View.VISIBLE);
                            break;
                    }
                })
                .show();
    }

    private void updateStartDateDisplay() {
        Calendar today = Calendar.getInstance();
        if (isSameDay(selectedDate, today)) {
            tvStartDate.setText("Today");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            tvStartDate.setText(sdf.format(selectedDate.getTime()));
        }
    }

    private void updateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        tvSelectedTime.setText(sdf.format(selectedTime.getTime()));
    }

    private void updateDayButton(int dayIndex) {
        TextView dayButton = dayButtons.get(dayIndex);
        boolean isSelected = selectedDays.get(dayIndex);

        dayButton.setSelected(isSelected);
        dayButton.setTextColor(isSelected ? getColor(android.R.color.white) : getColor(android.R.color.darker_gray));
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void loadFamilyKids() {
        android.util.Log.d("CreateTaskActivity", "loadFamilyKids called with familyId: " + familyId);

        if (familyId == null || familyId.isEmpty()) {
            android.util.Log.e("CreateTaskActivity", "familyId is null or empty! Attempting to load from Firebase user...");

            // Try to get familyId from current Firebase user as fallback
            FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    android.util.Log.d("CreateTaskActivity", "Firebase user loaded: " + user.getName() + ", familyId: " + user.getFamilyId());

                    if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                        familyId = user.getFamilyId();
                        // Save to SharedPreferences for future use
                        android.content.SharedPreferences prefs = getSharedPreferences("NeatKidPrefs", MODE_PRIVATE);
                        prefs.edit().putString("family_id", familyId).apply();
                        android.util.Log.d("CreateTaskActivity", "Recovered familyId: " + familyId);

                        // Now load children with recovered familyId
                        loadFamilyKidsWithId(familyId);
                    } else {
                        android.util.Log.e("CreateTaskActivity", "Firebase user also has no familyId!");
                        Toast.makeText(CreateTaskActivity.this, "No family found. Please check your account setup.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("CreateTaskActivity", "Failed to load Firebase user: " + error);
                    Toast.makeText(CreateTaskActivity.this, "Unable to load user data. Please try logging in again.", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        loadFamilyKidsWithId(familyId);
    }

    private void loadFamilyKidsWithId(String familyId) {
        // Debug: Also check all users with role "child" to see what familyIds they have
        FirebaseHelper.debugAllChildren();

        FirebaseHelper.getFamilyChildren(familyId, new FirebaseHelper.FamilyChildrenCallback() {
            @Override
            public void onChildrenLoaded(List<User> children) {
                android.util.Log.d("CreateTaskActivity", "Loaded " + children.size() + " children");
                for (User child : children) {
                    android.util.Log.d("CreateTaskActivity", "Child: " + child.getName() + " (ID: " + child.getUserId() + ")");
                }

                // Use adapter's updateKidList method for better handling
                kidAdapter.updateKidList(children);

                // Pre-select kid if passed from intent
                String selectedKidId = getIntent().getStringExtra("selectedKidId");
                if (selectedKidId != null) {
                    for (User kid : children) {
                        if (selectedKidId.equals(kid.getUserId())) {
                            List<String> preSelectedKids = new ArrayList<>();
                            preSelectedKids.add(selectedKidId);
                            kidAdapter.setSelectedKids(preSelectedKids);

                            // IMPORTANT: Also update the selectedKids list used for validation
                            selectedKids.clear();
                            selectedKids.addAll(preSelectedKids);

                            android.util.Log.d("CreateTaskActivity", "Pre-selected kid: " + kid.getName() + " (ID: " + selectedKidId + ")");
                            android.util.Log.d("CreateTaskActivity", "selectedKids list now contains: " + selectedKids.size() + " kids");
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("CreateTaskActivity", "Failed to load kids: " + error);
                Toast.makeText(CreateTaskActivity.this, "Failed to load family members: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTask() {
        if (!validateInput()) {
            return;
        }

        // Show loading state
        btnAdd.setEnabled(false);
        btnAdd.setText("Adding...");

        // Create task object
        Task task = new Task();
        task.setName(etTaskName.getText().toString().trim());
        task.setNotes(etNotes.getText().toString().trim());
        task.setIconUrl(selectedIconUrl);
        task.setStarReward(starReward);
        task.setAssignedKids(selectedKids);
        task.setFamilyId(familyId);
        task.setCreatedBy(AuthHelper.getCurrentUserId());
        task.setRepeatType(repeatType);

        // Set start date timestamp (normalize to start of day)
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTimeInMillis(selectedDate.getTimeInMillis());
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        task.setStartDateTimestamp(startOfDay.getTimeInMillis());

        task.setPhotoProofRequired(switchPhotoProof.isChecked());
        task.setStatus("active");
        task.setCreatedTimestamp(System.currentTimeMillis());

        if (switchTime.isChecked()) {
            task.setReminderTime(selectedTime.get(Calendar.HOUR_OF_DAY) + ":" +
                    String.format("%02d", selectedTime.get(Calendar.MINUTE)));
        }

        if (repeatType.equals("specific")) {
            List<Integer> days = new ArrayList<>();
            for (int i = 0; i < selectedDays.size(); i++) {
                if (selectedDays.get(i)) {
                    days.add(i + 1); // 1-7 for Sunday-Saturday
                }
            }
            task.setCustomDays(days);
        }

        // Save to Firebase
        FirebaseHelper.addTask(task, taskResult -> {
            btnAdd.setEnabled(true);
            btnAdd.setText("Add");

            if (taskResult.isSuccessful()) {
                Toast.makeText(CreateTaskActivity.this, "Task created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMessage = taskResult.getException() != null ?
                        taskResult.getException().getMessage() : "Failed to create task";
                Toast.makeText(CreateTaskActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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

        android.util.Log.d("CreateTaskActivity", "Validating kids selection. selectedKids size: " + selectedKids.size());
        for (String kidId : selectedKids) {
            android.util.Log.d("CreateTaskActivity", "Validating - Selected kid ID: " + kidId);
        }

        if (selectedKids.isEmpty()) {
            android.util.Log.e("CreateTaskActivity", "Validation failed: selectedKids is empty!");
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

        android.util.Log.d("CreateTaskActivity", "Validation passed with " + selectedKids.size() + " selected kids");
        return true;
    }

    private void updateTaskIcon(TaskIcon icon) {
        // Load the selected icon using appropriate method
        if (icon.getIconUrl() != null && !icon.getIconUrl().isEmpty()) {
            // Load from URL using Glide
            Glide.with(this)
                    .load(icon.getIconUrl())
                    .placeholder(R.drawable.ic_brush_teeth)
                    .error(R.drawable.ic_brush_teeth)
                    .into(ivTaskIcon);
        }  else {
            // Default icon
            ivTaskIcon.setImageResource(R.drawable.ic_brush_teeth);
        }
    }
}
