package com.chores.app.kids.chores_app_for_kids.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.chores.app.kids.chores_app_for_kids.R;

public class TestGoogleSignInActivity extends AppCompatActivity {

    private static final String TAG = "TestGoogleSignIn";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private Button btnSignIn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_google_sign_in);

        btnSignIn = findViewById(R.id.btn_test_signin);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Debug information
        debugConfiguration();

        // Configure Google Sign-in
        configureGoogleSignIn();

        btnSignIn.setOnClickListener(v -> signIn());
    }

    private void debugConfiguration() {
        try {
            String webClientId = getString(R.string.default_web_client_id);
            Log.d(TAG, "Web Client ID: " + webClientId);
            Log.d(TAG, "Package Name: " + getPackageName());
            Log.d(TAG, "Firebase Auth: " + (firebaseAuth != null));

            // Check if Google Play Services is available
            int resultCode = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(this);
            Log.d(TAG, "Google Play Services result: " + resultCode);

        } catch (Exception e) {
            Log.e(TAG, "Debug configuration error: " + e.getMessage());
        }
    }

    private void configureGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google Sign-in configured successfully");

        } catch (Exception e) {
            Log.e(TAG, "Google Sign-in configuration error: " + e.getMessage());
            Toast.makeText(this, "Configuration error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "Google Sign-in not configured", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            Log.d(TAG, "Starting Google Sign-in intent");

        } catch (Exception e) {
            Log.e(TAG, "Sign-in intent error: " + e.getMessage());
            Toast.makeText(this, "Sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign-in successful: " + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-in failed. Status code: " + e.getStatusCode());
                Log.e(TAG, "Error message: " + e.getMessage());

                String errorMessage = getGoogleSignInErrorMessage(e.getStatusCode());
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d(TAG, "Firebase Auth successful: " + user.getEmail());
                        Toast.makeText(this, "Success! Welcome " + user.getDisplayName(), Toast.LENGTH_LONG).show();

                    } else {
                        Log.e(TAG, "Firebase Auth failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Firebase Auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getGoogleSignInErrorMessage(int statusCode) {
        switch (statusCode) {
            case 10:
                return "DEVELOPER_ERROR (10): Check SHA-1 certificate and package name in Firebase Console";
            case 12500:
                return "SIGN_IN_CANCELLED (12500): User cancelled sign-in";
            case 12501:
                return "SIGN_IN_CURRENTLY_IN_PROGRESS (12501): Sign-in already in progress";
            case 12502:
                return "SIGN_IN_FAILED (12502): Generic sign-in failure";
            case 7:
                return "NETWORK_ERROR (7): Check internet connection";
            default:
                return "Google Sign-in failed with code: " + statusCode;
        }
    }
}