package com.chores.app.kids.chores_app_for_kids.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.chores.app.kids.chores_app_for_kids.ChoresApplication;
import com.chores.app.kids.chores_app_for_kids.data.firebase.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.PrefsManager;

/**
 * Base Fragment class that provides common functionality for all fragments
 * Implements MVP pattern with BaseView
 */
public abstract class BaseFragment extends Fragment implements BaseView {

    private ProgressDialog progressDialog;
    protected PrefsManager prefsManager;
    protected FirebaseManager firebaseManager;
    protected FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize managers
        initializeManagers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Initialize presenter
        initPresenter();
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
     * @param view Root view
     */
    protected abstract void initViews(View view);

    /**
     * Initialize presenter
     */
    protected abstract void initPresenter();

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

    @Override
    public void showLoading() {
        showLoading("Loading...");
    }

    @Override
    public void showLoading(String message) {
        if (getActivity() == null) return;

        hideLoading();
        progressDialog = new ProgressDialog(getActivity());
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
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show long toast message
     * @param message Message to show
     */
    protected void showLongToast(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show snackbar
     * @param message Message to show
     */
    protected void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Show snackbar with action
     * @param message Message to show
     * @param actionText Action button text
     * @param action Action to perform
     */
    protected void showSnackbarWithAction(String message, String actionText, View.OnClickListener action) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setAction(actionText, action)
                    .show();
        }
    }

    /**
     * Hide keyboard
     */
    protected void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    /**
     * Show keyboard
     * @param view View to show keyboard for
     */
    protected void showKeyboard(View view) {
        if (view != null && getActivity() != null) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public boolean isNetworkAvailable() {
        if (getActivity() != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
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
    public void onPause() {
        super.onPause();
        // Hide keyboard if visible
        hideKeyboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hide loading if showing
        hideLoading();
    }

    /**
     * Check if fragment is still added to activity
     * @return true if added, false otherwise
     */
    protected boolean isAdded() {
        return isAdded() && getActivity() != null;
    }
}