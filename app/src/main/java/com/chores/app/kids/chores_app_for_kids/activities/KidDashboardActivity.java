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

public class KidDashboardActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvGreeting;
    private TextView tvChildName;
    private TextView tvStarBalance;
    private ImageView ivStarIcon;
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
        setupTextToSpeech();
        loadUserData();

        // Show default fragment (Tasks)
        if (savedInstanceState == null) {
            loadFragment(new KidTasksFragment());
        }

        // Welcome the child
        showWelcomeMessage();
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        tvChildName = findViewById(R.id.tv_child_name);
        tvStarBalance = findViewById(R.id.tv_star_balance);
        ivStarIcon = findViewById(R.id.iv_star_icon);
        bottomNavigation = findViewById(R.id.bottom_navigation_kid);
        fragmentManager = getSupportFragmentManager();

        // Set child name
        if (childName != null) {
            tvChildName.setText(childName);
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

        tvGreeting.setText(greeting + "!");
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String fragmentTag = "";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_kid_tasks) {
                selectedFragment = new KidTasksFragment();
                fragmentTag = "tasks";
                announceIfEnabled("Tasks page");
            } else if (itemId == R.id.nav_kid_rewards) {
                selectedFragment = new KidRewardsFragment();
                fragmentTag = "rewards";
                announceIfEnabled("Rewards page");
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                SoundHelper.playClickSound(this);
                return true;
            }
            return false;
        });
    }

    private void setupTextToSpeech() {
        ttsHelper = new TextToSpeechHelper(this, this);
    }

    private void loadUserData() {
        // Load star balance
        FirebaseHelper.getUserStarBalance(balance -> {
            runOnUiThread(() -> {
                updateStarBalance(balance);
            });
        });

        // Load user preferences
        FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                isTextToSpeechEnabled = user.isTextToSpeechEnabled();
                if (childName == null || childName.isEmpty()) {
                    childName = user.getName();
                    tvChildName.setText(childName);
                }
            }

            @Override
            public void onError(String error) {
                // Handle error silently for kid interface
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_kid, fragment);
        transaction.commit();
    }

    public void updateStarBalance(int newBalance) {
        int previousBalance = currentStarBalance;
        currentStarBalance = newBalance;

        tvStarBalance.setText(String.valueOf(newBalance));

        // Animate star icon if balance increased
        if (newBalance > previousBalance) {
            animateStarIncrease(newBalance - previousBalance);
        }
    }

    private void animateStarIncrease(int starsEarned) {
        // Scale animation for star icon
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivStarIcon, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivStarIcon, "scaleY", 1f, 1.5f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.start();
        scaleY.start();

        // Rotation animation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(ivStarIcon, "rotation", 0f, 360f);
        rotation.setDuration(800);
        rotation.start();

        // Play sound
        SoundHelper.playStarEarnedSound(this);

        // Announce star gain
        announceIfEnabled(String.format("You earned %d stars! You now have %d stars total!",
                starsEarned, currentStarBalance));
    }

    private void showWelcomeMessage() {
        if (childName != null && !childName.isEmpty()) {
            String welcomeMessage = String.format("Welcome back, %s! Ready for some fun tasks?", childName);
            announceIfEnabled(welcomeMessage);
        }
    }

    public void announceTaskCompletion(String taskName, int starsEarned) {
        String message = String.format("Great job completing %s! You earned %d stars!", taskName, starsEarned);
        announceIfEnabled(message);
        SoundHelper.playCelebrationSound(this);
    }

    public void announceRewardRedemption(String rewardName, int starsSpent) {
        String message = String.format("Awesome! You redeemed %s for %d stars!", rewardName, starsSpent);
        announceIfEnabled(message);
        SoundHelper.playRewardSound(this);
    }

    public void announceInsufficientStars(int needed) {
        String message = String.format("You need %d more stars for this reward. Keep completing tasks!", needed);
        announceIfEnabled(message);
        SoundHelper.playErrorSound(this);
    }

    public void announceIfEnabled(String message) {
        if (isTextToSpeechEnabled && ttsHelper != null) {
            ttsHelper.speak(message);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (ttsHelper != null) {
                ttsHelper.setLanguage(Locale.getDefault());
            }
        }
    }

    public void onTaskCompleted(Task task) {
        // Handle task completion from fragments
        announceTaskCompletion(task.getName(), task.getStarReward());

        // Update star balance
        int newBalance = currentStarBalance + task.getStarReward();
        updateStarBalance(newBalance);

        // Show celebration overlay
        showCelebrationOverlay(task);
    }

    public void onRewardRedeemed(Reward reward) {
        // Handle reward redemption from fragments
        announceRewardRedemption(reward.getName(), reward.getStarCost());

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
        String userId = AuthHelper.getCurrentUserId();
        if (userId != null) {
            FirebaseHelper.updateTextToSpeechSetting(userId, isTextToSpeechEnabled, task -> {
                // Handle result silently
            });
        }

        // Announce the change
        String message = isTextToSpeechEnabled ? "Text to speech turned on" : "Text to speech turned off";
        if (isTextToSpeechEnabled) {
            announceIfEnabled(message);
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