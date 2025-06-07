package com.chores.app.kids.chores_app_for_kids.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.chores.app.kids.chores_app_for_kids.ChoresApplication;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.data.firebase.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.ui.login.LoginActivity;
import com.chores.app.kids.chores_app_for_kids.utils.PrefsManager;

/**
 * Base Activity class that provides common functionality for all activities
 * Implements MVP pattern with BaseView
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    private ProgressDialog progressDialog;
    protected PrefsManager prefsManager;
    protected FirebaseManager firebaseManager;
    protected FirebaseAuth firebaseAuth;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize managers
        initializeManagers();

        // Check authentication if required
        if (requiresAuthentication() && !isUserAuthenticated()) {
            redirectToLogin();
            return;
        }

        // Set content view
        setContentView(getLayoutId());

        // Initialize views
        initViews();

        // Set up toolbar if exists
        setupToolbar();

        // Initialize presenter
        initPresenter();

        // Update online status
        updateOnlineStatus(true);
    }

    /**
     * Initialize managers from application class
     */
    private void initializeManagers() {
        ChoresApplication app = ChoresApplication.getInstance();
        prefsManager = app.getPrefsManager();
        firebaseManager = app.getFirebaseManager();
        firebaseAuth = app.getFirebaseAuth();
    }

    /**
     * Get layout resource ID
     * @return Layout resource ID
     */
    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * Initialize views
     */
    protected abstract void initViews();

    /**
     * Initialize presenter
     */
    protected abstract void initPresenter();

    /**
     * Check if activity requires authentication
     * Override this method to false for activities that don't require auth (like LoginActivity)
     * @return true if authentication is required, false otherwise
     */
    protected boolean requiresAuthentication() {
        return true;
    }

    /**
     * Set up toolbar
     */
    protected void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null && showBackButton()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }

    /**
     * Override to show back button in toolbar
     * @return true to show back button, false otherwise
     */
    protected boolean showBackButton() {
        return true;
    }

    /**
     * Set toolbar title
     * @param title Title to set
     */
    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Set toolbar subtitle
     * @param subtitle Subtitle to set
     */
    protected void setToolbarSubtitle(String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if user is authenticated
     * @return true if authenticated, false otherwise
     */
    protected boolean isUserAuthenticated() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null;
    }

    /**
     * Get current user
     * @return Current FirebaseUser or null
     */
    protected FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Get current user ID
     * @return User ID or null
     */
    protected String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Redirect to login screen
     */
    protected void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Sign out user
     */
    protected void signOut() {
        // Update offline status
        updateOnlineStatus(false);

        // Clear app data
        ChoresApplication.getInstance().clearAppData();

        // Sign out from Firebase
        firebaseAuth.signOut();

        // Redirect to login
        redirectToLogin();
    }

    @Override
    public void showLoading() {
        showLoading("Loading...");
    }

    /**
     * Show loading with custom message
     * @param message Loading message
     */
    public void showLoading(String message) {
        hideLoading();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void showMessage(String message) {
        showToast(message);
    }

    /**
     * Show toast message
     * @param message Message to show
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show long toast message
     * @param message Message to show
     */
    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show snackbar
     * @param message Message to show
     */
    protected void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Show snackbar with action
     * @param message Message to show
     * @param actionText Action button text
     * @param action Action to perform
     */
    protected void showSnackbarWithAction(String message, String actionText, View.OnClickListener action) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                    .setAction(actionText, action)
                    .show();
        }
    }

    /**
     * Hide keyboard
     */
    protected void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * Show keyboard
     * @param view View to show keyboard for
     */
    protected void showKeyboard(View view) {
        if (view != null) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * Check if network is available
     * @return true if network available, false otherwise
     */
    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Check network and show error if not available
     * @return true if network available, false otherwise
     */
    protected boolean checkNetworkAvailability() {
        if (!isNetworkAvailable()) {
            showError("No internet connection. Please check your network settings.");
            return false;
        }
        return true;
    }

    /**
     * Start activity with animation
     * @param intent Intent to start
     */
    protected void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Finish activity with animation
     */
    protected void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Update user online status
     * @param isOnline Online status
     */
    protected void updateOnlineStatus(boolean isOnline) {
        String userId = getCurrentUserId();
        if (userId != null) {
            firebaseManager.updateUserOnlineStatus(userId, isOnline);
        }
    }

    /**
     * Get user role from preferences
     * @return User role (parent/kid)
     */
    protected String getUserRole() {
        return prefsManager.getUserRole();
    }

    /**
     * Check if user is parent
     * @return true if parent, false otherwise
     */
    protected boolean isParent() {
        return "parent".equals(getUserRole());
    }

    /**
     * Check if user is kid
     * @return true if kid, false otherwise
     */
    protected boolean isKid() {
        return "kid".equals(getUserRole());
    }

    /**
     * Get family ID from preferences
     * @return Family ID or null
     */
    protected String getFamilyId() {
        return prefsManager.getFamilyId();
    }

    /**
     * Handle common errors
     * @param exception Exception to handle
     */
    protected void handleError(Exception exception) {
        hideLoading();
        if (exception != null) {
            String message = exception.getMessage();
            if (message != null && message.contains("network")) {
                showError("Network error. Please check your connection.");
            } else if (message != null && message.contains("permission")) {
                showError("Permission denied. Please check your access rights.");
            } else {
                showError("An error occurred: " + (message != null ? message : "Unknown error"));
            }
        } else {
            showError("An unknown error occurred");
        }
    }

    /**
     * Log debug message
     * @param tag Tag for log
     * @param message Message to log
     */
    protected void logDebug(String tag, String message) {
        if (com.chores.app.kids.chores_app_for_kids.BuildConfig.DEBUG) {
            android.util.Log.d(tag, message);
        }
    }

    /**
     * Log error message
     * @param tag Tag for log
     * @param message Message to log
     * @param throwable Throwable to log
     */
    protected void logError(String tag, String message, Throwable throwable) {
        android.util.Log.e(tag, message, throwable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update online status
        updateOnlineStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hide keyboard if visible
        hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hide loading if showing
        hideLoading();

        // Update offline status if finishing
        if (isFinishing()) {
            updateOnlineStatus(false);
        }
    }
}