package com.chores.app.kids.chores_app_for_kids.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.IconSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.KidSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.adapters.PrebuiltRewardAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;
import java.util.ArrayList;
import java.util.List;

public class CreateRewardActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerViewPrebuilt;
    private LinearLayout layoutCustomReward;
    private EditText etRewardName;
    private RecyclerView recyclerViewIcons;
    private TextView tvStarCost;
    private ImageView ivStarMinus, ivStarPlus;
    private RecyclerView recyclerViewKids;
    private RadioGroup rgRenewalPeriod;
    private Button btnCreateReward, btnCancel, btnToggleCustom;

    // Data
    private PrebuiltRewardAdapter prebuiltAdapter;
    private IconSelectionAdapter iconAdapter;
    private KidSelectionAdapter kidAdapter;
    private List<Reward> prebuiltRewards;
    private List<String> availableIcons;
    private List<User> familyKids;
    private String selectedIcon = "";
    private int starCost = 5;
    private List<String> selectedKids;
    private String familyId;
    private boolean isCustomMode = false;
    private Reward selectedPrebuiltReward = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reward);

        initializeData();
        initializeViews();
        setupPrebuiltRewards();
        setupIconSelection();
        setupKidSelection();
        setupStarCounter();
        setupClickListeners();
        loadFamilyKids();

        // Start with prebuilt rewards view
        showPrebuiltRewards();
    }

    private void initializeData() {
        familyId = AuthHelper.getFamilyId(this);
        selectedKids = new ArrayList<>();
        familyKids = new ArrayList<>();

        // Available reward icons
        availableIcons = new ArrayList<>();
        availableIcons.add("ic_ice_cream");
        availableIcons.add("ic_screen");
        availableIcons.add("ic_dinner");
        availableIcons.add("ic_bedtime");
        availableIcons.add("ic_movie");
        availableIcons.add("ic_park");
        availableIcons.add("ic_toy_new");
        availableIcons.add("ic_friend");
        availableIcons.add("ic_allowance");
        availableIcons.add("ic_treat");
        availableIcons.add("ic_activity");
        availableIcons.add("ic_privilege");

        // Prebuilt rewards
        prebuiltRewards = createPrebuiltRewards();
    }

    private void initializeViews() {
        recyclerViewPrebuilt = findViewById(R.id.recycler_view_prebuilt);
        layoutCustomReward = findViewById(R.id.layout_custom_reward);
        etRewardName = findViewById(R.id.et_reward_name);
        recyclerViewIcons = findViewById(R.id.recycler_view_icons);
        tvStarCost = findViewById(R.id.tv_star_cost);
        ivStarMinus = findViewById(R.id.iv_star_minus);
        ivStarPlus = findViewById(R.id.iv_star_plus);
        recyclerViewKids = findViewById(R.id.recycler_view_kids);
        rgRenewalPeriod = findViewById(R.id.rg_renewal_period);
        btnCreateReward = findViewById(R.id.btn_create_reward);
        btnCancel = findViewById(R.id.btn_cancel);
        btnToggleCustom = findViewById(R.id.btn_toggle_custom);

        // Set initial star cost
        tvStarCost.setText(String.valueOf(starCost));
    }

    private void setupPrebuiltRewards() {
        prebuiltAdapter = new PrebuiltRewardAdapter(prebuiltRewards, this, new PrebuiltRewardAdapter.OnRewardSelectedListener() {
            @Override
            public void onRewardSelected(Reward reward) {
                selectedPrebuiltReward = reward;
                starCost = reward.getStarCost();
                tvStarCost.setText(String.valueOf(starCost));
            }

            @Override
            public void onRewardDeselected() {

            }
        });

        recyclerViewPrebuilt.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewPrebuilt.setAdapter(prebuiltAdapter);
    }

    private void setupIconSelection() {
        iconAdapter = new IconSelectionAdapter(availableIcons, this, new IconSelectionAdapter.OnIconSelectedListener() {
            @Override
            public void onIconSelected(String iconName) {
                selectedIcon = iconName;
            }
        });

        recyclerViewIcons.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerViewIcons.setAdapter(iconAdapter);
    }

    private void setupKidSelection() {
        kidAdapter = new KidSelectionAdapter(familyKids, this, new KidSelectionAdapter.OnKidSelectionChangedListener() {
            @Override
            public void onKidSelectionChanged(List<String> selectedKidIds) {
                selectedKids = selectedKidIds;
            }
        });

        recyclerViewKids.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewKids.setAdapter(kidAdapter);
    }

    private void setupStarCounter() {
        ivStarMinus.setOnClickListener(v -> {
            if (starCost > 1) {
                starCost--;
                tvStarCost.setText(String.valueOf(starCost));
                // Update prebuilt reward if selected
                if (selectedPrebuiltReward != null) {
                    selectedPrebuiltReward.setStarCost(starCost);
                }
            }
        });

        ivStarPlus.setOnClickListener(v -> {
            if (starCost < 50) {
                starCost++;
                tvStarCost.setText(String.valueOf(starCost));
                // Update prebuilt reward if selected
                if (selectedPrebuiltReward != null) {
                    selectedPrebuiltReward.setStarCost(starCost);
                }
            }
        });
    }

    private void setupClickListeners() {
        btnCreateReward.setOnClickListener(v -> createReward());
        btnCancel.setOnClickListener(v -> finish());

        btnToggleCustom.setOnClickListener(v -> {
            isCustomMode = !isCustomMode;
            if (isCustomMode) {
                showCustomReward();
            } else {
                showPrebuiltRewards();
            }
        });
    }

    private void showPrebuiltRewards() {
        isCustomMode = false;
        recyclerViewPrebuilt.setVisibility(View.VISIBLE);
        layoutCustomReward.setVisibility(View.GONE);
        btnToggleCustom.setText("Create Custom");
        btnCreateReward.setText("Add Selected Reward");
    }

    private void showCustomReward() {
        isCustomMode = true;
        recyclerViewPrebuilt.setVisibility(View.GONE);
        layoutCustomReward.setVisibility(View.VISIBLE);
        btnToggleCustom.setText("Use Prebuilt");
        btnCreateReward.setText("Create Custom Reward");
        selectedPrebuiltReward = null;
    }

    private void loadFamilyKids() {
        FirebaseHelper.getFamilyChildren(familyId, new FirebaseHelper.FamilyChildrenCallback() {
            @Override
            public void onChildrenLoaded(List<User> children) {
                familyKids.clear();
                familyKids.addAll(children);
                kidAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CreateRewardActivity.this, "Failed to load family members: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReward() {
        if (!validateInput()) {
            return;
        }

        // Show loading state
        btnCreateReward.setEnabled(false);
        btnCreateReward.setText("Creating...");

        Reward reward;

        if (isCustomMode) {
            // Create custom reward
            reward = new Reward();
            reward.setName(etRewardName.getText().toString().trim());
            reward.setIconName(selectedIcon);
            reward.setStarCost(starCost);
            reward.setCustom(true);
        } else {
            // Use selected prebuilt reward
            reward = selectedPrebuiltReward;
            reward.setCustom(false);
        }

        // Set common properties
        reward.setAvailableForKids(selectedKids);
        reward.setFamilyId(familyId);
        reward.setRenewalPeriod(getSelectedRenewalPeriod());

        // Save to Firebase
        FirebaseHelper.addReward(reward, rewardResult -> {
            btnCreateReward.setEnabled(true);
            btnCreateReward.setText(isCustomMode ? "Create Custom Reward" : "Add Selected Reward");

            if (rewardResult.isSuccessful()) {
                Toast.makeText(CreateRewardActivity.this, "Reward created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMessage = rewardResult.getException() != null ?
                        rewardResult.getException().getMessage() : "Failed to create reward";
                Toast.makeText(CreateRewardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        if (isCustomMode) {
            String rewardName = etRewardName.getText().toString().trim();

            if (TextUtils.isEmpty(rewardName)) {
                etRewardName.setError("Reward name is required");
                etRewardName.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(selectedIcon)) {
                Toast.makeText(this, "Please select an icon for the reward", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (selectedPrebuiltReward == null) {
                Toast.makeText(this, "Please select a reward", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (selectedKids.isEmpty()) {
            Toast.makeText(this, "Please select at least one child for this reward", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getSelectedRenewalPeriod() {
        int selectedId = rgRenewalPeriod.getCheckedRadioButtonId();

        if (selectedId == R.id.rb_daily) {
            return "daily";
        } else if (selectedId == R.id.rb_weekly) {
            return "weekly";
        } else if (selectedId == R.id.rb_monthly) {
            return "monthly";
        } else if (selectedId == R.id.rb_once) {
            return "once";
        } else {
            return "weekly"; // default
        }
    }

    private List<Reward> createPrebuiltRewards() {
        List<Reward> rewards = new ArrayList<>();

        // Treats category
        rewards.add(new Reward("Ice Cream", "ic_ice_cream", 10, familyId));
        rewards.add(new Reward("Special Treat", "ic_treat", 8, familyId));
        rewards.add(new Reward("Favorite Snack", "ic_snack", 5, familyId));

        // Activities category
        rewards.add(new Reward("Extra Screen Time", "ic_screen", 15, familyId));
        rewards.add(new Reward("Choose Movie Night", "ic_movie", 20, familyId));
        rewards.add(new Reward("Park Visit", "ic_park", 25, familyId));
        rewards.add(new Reward("Friend Playdate", "ic_friend", 30, familyId));

        // Privileges category
        rewards.add(new Reward("Choose Dinner", "ic_dinner", 20, familyId));
        rewards.add(new Reward("Stay Up Late", "ic_bedtime", 25, familyId));
        rewards.add(new Reward("Skip Chore", "ic_skip", 15, familyId));

        // Special rewards
        rewards.add(new Reward("New Toy", "ic_toy_new", 50, familyId));
        rewards.add(new Reward("Extra Allowance", "ic_allowance", 40, familyId));

        return rewards;
    }
}