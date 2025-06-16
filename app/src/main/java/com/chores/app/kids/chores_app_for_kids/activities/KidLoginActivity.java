package com.chores.app.kids.chores_app_for_kids.activities;


import androidx.appcompat.app.AppCompatActivity;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import java.util.List;

public class KidLoginActivity extends AppCompatActivity {

    private EditText etChildName;
    private EditText etInviteCode;
    private Button btnJoin;
    private LinearLayout layoutNameInput;
    private LinearLayout layoutCodeInput;
    private TextView tvWelcomeMessage;
    private ImageView ivWelcomeIcon;
    private LinearLayout layoutStars;

    private boolean isNameEntered = false;
    private String childName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_login);

        initializeViews();
        setupClickListeners();
        setupAnimations();
        showNameInputStep();
    }

    private void initializeViews() {
        etChildName = findViewById(R.id.et_child_name);
        etInviteCode = findViewById(R.id.et_invite_code);
        btnJoin = findViewById(R.id.btn_join);
        layoutNameInput = findViewById(R.id.layout_name_input);
        layoutCodeInput = findViewById(R.id.layout_code_input);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);
        ivWelcomeIcon = findViewById(R.id.iv_welcome_icon);
        layoutStars = findViewById(R.id.layout_stars);
    }

    private void setupClickListeners() {
        btnJoin.setOnClickListener(v -> {
            if (!isNameEntered) {
                handleNameInput();
            } else {
                handleInviteCodeInput();
            }
        });
    }

    private void setupAnimations() {
        // Animate welcome icon
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivWelcomeIcon, "scaleX", 0.8f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivWelcomeIcon, "scaleY", 0.8f, 1.2f, 1.0f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);

        AnimatorSet welcomeAnimation = new AnimatorSet();
        welcomeAnimation.playTogether(scaleX, scaleY);
        welcomeAnimation.start();

        // Animate stars
        animateStars();
    }

    private void animateStars() {
        for (int i = 0; i < layoutStars.getChildCount(); i++) {
            View star = layoutStars.getChildAt(i);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(star, "rotation", 0f, 360f);
            rotation.setDuration(3000 + (i * 500)); // Different speeds
            rotation.setRepeatCount(ObjectAnimator.INFINITE);
            rotation.setRepeatMode(ObjectAnimator.RESTART);
            rotation.start();

            // Delayed start for each star
            rotation.setStartDelay(i * 200);
        }
    }

    private void showNameInputStep() {
        isNameEntered = false;
        layoutNameInput.setVisibility(View.VISIBLE);
        layoutCodeInput.setVisibility(View.GONE);

        tvWelcomeMessage.setText("Hi there! What's your name?");
        btnJoin.setText("NEXT");

        // Focus on name input
        etChildName.requestFocus();
    }

    private void showCodeInputStep() {
        isNameEntered = true;
        layoutNameInput.setVisibility(View.GONE);
        layoutCodeInput.setVisibility(View.VISIBLE);

        tvWelcomeMessage.setText(String.format("Hi %s! Enter your family code", childName));
        btnJoin.setText("JOIN FAMILY");

        // Animate transition
        layoutCodeInput.setAlpha(0f);
        layoutCodeInput.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        // Focus on code input
        etInviteCode.requestFocus();

        // Play success sound
        SoundHelper.playSuccessSound(this);
    }

    private void handleNameInput() {
        String name = etChildName.getText().toString().trim();

        // Validate name
        String nameError = ValidationHelper.getChildNameError(name);
        if (nameError != null) {
            etChildName.setError(nameError);
            etChildName.requestFocus();
            SoundHelper.playErrorSound(this);
            shakeView(etChildName);
            return;
        }

        // Store name and proceed to code input
        childName = name;
        showCodeInputStep();
    }

    private void handleInviteCodeInput() {
        String inviteCode = etInviteCode.getText().toString().trim();

        // Validate invite code
        String codeError = ValidationHelper.getInviteCodeError(inviteCode);
        if (codeError != null) {
            etInviteCode.setError(codeError);
            etInviteCode.requestFocus();
            SoundHelper.playErrorSound(this);
            shakeView(etInviteCode);
            return;
        }

        // Show loading state
        btnJoin.setEnabled(false);
        btnJoin.setText("Joining...");

        // Attempt to join family with child-specific code
        AuthHelper.joinFamilyWithChildCode(inviteCode, this, task -> {
            btnJoin.setEnabled(true);
            btnJoin.setText("JOIN FAMILY");

            if (task.isSuccessful()) {
                // Play success sound and show celebration
                SoundHelper.playSuccessSound(this);
                showSuccessAnimation();

                // Navigate to kid dashboard after a short delay
                postDelayed(() -> {
                    Intent intent = new Intent(this, KidDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }, 2000);
            } else {
                // Show error message
                String errorMessage = getErrorMessage(task.getException());
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                SoundHelper.playErrorSound(this);
                shakeView(etInviteCode);
            }
        });
    }

    private void showSuccessAnimation() {
        // Create celebration animation
        TextView celebrationText = findViewById(R.id.tv_celebration);
        celebrationText.setVisibility(View.VISIBLE);
        celebrationText.setText(String.format("Welcome to the family, %s!", childName));

        // Scale up animation
        celebrationText.setScaleX(0f);
        celebrationText.setScaleY(0f);
        celebrationText.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .start();

        // Animate all stars
        for (int i = 0; i < layoutStars.getChildCount(); i++) {
            View star = layoutStars.getChildAt(i);
            star.animate()
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(600)
                    .setStartDelay(i * 100)
                    .start();
        }
    }

    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(600);
        shake.start();
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Invalid invite code. Please check with your parent.";
        }

        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("expired")) {
                return "This invite code has expired. Ask your parent for a new one!";
            } else if (message.contains("not found")) {
                return "Invite code not found. Double-check the numbers!";
            }
        }

        return "Something went wrong. Please try again!";
    }

    private void postDelayed(Runnable runnable, long delayMillis) {
        new android.os.Handler().postDelayed(runnable, delayMillis);
    }

    @Override
    public void onBackPressed() {
        if (isNameEntered) {
            // Go back to name input
            showNameInputStep();
        } else {
            // Exit to main activity
            super.onBackPressed();
        }
    }
}
