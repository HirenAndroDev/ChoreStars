package com.chores.app.kids.chores_app_for_kids.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.kid.KidLoginActivity;
import com.chores.app.kids.chores_app_for_kids.activities.parent.ParentLoginActivity;

public class LandingActivity extends AppCompatActivity {
    private Button btnParent, btnKid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnParent = findViewById(R.id.btn_parent);
        btnKid = findViewById(R.id.btn_kid);
    }

    private void setupClickListeners() {
        btnParent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentLoginActivity.class);
            startActivity(intent);
        });

        btnKid.setOnClickListener(v -> {
            Intent intent = new Intent(this, KidLoginActivity.class);
            startActivity(intent);
        });
    }
}


