package com.chores.app.kids.chores_app_for_kids.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.fragments.KidTasksFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.KidRewardsFragment;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import com.chores.app.kids.chores_app_for_kids.utils.TextToSpeechHelper;
import java.util.Calendar;
import java.util.Locale;

public class KidDashboardActivity extends AppCompatActivity  {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    private String childName;
    private int currentStarBalance = 0;
    private TextToSpeechHelper ttsHelper;
    private boolean isTextToSpeechEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_dashboard);

        // Get child name from intent
        childName = getIntent().getStringExtra("child_name");
        if (childName == null) {
            childName = AuthHelper.getUserName(this);
        }

        initializeViews();
        setupBottomNavigation();
        loadUserData();

        // Show default fragment (Tasks)
        if (savedInstanceState == null) {
            loadFragment(new KidTasksFragment());
        }

        // Welcome the child
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation_kid);
        fragmentManager = getSupportFragmentManager();

        // Set child name
        if (childName != null) {
        }

        // Set greeting based on time of day
        setGreetingMessage();
    }

    private void setGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String fragmentTag = "";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_kid_tasks) {
                selectedFragment = new KidTasksFragment();
                fragmentTag = "tasks";
            } else if (itemId == R.id.nav_kid_rewards) {
                selectedFragment = new KidRewardsFragment();
                fragmentTag = "rewards";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                SoundHelper.playClickSound(this);
                return true;
            }
            return false;
        });
    }


    private void loadUserData() {
        // Get the child ID from session
        String childId = AuthHelper.getChildId(this);

        if (childId != null && !childId.isEmpty()) {
            // Load star balance for this specific child
            FirebaseHelper.getUserStarBalanceById(childId, balance -> {
                runOnUiThread(() -> {
                    updateStarBalance(balance);
                });
            });

            // Load child user data
            FirebaseHelper.getUserById(childId, new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    runOnUiThread(() -> {
                        isTextToSpeechEnabled = user.isTextToSpeechEnabled();
                        if (childName == null || childName.isEmpty()) {
                            childName = user.getName();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    // Handle error silently for kid interface
                }
            });
        } else {
            // Fallback to generic method if no child ID
            FirebaseHelper.getUserStarBalance(balance -> {
                runOnUiThread(() -> {
                    updateStarBalance(balance);
                });
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_kid, fragment);
        transaction.commit();
    }

    public void updateStarBalance(int newBalance) {
        int previousBalance = currentStarBalance;
        currentStarBalance = newBalance;


        // Animate star icon if balance increased
        if (newBalance > previousBalance) {
        }
    }





    public void onTaskCompleted(Task task) {
        // Handle task completion from fragments

        // Update star balance
        int newBalance = currentStarBalance + task.getStarReward();
        updateStarBalance(newBalance);

        // Show celebration overlay
        showCelebrationOverlay(task);
    }

    public void onRewardRedeemed(Reward reward) {
        // Handle reward redemption from fragments

        // Update star balance
        int newBalance = currentStarBalance - reward.getStarCost();
        updateStarBalance(newBalance);
    }

    private void showCelebrationOverlay(Task task) {
        // Create and show celebration dialog/overlay
        View celebrationView = getLayoutInflater().inflate(R.layout.celebration_overlay, null);

        TextView tvCelebrationMessage = celebrationView.findViewById(R.id.tv_celebration_message);
        TextView tvStarsEarned = celebrationView.findViewById(R.id.tv_stars_earned);

        tvCelebrationMessage.setText(String.format("Great job completing\n%s!", task.getName()));
        tvStarsEarned.setText(String.format("+%d ⭐", task.getStarReward()));

        // Show with animation (you can implement as dialog or overlay)
        // For now, we'll use a simple approach
    }

    public void toggleTextToSpeech() {
        isTextToSpeechEnabled = !isTextToSpeechEnabled;

        // Update in Firebase
        String userId = AuthHelper.getCurrentUserId(this);
        if (userId != null) {
            FirebaseHelper.updateTextToSpeechSetting(userId, isTextToSpeechEnabled, task -> {
                // Handle result silently
            });
        }

        // Announce the change
        String message = isTextToSpeechEnabled ? "Text to speech turned on" : "Text to speech turned off";
        if (isTextToSpeechEnabled) {
        }
    }

    public boolean isTextToSpeechEnabled() {
        return isTextToSpeechEnabled;
    }

    public int getCurrentStarBalance() {
        return currentStarBalance;
    }

    public String getChildName() {
        return childName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadUserData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper != null) {
            ttsHelper.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent accidental exits for kids
        // Could show a "Are you sure?" dialog or require parent confirmation
        // For now, do nothing to keep kids in the app
        super.onBackPressed();
    }
}
