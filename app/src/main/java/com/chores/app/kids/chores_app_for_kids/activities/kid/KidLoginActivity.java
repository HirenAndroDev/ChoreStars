package com.chores.app.kids.chores_app_for_kids.activities.kid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Family;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

public class KidLoginActivity extends AppCompatActivity {
    private TextView tvTitle;
    private TextView tvSubtitle;
    private EditText etInviteCode;
    private Button btnJoin;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_login);

        initViews();
        initManagers();
        setupClickListeners();
        setupTextWatcher();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        etInviteCode = findViewById(R.id.et_invite_code);
        btnJoin = findViewById(R.id.btn_join);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
    }

    private void setupClickListeners() {
        btnJoin.setOnClickListener(v -> {
            String inviteCode = etInviteCode.getText().toString().trim();
            if (validateInviteCode(inviteCode)) {
                joinFamilyWithCode(inviteCode);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }

    private void setupTextWatcher() {
        etInviteCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnJoin.setEnabled(s.length() == 6); // Enable when 6 digits entered
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInviteCode(String inviteCode) {
        if (inviteCode.isEmpty()) {
            etInviteCode.setError(getString(R.string.validation_invite_code_required));
            return false;
        }

        if (inviteCode.length() != 6) {
            etInviteCode.setError("Invite code must be 6 digits");
            return false;
        }

        return true;
    }

    private void joinFamilyWithCode(String inviteCode) {
        showLoading(true);

        firebaseManager.getFamilyByInviteCode(inviteCode, task -> {
            showLoading(false);

            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                Family family = task.getResult().getDocuments().get(0).toObject(Family.class);
                if (family != null) {
                    proceedToKidProfileCreation(family);
                }
            } else {
                Toast.makeText(this, getString(R.string.error_invalid_invite_code), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToKidProfileCreation(Family family) {
        // Save family information temporarily
        prefManager.setFamilyId(family.getFamilyId());
        prefManager.setUserRole(Constants.ROLE_KID);

        // Navigate to kid profile creation
        Intent intent = new Intent(this, KidProfileCreationActivity.class);
        intent.putExtra(Constants.EXTRA_FAMILY_ID, family.getFamilyId());
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnJoin.setEnabled(!show);
        etInviteCode.setEnabled(!show);
    }
}