package com.chores.app.kids.chores_app_for_kids.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.kid.KidDashboardActivity;
import com.chores.app.kids.chores_app_for_kids.activities.parent.ParentDashboardActivity;
import com.chores.app.kids.chores_app_for_kids.utils.AuthManager;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserLoginStatus();
        }, SPLASH_DELAY);
    }

    private void checkUserLoginStatus() {
        AuthManager authManager = AuthManager.getInstance(this);
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);

        if (authManager.isUserLoggedIn() && prefManager.isLoggedIn()) {
            // User is logged in, navigate to appropriate dashboard
            String userRole = prefManager.getUserRole();

            if (Constants.ROLE_PARENT.equals(userRole)) {
                startActivity(new Intent(this, ParentDashboardActivity.class));
            } else if (Constants.ROLE_KID.equals(userRole)) {
                startActivity(new Intent(this, KidDashboardActivity.class));
            } else {
                // Role not set, go to landing
                startActivity(new Intent(this, LandingActivity.class));
            }
        } else {
            // User not logged in, go to landing
            startActivity(new Intent(this, LandingActivity.class));
        }

        finish();
    }
}
