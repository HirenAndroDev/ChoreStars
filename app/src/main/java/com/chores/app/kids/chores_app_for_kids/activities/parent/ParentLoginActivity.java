// ParentLoginActivity.java
package com.chores.app.kids.chores_app_for_kids.activities.parent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthManager;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

public class ParentLoginActivity extends AppCompatActivity {
    private static final String TAG = "ParentLoginActivity";

    private Button btnContinueWithGoogle;
    private Button btnJoinFamilyCode;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);

        initViews();
        initManagers();
        setupClickListeners();
    }

    private void initViews() {
        btnContinueWithGoogle = findViewById(R.id.btn_continue_with_google);
        btnJoinFamilyCode = findViewById(R.id.btn_join_family_code);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initManagers() {
        authManager = AuthManager.getInstance(this);
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
    }

    private void setupClickListeners() {
        btnContinueWithGoogle.setOnClickListener(v -> signInWithGoogle());
        btnJoinFamilyCode.setOnClickListener(v -> {
            // Navigate to account selection for joining family
            Intent intent = new Intent(this, AccountSelectionActivity.class);
            intent.putExtra("join_family", true);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        showLoading(true);
        Intent signInIntent = authManager.getGoogleSignInIntent();
        startActivityForResult(signInIntent, Constants.REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_GOOGLE_SIGN_IN) {
            authManager.handleGoogleSignInResult(data, task -> {
                showLoading(false);
                if (task != null && task.isSuccessful()) {
                    FirebaseUser firebaseUser = authManager.getCurrentUser();
                    if (firebaseUser != null) {
                        handleSuccessfulLogin(firebaseUser);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleSuccessfulLogin(FirebaseUser firebaseUser) {
        // Check if user exists in Firestore
        firebaseManager.getUser(firebaseUser.getUid(), task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult().exists()) {
                    // User exists, load user data
                    User user = task.getResult().toObject(User.class);
                    if (user != null) {
                        saveUserToPrefs(user);
                        navigateToNextScreen(user);
                    }
                } else {
                    // New user, create user record
                    createNewUser(firebaseUser);
                }
            } else {
                Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewUser(FirebaseUser firebaseUser) {
        User newUser = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                Constants.ROLE_PARENT
        );

        firebaseManager.createUser(newUser, task -> {
            if (task.isSuccessful()) {
                saveUserToPrefs(newUser);
                navigateToNextScreen(newUser);
            } else {
                Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToPrefs(User user) {
        prefManager.setUserId(user.getUserId());
        prefManager.setUserName(user.getName());
        prefManager.setUserEmail(user.getEmail());
        prefManager.setUserRole(user.getRole());
        prefManager.setLoggedIn(true);

        if (user.getFamilyId() != null) {
            prefManager.setFamilyId(user.getFamilyId());
        }
    }

    private void navigateToNextScreen(User user) {
        Intent intent;
        if (user.getFamilyId() != null) {
            // User has a family, go to dashboard
            intent = new Intent(this, ParentDashboardActivity.class);
        } else {
            // User needs to create/join a family
            intent = new Intent(this, AccountSelectionActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnContinueWithGoogle.setEnabled(!show);
        btnJoinFamilyCode.setEnabled(!show);
    }
}