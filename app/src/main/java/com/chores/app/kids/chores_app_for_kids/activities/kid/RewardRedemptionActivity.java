package com.chores.app.kids.chores_app_for_kids.activities.kid;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.RewardAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.RewardRedemptionDialog;
import com.chores.app.kids.chores_app_for_kids.models.Kid;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.RewardRedemption;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class RewardRedemptionActivity extends AppCompatActivity implements RewardAdapter.OnRewardClickListener {
    private static final String TAG = "RewardRedemptionActivity";

    // Views
    private Toolbar toolbar;
    private TextView tvStarBalance;
    private RecyclerView recyclerViewRewards;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigation;
    private View layoutNoRewards;
    private TextView tvNoRewardsMessage;

    // Data
    private RewardAdapter rewardAdapter;
    private List<Reward> rewards;
    private Kid currentKid;

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_redemption);

        initViews();
        initManagers();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupBottomNavigation();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStarBalance = findViewById(R.id.tv_star_balance);
        recyclerViewRewards = findViewById(R.id.recycler_view_rewards);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        layoutNoRewards = findViewById(R.id.layout_no_rewards);
        tvNoRewardsMessage = findViewById(R.id.tv_no_rewards_message);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        rewards = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Rewards");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        rewardAdapter = new RewardAdapter(rewards, this, true); // true for kid view
        recyclerViewRewards.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewRewards.setAdapter(rewardAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_primary);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                finish(); // Go back to tasks
                return true;
            } else if (itemId == R.id.nav_rewards) {
                // Already on rewards
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to profile
                return true;
            }
            return false;
        });

        // Set initial selection
        bottomNavigation.setSelectedItemId(R.id.nav_rewards);
    }

    private void loadData() {
        showLoading(true);
        loadKidProfile();
    }

    private void loadKidProfile() {
        String kidId = prefManager.getKidId();
        if (kidId != null) {
            firebaseManager.getKid(kidId, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    currentKid = task.getResult().toObject(Kid.class);
                    if (currentKid != null) {
                        updateStarBalance();
                        loadAvailableRewards();
                    }
                } else {
                    showError("Failed to load profile data");
                    showLoading(false);
                }
            });
        }
    }

    private void updateStarBalance() {
        if (currentKid != null) {
            tvStarBalance.setText("⭐ " + currentKid.getStarBalance());
        }
    }

    private void loadAvailableRewards() {
        String kidId = prefManager.getKidId();
        if (kidId != null) {
            firebaseManager.getKidRewards(kidId, task -> {
                showLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Reward> availableRewards = task.getResult().toObjects(Reward.class);
                    updateRewardsList(availableRewards);
                } else {
                    showError("Failed to load rewards");
                }
            });
        }
    }

    private void updateRewardsList(List<Reward> availableRewards) {
        rewards.clear();
        rewards.addAll(availableRewards);
        rewardAdapter.notifyDataSetChanged();

        if (rewards.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        recyclerViewRewards.setVisibility(View.GONE);
        layoutNoRewards.setVisibility(View.VISIBLE);
        tvNoRewardsMessage.setText("No rewards available yet!\nKeep completing tasks to see rewards here.");
    }

    private void hideEmptyState() {
        recyclerViewRewards.setVisibility(View.VISIBLE);
        layoutNoRewards.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
    }