package com.chores.app.kids.chores_app_for_kids.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.chores.app.kids.chores_app_for_kids.fragments.DashboardFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.MainRewardFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.TaskManageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.fragments.TasksFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.RewardsFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.SettingsFragment;

public class ParentDashboardActivity extends AppCompatActivity {

    private static final String TAG = "ParentDashboardActivity";

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    // Broadcast receiver for task completion notifications
    private BroadcastReceiver taskCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.chores.app.TASK_COMPLETED".equals(intent.getAction())) {
                String childId = intent.getStringExtra("childId");
                String familyId = intent.getStringExtra("familyId");

                Log.d(TAG, "Received task completion broadcast for child: " + childId);

                // Refresh dashboard if it's currently displayed
                onTaskCompleted(childId);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        initializeViews();
        setupBottomNavigation();
        registerTaskCompletionReceiver();

        // Show default fragment (Tasks)
        if (savedInstanceState == null) {
            loadFragment(new TaskManageFragment());
        }
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasksManage) {
                selectedFragment = new TaskManageFragment();
            } else if (itemId == R.id.nav_tasks) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_rewards) {
                selectedFragment = new MainRewardFragment();
            }else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        // Store reference to current fragment
        currentFragment = fragment;

        Log.d(TAG, "Loaded fragment: " + fragment.getClass().getSimpleName());
    }

    /**
     * Register broadcast receiver for task completion notifications
     */
    private void registerTaskCompletionReceiver() {
        IntentFilter filter = new IntentFilter("com.chores.app.TASK_COMPLETED");
        registerReceiver(taskCompletionReceiver, filter);
        Log.d(TAG, "Registered task completion broadcast receiver");
    }

    /**
     * Unregister broadcast receiver
     */
    private void unregisterTaskCompletionReceiver() {
        try {
            unregisterReceiver(taskCompletionReceiver);
            Log.d(TAG, "Unregistered task completion broadcast receiver");
        } catch (IllegalArgumentException e) {
            // Receiver was not registered, ignore
        }
    }

    /**
     * Method to refresh dashboard when task is completed
     */
    public void refreshDashboard() {
        Log.d(TAG, "Dashboard refresh requested");

        // If current fragment is DashboardFragment, refresh it
        if (currentFragment instanceof DashboardFragment) {
            ((DashboardFragment) currentFragment).refreshDashboardData();
            Log.d(TAG, "Dashboard fragment refreshed");
        }
    }

    /**
     * Method to notify dashboard of task completion for specific child
     */
    public void onTaskCompleted(String childId) {
        Log.d(TAG, "Task completed notification for child: " + childId);

        // If current fragment is DashboardFragment, notify it
        if (currentFragment instanceof DashboardFragment) {
            ((DashboardFragment) currentFragment).onTaskCompleted(childId);
            Log.d(TAG, "Dashboard fragment notified of task completion");
        }
    }

    /**
     * Method to get current fragment
     */
    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver to prevent memory leaks
        unregisterTaskCompletionReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receiver when activity is not visible
        unregisterTaskCompletionReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-register receiver when activity becomes visible
        registerTaskCompletionReceiver();
    }
}
