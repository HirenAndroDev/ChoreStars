package com.chores.app.kids.chores_app_for_kids.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.ParentLoginActivity;
import com.chores.app.kids.chores_app_for_kids.activities.KidLoginActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnParent;
    private Button btnKid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnParent = findViewById(R.id.btn_parent);
        btnKid = findViewById(R.id.btn_kid);
    }

    private void setupClickListeners() {
        btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ParentLoginActivity.class);
                startActivity(intent);
            }
        });

        btnKid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, KidLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}