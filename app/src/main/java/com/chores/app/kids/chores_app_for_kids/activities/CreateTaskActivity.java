package com.chores.app.kids.chores_app_for_kids.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.IconSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.KidSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;
import java.util.ArrayList;
import java.util.List;

public class CreateTaskActivity extends AppCompatActivity {

    // UI Components
    private EditText etTaskName;
    private RecyclerView recyclerViewIcons;
    private TextView tvStarCount;
    private ImageView ivStarMinus, ivStarPlus;
    private RecyclerView recyclerViewKids;
    private RadioGroup rgRepeatType;
    private LinearLayout layoutWeekdays;
    private EditText etReminderTime;
    private Switch switchPhotoProof;
    private Button btnCreateTask, btnCancel;

    // Data
    private IconSelectionAdapter iconAdapter;
    private KidSelectionAdapter kidAdapter;
    private List<String> availableIcons;
    private List<User> familyKids;
    private String selectedIcon = "";
    private int starReward = 1;
    private List<String> selectedKids;
    private String familyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        initializeData();
        initializeViews();
        setupIconSelection();
        setupKidSelection();
        setupStarCounter();
        setupWeekdaySelection();
        setupClickListeners();
        loadFamilyKids();
    }

    private void initializeData() {
        familyId = AuthHelper.getFamilyId(this);
        selectedKids = new ArrayList<>();

        // Available task icons
        availableIcons = new ArrayList<>();
        availableIcons.add("ic_brush_teeth");
        availableIcons.add("ic_toys");
        availableIcons.add("ic_clothes");
        availableIcons.add("ic_laundry");
        availableIcons.add("ic_dishes");
        availableIcons.add("ic_bed");
        availableIcons.add("ic_homework");
        availableIcons.add("ic_pets");
        availableIcons.add("ic_trash");
        availableIcons.add("ic_plants");
        availableIcons.add("ic_vacuum");
        availableIcons.add("ic_bathroom");

        familyKids = new ArrayList<>();
    }

    private void initializeViews() {
        etTaskName = findViewById(R.id.et_task_name);
        recyclerViewIcons = findViewById(R.id.recycler_view_icons);
        tvStarCount = findViewById(R.id.tv_star_count);
        ivStarMinus = findViewById(R.id.iv_star_minus);
        ivStarPlus = findViewById(R.id.iv_star_plus);
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        rgRepeatType = findViewById(R.id.rg_repeat_type);
        layoutWeekdays = findViewById(R.id.layout_weekdays);
        etReminderTime = findViewById(R.id.et_reminder_time);
        switchPhotoProof = findViewById(R.id.switch_photo_proof);
        btnCreateTask = findViewById(R.id.btn_create_task);
        btnCancel = findViewById(R.id.btn_cancel);

        // Set initial star count
        tvStarCount.setText(String.valueOf(starReward));
    }

    private void setupIconSelection() {
        iconAdapter = new IconSelectionAdapter(availableIcons, this, new IconSelectionAdapter.OnIconSelectedListener() {
            @Override
            public void onIconSelected(String iconName) {
                selectedIcon = iconName;
            }
        });

        recyclerViewIcons.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerViewIcons.setAdapter(iconAdapter);
    }

    private void setupKidSelection() {
        kidAdapter = new KidSelectionAdapter(familyKids, this, new KidSelectionAdapter.OnKidSelectionChangedListener() {
            @Override
            public void onKidSelectionChanged(List<String> selectedKidIds) {
                selectedKids = selectedKidIds;
            }
        });

        recyclerViewKids.setLayoutManager(new LinearLayoutManager(this));
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

    private void setupWeekdaySelection() {
        rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_custom) {
                layoutWeekdays.setVisibility(View.VISIBLE);
            } else {
                layoutWeekdays.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        btnCreateTask.setOnClickListener(v -> createTask());
        btnCancel.setOnClickListener(v -> finish());

        etReminderTime.setOnClickListener(v -> showTimePicker());
    }

    private void loadFamilyKids() {
        FirebaseHelper.getFamilyChildren(familyId, new FirebaseHelper.FamilyChildrenCallback() {
            @Override
            public void onChildrenLoaded(List<User> children) {
                familyKids.clear();
                familyKids.addAll(children);
                kidAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CreateTaskActivity.this, "Failed to load family members: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTask() {
        if (!validateInput()) {
            return;
        }

        // Show loading state
        btnCreateTask.setEnabled(false);
        btnCreateTask.setText("Creating...");

        // Create task object
        Task task = new Task();
        task.setName(etTaskName.getText().toString().trim());
        task.setIconName(selectedIcon);
        task.setStarReward(starReward);
        task.setAssignedKids(selectedKids);
        task.setFamilyId(familyId);
        task.setCreatedBy(AuthHelper.getCurrentUserId());
        task.setRepeatType(getSelectedRepeatType());
        task.setReminderTime(etReminderTime.getText().toString().trim());
        task.setPhotoProofRequired(switchPhotoProof.isChecked());
        task.setStatus("active");
        task.setCreatedTimestamp(System.currentTimeMillis());

        // Save to Firebase
        FirebaseHelper.addTask(task, taskResult -> {
            btnCreateTask.setEnabled(true);
            btnCreateTask.setText("Create Task");

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

        if (TextUtils.isEmpty(selectedIcon)) {
            Toast.makeText(this, "Please select an icon for the task", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedKids.isEmpty()) {
            Toast.makeText(this, "Please select at least one child for this task", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getSelectedRepeatType() {
        int selectedId = rgRepeatType.getCheckedRadioButtonId();

        if (selectedId == R.id.rb_daily) {
            return "daily";
        } else if (selectedId == R.id.rb_weekly) {
            return "weekly";
        } else if (selectedId == R.id.rb_weekdays) {
            return "weekdays";
        } else if (selectedId == R.id.rb_weekends) {
            return "weekends";
        } else if (selectedId == R.id.rb_custom) {
            return "custom";
        } else {
            return "daily"; // default
        }
    }

    private void showTimePicker() {
        // TODO: Implement time picker dialog
        // For now, just show a placeholder
        etReminderTime.setText("08:00");
    }

    private List<Integer> getSelectedWeekdays() {
        List<Integer> selectedDays = new ArrayList<>();

        CheckBox cbMonday = findViewById(R.id.cb_monday);
        CheckBox cbTuesday = findViewById(R.id.cb_tuesday);
        CheckBox cbWednesday = findViewById(R.id.cb_wednesday);
        CheckBox cbThursday = findViewById(R.id.cb_thursday);
        CheckBox cbFriday = findViewById(R.id.cb_friday);
        CheckBox cbSaturday = findViewById(R.id.cb_saturday);
        CheckBox cbSunday = findViewById(R.id.cb_sunday);

        if (cbMonday.isChecked()) selectedDays.add(1);
        if (cbTuesday.isChecked()) selectedDays.add(2);
        if (cbWednesday.isChecked()) selectedDays.add(3);
        if (cbThursday.isChecked()) selectedDays.add(4);
        if (cbFriday.isChecked()) selectedDays.add(5);
        if (cbSaturday.isChecked()) selectedDays.add(6);
        if (cbSunday.isChecked()) selectedDays.add(7);

        return selectedDays;
    }
}