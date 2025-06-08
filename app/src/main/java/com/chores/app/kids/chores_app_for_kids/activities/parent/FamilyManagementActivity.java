package com.chores.app.kids.chores_app_for_kids.activities.parent;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.kid.KidProfileCreationActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.FamilyMemberAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.KidProfileAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.InviteCodeDialog;
import com.chores.app.kids.chores_app_for_kids.models.Family;
import com.chores.app.kids.chores_app_for_kids.models.Kid;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class FamilyManagementActivity extends AppCompatActivity implements
        FamilyMemberAdapter.OnMemberClickListener,
        KidProfileAdapter.OnKidClickListener {

    private static final String TAG = "FamilyManagementActivity";

    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Family Info Card
    private MaterialCardView cardFamilyInfo;
    private TextView tvFamilyName;
    private TextView tvInviteCode;
    private Button btnCopyCode;
    private Button btnShareLink;
    private Button btnRegenerateCode;

    // Adults Section
    private RecyclerView recyclerViewAdults;
    private TextView tvAdultsCount;
    private Button btnAddAdult;

    // Kids Section
    private RecyclerView recyclerViewKids;
    private TextView tvKidsCount;
    private Button btnAddKid;

    // Data
    private FamilyMemberAdapter adultsAdapter;
    private KidProfileAdapter kidsAdapter;
    private List<User> familyAdults;
    private List<Kid> familyKids;
    private Family currentFamily;

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_management);

        initViews();
        initManagers();
        setupToolbar();
        setupRecyclerViews();
        setupSwipeRefresh();
        setupClickListeners();
        loadFamilyData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Family Info Card
        cardFamilyInfo = findViewById(R.id.card_family_info);
        tvFamilyName = findViewById(R.id.tv_family_name);
        tvInviteCode = findViewById(R.id.tv_invite_code);
        btnCopyCode = findViewById(R.id.btn_copy_code);
        btnShareLink = findViewById(R.id.btn_share_link);
        btnRegenerateCode = findViewById(R.id.btn_regenerate_code);

        // Adults Section
        recyclerViewAdults = findViewById(R.id.recycler_view_adults);
        tvAdultsCount = findViewById(R.id.tv_adults_count);
        btnAddAdult = findViewById(R.id.btn_add_adult);

        // Kids Section
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        tvKidsCount = findViewById(R.id.tv_kids_count);
        btnAddKid = findViewById(R.id.btn_add_kid);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        familyAdults = new ArrayList<>();
        familyKids = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Family");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerViews() {
        // Adults RecyclerView
        adultsAdapter = new FamilyMemberAdapter(familyAdults, this);
        recyclerViewAdults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdults.setAdapter(adultsAdapter);
        recyclerViewAdults.setNestedScrollingEnabled(false);

        // Kids RecyclerView
        kidsAdapter = new KidProfileAdapter(familyKids, this);
        recyclerViewKids.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewKids.setAdapter(kidsAdapter);
        recyclerViewKids.setNestedScrollingEnabled(false);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadFamilyData);
        swipeRefreshLayout.setColorSchemeResources(R.color.green_primary);
    }

    private void setupClickListeners() {
        btnCopyCode.setOnClickListener(v -> copyInviteCode());
        btnShareLink.setOnClickListener(v -> shareInviteLink());
        btnRegenerateCode.setOnClickListener(v -> showRegenerateCodeDialog());
        btnAddAdult.setOnClickListener(v -> showAddAdultDialog());
        btnAddKid.setOnClickListener(v -> navigateToKidCreation());
    }

    private void loadFamilyData() {
        showLoading(true);
        String familyId = prefManager.getFamilyId();

        if (familyId == null) {
            // No family yet, create one
            createNewFamily();
        } else {
            loadExistingFamily(familyId);
        }
    }

    private void createNewFamily() {
        String userId = prefManager.getUserId();
        String userName = prefManager.getUserName();
        String familyId = firebaseManager.generateInviteCode() + "_" + System.currentTimeMillis();

        Family newFamily = new Family(familyId, userId, userName + "'s Family");
        newFamily.getMembers().add(userId);

        firebaseManager.createFamily(newFamily, task -> {
            if (task.isSuccessful()) {
                prefManager.setFamilyId(familyId);

                // Update user's family ID
                firebaseManager.updateUser(userId,
                        java.util.Collections.singletonMap("familyId", familyId),
                        updateTask -> {
                            showLoading(false);
                            if (updateTask.isSuccessful()) {
                                loadExistingFamily(familyId);
                            } else {
                                showError("Failed to update user family");
                            }
                        });
            } else {
                showLoading(false);
                showError("Failed to create family");
            }
        });
    }

    private void loadExistingFamily(String familyId) {
        firebaseManager.getFamily(familyId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                currentFamily = task.getResult().toObject(Family.class);
                if (currentFamily != null) {
                    updateFamilyInfoUI();
                    loadFamilyMembers();
                }
            } else {
                showLoading(false);
                showError("Failed to load family data");
            }
        });
    }

    private void updateFamilyInfoUI() {
        if (currentFamily != null) {
            tvFamilyName.setText(currentFamily.getFamilyName());
            tvInviteCode.setText(currentFamily.getInviteCode());
        }
    }

    private void loadFamilyMembers() {
        // Load adult members
        familyAdults.clear();
        for (String memberId : currentFamily.getMembers()) {
            firebaseManager.getUser(memberId, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User member = task.getResult().toObject(User.class);
                    if (member != null) {
                        familyAdults.add(member);
                        adultsAdapter.updateMembers(familyAdults);
                        tvAdultsCount.setText("(" + familyAdults.size() + ")");
                    }
                }
            });
        }

        // Load kids
        loadFamilyKids();
    }

    private void loadFamilyKids() {
        firebaseManager.getFamilyKids(currentFamily.getFamilyId(), task -> {
            showLoading(false);
            if (task.isSuccessful() && task.getResult() != null) {
                familyKids.clear();
                familyKids.addAll(task.getResult().toObjects(Kid.class));
                kidsAdapter.updateKids(familyKids);
                tvKidsCount.setText("(" + familyKids.size() + ")");
            }
        });
    }

    private void copyInviteCode() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Invite Code", currentFamily.getInviteCode());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Invite code copied!", Toast.LENGTH_SHORT).show();
    }

    private void shareInviteLink() {
        String shareText = "Join our family on ChoreStars!\n\n" +
                "Use invite code: " + currentFamily.getInviteCode() + "\n\n" +
                "Download the app and enter this code to join.";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Invite Code"));
    }

    private void showRegenerateCodeDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Regenerate Invite Code")
                .setMessage("Are you sure you want to regenerate the invite code? The old code will no longer work.")
                .setPositiveButton("Regenerate", (dialog, which) -> regenerateInviteCode())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void regenerateInviteCode() {
        showLoading(true);
        String newCode = firebaseManager.generateInviteCode();

        firebaseManager.updateFamily(currentFamily.getFamilyId(),
                java.util.Collections.singletonMap("inviteCode", newCode),
                task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        currentFamily.setInviteCode(newCode);
                        tvInviteCode.setText(newCode);
                        Toast.makeText(this, "Invite code regenerated!", Toast.LENGTH_SHORT).show();
                    } else {
                        showError("Failed to regenerate code");
                    }
                });
    }

    private void showAddAdultDialog() {
        InviteCodeDialog dialog = new InviteCodeDialog(this, inviteCode -> {
            Toast.makeText(this, "Share the invite code: " + currentFamily.getInviteCode(),
                    Toast.LENGTH_LONG).show();
        });
        dialog.show();
    }

    private void navigateToKidCreation() {
        Intent intent = new Intent(this, KidProfileCreationActivity.class);
        intent.putExtra(Constants.EXTRA_FAMILY_ID, currentFamily.getFamilyId());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMemberClick(User member) {
        // Show member details or options
    }

    @Override
    public void onKidClick(Kid kid) {
        // Show kid profile details
    }

    @Override
    public void onKidEdit(Kid kid) {
        // Edit kid profile
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFamilyData();
    }
}