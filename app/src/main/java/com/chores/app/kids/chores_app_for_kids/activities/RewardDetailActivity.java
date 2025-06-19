package com.chores.app.kids.chores_app_for_kids.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.KidSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.IconSelectionDialog;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.TaskIcon;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;

import java.util.ArrayList;
import java.util.List;

public class RewardDetailActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack, ivRewardIcon;
    private TextView btnAdd, tvStarCount, tvRenewalPeriod;
    private EditText etRewardName;
    private CardView cardRewardIcon, cardEditIcon;
    private RecyclerView recyclerViewKids;
    private ImageView ivStarMinus, ivStarPlus;

    // Data
    private KidSelectionAdapter kidAdapter;
    private List<User> familyKids;
    private List<String> selectedKids;
    private String familyId;
    private String selectedIconUrl = "";
    private int starCost = 1;
    private String renewalPeriod = "None";

    // Pre-loaded data (when coming from pre-reward)
    private String preloadedName = "";
    private String preloadedIcon = "";
    private int preloadedStars = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        initializeData();
        initializeViews();
        setupKidSelection();
        setupStarCounter();
        setupClickListeners();
        loadFamilyKids();
        handlePreloadedData();
    }

    private void initializeData() {
        familyId = AuthHelper.getFamilyId(this);
        selectedKids = new ArrayList<>();
        familyKids = new ArrayList<>();

        // Get pre-loaded data from intent
        Intent intent = getIntent();
        preloadedName = intent.getStringExtra("rewardName");
        preloadedIcon = intent.getStringExtra("rewardIcon");
        preloadedStars = intent.getIntExtra("rewardStars", 0);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        btnAdd = findViewById(R.id.btn_add);
        ivRewardIcon = findViewById(R.id.iv_reward_icon);
        cardRewardIcon = findViewById(R.id.card_reward_icon);
        cardEditIcon = findViewById(R.id.card_edit_icon);
        etRewardName = findViewById(R.id.et_reward_name);
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        tvStarCount = findViewById(R.id.tv_star_count);
        ivStarMinus = findViewById(R.id.iv_star_minus);
        ivStarPlus = findViewById(R.id.iv_star_plus);
        tvRenewalPeriod = findViewById(R.id.tv_renewal_period);

        // Set initial values
        tvStarCount.setText(String.valueOf(starCost));
    }

    private void setupKidSelection() {
        kidAdapter = new KidSelectionAdapter(familyKids, this, new KidSelectionAdapter.OnKidSelectionChangedListener() {
            @Override
            public void onKidSelectionChanged(List<String> selectedKidIds) {
                selectedKids = selectedKidIds;
            }
        });

        recyclerViewKids.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewKids.setAdapter(kidAdapter);
    }

    private void setupStarCounter() {
        ivStarMinus.setOnClickListener(v -> {
            if (starCost > 1) {
                starCost--;
                tvStarCount.setText(String.valueOf(starCost));
            }
        });

        ivStarPlus.setOnClickListener(v -> {
            if (starCost < 100) {
                starCost++;
                tvStarCount.setText(String.valueOf(starCost));
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> createReward());
        cardEditIcon.setOnClickListener(v -> showIconSelectionDialog());
        tvRenewalPeriod.setOnClickListener(v -> showRenewalPeriodOptions());
    }

    private void handlePreloadedData() {
        if (!TextUtils.isEmpty(preloadedName)) {
            etRewardName.setText(preloadedName);
        }

        if (!TextUtils.isEmpty(preloadedIcon)) {
            selectedIconUrl = preloadedIcon;
            updateRewardIcon();
        }

        if (preloadedStars > 0) {
            starCost = preloadedStars;
            tvStarCount.setText(String.valueOf(starCost));
        }
    }

    private void showIconSelectionDialog() {
        IconSelectionDialog dialog = IconSelectionDialog.newInstance();
        dialog.setOnIconSelectedListener(new IconSelectionDialog.OnIconSelectedListener() {
            @Override
            public void onIconSelected(TaskIcon icon) {
                selectedIconUrl = icon.getIconUrl();
                if (icon.getIconUrl() != null && !icon.getIconUrl().isEmpty()) {
                    selectedIconUrl = icon.getIconUrl();
                }
                updateRewardIcon();
            }
        });
        dialog.show(getSupportFragmentManager(), "IconSelectionDialog");
    }

    private void updateRewardIcon() {
        if (!TextUtils.isEmpty(selectedIconUrl)) {
            // Check if it's a URL (contains http or https) or a drawable resource name
            if (selectedIconUrl.startsWith("http://") || selectedIconUrl.startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(this)
                        .load(selectedIconUrl)
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = getResources().getIdentifier(selectedIconUrl, "drawable", getPackageName());
                if (drawableResId != 0) {
                    ivRewardIcon.setImageResource(drawableResId);
                } else {
                    ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        } else {
            ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
        }
    }

    private void loadFamilyKids() {
        if (familyId == null || familyId.isEmpty()) {
            FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                        familyId = user.getFamilyId();
                        loadFamilyKidsWithId(familyId);
                    } else {
                        Toast.makeText(RewardDetailActivity.this, "No family found. Please check your account setup.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RewardDetailActivity.this, "Unable to load user data. Please try logging in again.", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        loadFamilyKidsWithId(familyId);
    }

    private void loadFamilyKidsWithId(String familyId) {
        FirebaseHelper.getFamilyChildren(familyId, new FirebaseHelper.FamilyChildrenCallback() {
            @Override
            public void onChildrenLoaded(List<User> children) {
                kidAdapter.updateKidList(children);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RewardDetailActivity.this, "Failed to load family members: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReward() {
        if (!validateInput()) {
            return;
        }

        // Show loading state
        btnAdd.setEnabled(false);
        btnAdd.setText("Adding...");

        // Create reward object
        Reward reward = new Reward();
        reward.setName(etRewardName.getText().toString().trim());
        reward.setIconName(selectedIconUrl);
        reward.setIconUrl(selectedIconUrl);
        reward.setStarCost(starCost);
        reward.setAvailableForKids(selectedKids);
        reward.setFamilyId(familyId);
        reward.setRenewalPeriod(renewalPeriod);
        reward.setCustom(true);

        // Save to Firebase
        FirebaseHelper.addReward(reward, rewardResult -> {
            btnAdd.setEnabled(true);
            btnAdd.setText("Add");

            if (rewardResult.isSuccessful()) {
                Toast.makeText(RewardDetailActivity.this, "Reward created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMessage = rewardResult.getException() != null ?
                        rewardResult.getException().getMessage() : "Failed to create reward";
                Toast.makeText(RewardDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        String rewardName = etRewardName.getText().toString().trim();

        if (TextUtils.isEmpty(rewardName)) {
            etRewardName.setError("Reward name is required");
            etRewardName.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidTaskName(rewardName)) {
            etRewardName.setError("Please enter a valid reward name");
            etRewardName.requestFocus();
            return false;
        }

        if (selectedKids.isEmpty()) {
            Toast.makeText(this, "Please select at least one child for this reward", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showRenewalPeriodOptions() {
        String[] options = {"None", "Daily", "Weekly", "Monthly"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Renewal Period")
                .setItems(options, (dialog, which) -> {
                    renewalPeriod = options[which];
                    tvRenewalPeriod.setText(renewalPeriod);

                    // Update description text based on selection
                    String description;
                    switch (renewalPeriod) {
                        case "Daily":
                            description = "Reward can be claimed once per day.";
                            break;
                        case "Weekly":
                            description = "Reward can be claimed once per week.";
                            break;
                        case "Monthly":
                            description = "Reward can be claimed once per month.";
                            break;
                        default:
                            description = "Non-renewable reward.";
                            break;
                    }
                })
                .show();
    }
}
