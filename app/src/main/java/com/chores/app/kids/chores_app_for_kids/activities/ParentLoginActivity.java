package com.chores.app.kids.chores_app_for_kids.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.chores.app.kids.chores_app_for_kids.R;

public class ParentLoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "ParentLoginActivity";

    private Button btnGoogleSignIn;
    private Button btnJoinFamily;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);

        // Check if user is already signed in
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            Log.d(TAG, "User already signed in, checking user data");
            // User is already signed in, navigate to dashboard
            navigateToDashboard();
            return;
        }

        initializeFirebase();
        initializeViews();
        setupClickListeners();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initializeViews() {
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        btnJoinFamily = findViewById(R.id.btn_join_family);
    }

    private void setupClickListeners() {
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        btnJoinFamily.setOnClickListener(v -> {
            // TODO: Implement join family with code functionality
            Toast.makeText(ParentLoginActivity.this, "Join Family feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In");

        // Show loading state
        btnGoogleSignIn.setEnabled(false);
        btnGoogleSignIn.setText("Signing in...");

        // Sign out any previous account to ensure fresh sign-in
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In successful: " + account.getEmail());

            // Authenticate with Firebase
            firebaseAuthWithGoogle(account.getIdToken());

        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed", e);
            resetButtonState();
            handleSignInError(e.getStatusCode());
        }
    }

    private void handleSignInError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 10: // DEVELOPER_ERROR
                errorMessage = "Configuration error. Please check:\n" +
                        "1. SHA-1 certificate in Firebase\n" +
                        "2. Package name matches\n" +
                        "3. google-services.json is updated";
                break;
            case 12500: // SIGN_IN_REQUIRED
                errorMessage = "Sign-in cancelled. Please try again.";
                break;
            case 7: // NETWORK_ERROR
                errorMessage = "Network error. Check internet connection.";
                break;
            case 8: // INTERNAL_ERROR
                errorMessage = "Internal error. Please try again.";
                break;
            default:
                errorMessage = "Sign-in failed (Error: " + statusCode + ")";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Authenticating with Firebase");

        if (idToken == null) {
            Log.e(TAG, "ID Token is null");
            resetButtonState();
            Toast.makeText(this, "Failed to get authentication token", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthHelper.firebaseAuthWithGoogle(idToken, this, task -> {
            resetButtonState();

            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase authentication and user creation successful");
                Toast.makeText(this, "Welcome! Setting up your account...", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            } else {
                String error = task.getException() != null ?
                        task.getException().getMessage() : "Authentication failed";
                Log.e(TAG, "Firebase auth failed: " + error);
                Toast.makeText(this, "Authentication failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard() {
        Log.d(TAG, "Navigating to dashboard");
        Intent intent = new Intent(this, ParentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void resetButtonState() {
        btnGoogleSignIn.setEnabled(true);
        btnGoogleSignIn.setText("Sign in with Google");
    }
}