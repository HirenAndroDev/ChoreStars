package com.chores.app.kids.chores_app_for_kids.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.ChildInviteAdapter;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ManageChildrenActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChildren;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddChild;

    private ChildInviteAdapter childAdapter;
    private List<ChildProfile> childProfiles;
    private String familyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_children);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initializeViews() {
        recyclerViewChildren = findViewById(R.id.recycler_view_children);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        fabAddChild = findViewById(R.id.fab_add_child);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Children");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        childProfiles = new ArrayList<>();
        childAdapter = new ChildInviteAdapter(childProfiles, this, new ChildInviteAdapter.OnChildActionListener() {
            @Override
            public void onGenerateInviteCode(ChildProfile child) {
                generateInviteCodeForChild(child);
            }

            @Override
            public void onShareInviteCode(ChildProfile child) {
                shareInviteCode(child);
            }

            @Override
            public void onDeleteChild(ChildProfile child) {
                deleteChild(child);
            }
        });

        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChildren.setAdapter(childAdapter);
    }

    private void setupClickListeners() {
        fabAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddKidProfileActivity.class);
            intent.putExtra("familyId", familyId);
            startActivity(intent);
        });
    }

    private void loadData() {
        familyId = AuthHelper.getFamilyId(this);

        if (familyId != null && !familyId.isEmpty()) {
            loadChildProfiles();
        }
    }

    private void loadChildProfiles() {
        FirebaseHelper.getChildProfilesWithInviteCodes(familyId, new FirebaseHelper.ChildProfilesCallback() {
            @Override
            public void onProfilesLoaded(List<ChildProfile> profiles) {
                runOnUiThread(() -> {
                    childProfiles.clear();
                    childProfiles.addAll(profiles);
                    childAdapter.notifyDataSetChanged();

                    if (profiles.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showEmptyState();
                });
            }
        });
    }

    private void generateInviteCodeForChild(ChildProfile child) {
        FirebaseHelper.generateChildInviteCode(child.getChildId(), task -> {
            if (task.isSuccessful()) {
                loadChildProfiles(); // Refresh the list
            }
        });
    }

    private void shareInviteCode(ChildProfile child) {
        if (child.getInviteCode() != null && !child.getInviteCode().isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join Our Family - " + child.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    String.format("Hi! Use this code to join as %s in our family app: %s",
                            child.getName(), child.getInviteCode()));
            startActivity(Intent.createChooser(shareIntent, "Share Invite Code"));
        }
    }

    private void deleteChild(ChildProfile child) {
        // Implementation for deleting child profile
        FirebaseHelper.deleteChildProfile(child.getChildId(), task -> {
            if (task.isSuccessful()) {
                loadChildProfiles(); // Refresh the list
            }
        });
    }

    private void showEmptyState() {
        recyclerViewChildren.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        recyclerViewChildren.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildProfiles(); // Refresh when returning to activity
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}