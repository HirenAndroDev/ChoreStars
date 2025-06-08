package com.chores.app.kids.chores_app_for_kids.activities.kid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Kid;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class KidProfileCreationActivity extends AppCompatActivity {
    private static final String TAG = "KidProfileCreation";

    // Views
    private ImageView btnBack;
    private TextView tvTitle;
    private CircleImageView ivProfileImage;
    private ImageView ivCameraIcon;
    private TextInputLayout tilName;
    private TextInputEditText etName;
    private Switch switchTextToSpeech;
    private Button btnAdd;
    private ProgressBar progressBar;

    // Data
    private String familyId;
    private String selectedProfileImage = "";

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_profile_creation);

        initViews();
        initManagers();
        getIntentData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        ivCameraIcon = findViewById(R.id.iv_camera_icon);
        tilName = findViewById(R.id.til_name);
        etName = findViewById(R.id.et_name);
        switchTextToSpeech = findViewById(R.id.switch_text_to_speech);
        btnAdd = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
    }

    private void getIntentData() {
        familyId = getIntent().getStringExtra(Constants.EXTRA_FAMILY_ID);
        if (familyId == null) {
            familyId = prefManager.getFamilyId();
        }

        if (familyId == null) {
            Toast.makeText(this, "Family not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        ivProfileImage.setOnClickListener(v -> selectProfileImage());
        ivCameraIcon.setOnClickListener(v -> selectProfileImage());

        btnAdd.setOnClickListener(v -> createKidProfile());
    }

    private void selectProfileImage() {
        // For now, we'll use a default image
        // You can implement image picker later
        Toast.makeText(this, "Profile image selection coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void createKidProfile() {
        String name = etName.getText().toString().trim();

        if (!validateInput(name)) {
            return;
        }

        showLoading(true);

        // Generate unique kid ID
        String kidId = UUID.randomUUID().toString();

        // Create kid object
        Kid kid = new Kid(kidId, name, familyId);
        kid.setTextToSpeechEnabled(switchTextToSpeech.isChecked());
        kid.setProfileImage(selectedProfileImage);

        // Save to Firebase
        firebaseManager.createKid(kid, task -> {
            showLoading(false);

            if (task.isSuccessful()) {
                // Save kid ID to preferences
                prefManager.setKidId(kidId);
                prefManager.setUserRole(Constants.ROLE_KID);
                prefManager.setLoggedIn(true);

                // Navigate to kid dashboard
                Intent intent = new Intent(this, KidDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

                Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create profile. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String name) {
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.validation_name_required));
            return false;
        } else {
            tilName.setError(null);
        }

        if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            return false;
        }

        if (name.length() > 20) {
            tilName.setError("Name must be less than 20 characters");
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAdd.setEnabled(!show);
        etName.setEnabled(!show);
        switchTextToSpeech.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        // Clear any saved data and go back to landing
        prefManager.clearAll();
        Intent intent = new Intent(this, KidLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}