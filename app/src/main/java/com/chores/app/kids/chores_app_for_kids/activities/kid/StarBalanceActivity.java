package com.chores.app.kids.chores_app_for_kids.activities.kid;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.StarHistoryAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Kid;
import com.chores.app.kids.chores_app_for_kids.models.StarTransaction;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StarBalanceActivity extends AppCompatActivity {
    private static final String TAG = "StarBalanceActivity";

    // Views
    private Toolbar toolbar;
    private CircleImageView ivProfileImage;
    private TextView tvKidName;
    private TextView tvStarBalance;
    private TextView tvTotalEarned;
    private TextView tvTotalSpent;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipEarned, chipSpent;
    private RecyclerView recyclerViewHistory;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigation;
    private View layoutNoHistory;

    // Data
    private StarHistoryAdapter historyAdapter;
    private List<StarTransaction> allTransactions;
    private List<StarTransaction> filteredTransactions;
    private String currentFilter = "all";
    private Kid currentKid;

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_balance);

        initViews();
        initManagers();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFilterChips();
        setupBottomNavigation();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        tvKidName = findViewById(R.id.tv_kid_name);
        tvStarBalance = findViewById(R.id.tv_star_balance);
        tvTotalEarned = findViewById(R.id.tv_total_earned);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        chipGroupFilter = findViewById(R.id.chip_group_filter);
        chipAll = findViewById(R.id.chip_all);
        chipEarned = findViewById(R.id.chip_earned);
        chipSpent = findViewById(R.id.chip_spent);
        recyclerViewHistory = findViewById(R.id.recycler_view_history);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        layoutNoHistory = findViewById(R.id.layout_no_history);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        allTransactions = new ArrayList<>();
        filteredTransactions = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Star Balance");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        historyAdapter = new StarHistoryAdapter(filteredTransactions);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        swipeRefreshLayout.setColorSchemeResources(R.color.star_gold);
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> filterTransactions("all"));
        chipEarned.setOnClickListener(v -> filterTransactions("earned"));
        chipSpent.setOnClickListener(v -> filterTransactions("spent"));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                finish(); // Go back to tasks
                return true;
            } else if (itemId == R.id.nav_rewards) {
                // Navigate to rewards
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Already on profile
                return true;
            }
            return false;
        });

        // Set initial selection
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
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
                        updateProfileUI();
                        loadStarHistory();
                    }
                } else {
                    showError("Failed to load profile data");
                    showLoading(false);
                }
            });
        }
    }

    private void updateProfileUI() {
        if (currentKid != null) {
            tvKidName.setText(currentKid.getName());
            tvStarBalance.setText(String.valueOf(currentKid.getStarBalance()));
            tvTotalEarned.setText(String.valueOf(currentKid.getTotalStarsEarned()));

            int totalSpent = currentKid.getTotalStarsEarned() - currentKid.getStarBalance();
            tvTotalSpent.setText(String.valueOf(Math.max(0, totalSpent)));

            // Load profile image if available
            if (currentKid.getProfileImage() != null && !currentKid.getProfileImage().isEmpty()) {
                // Load with Glide (you can implement this)
            }
        }
    }

    private void loadStarHistory() {
        String kidId = prefManager.getKidId();
        if (kidId != null) {
            firebaseManager.getStarHistory(kidId, task -> {
                showLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<StarTransaction> transactions = task.getResult().toObjects(StarTransaction.class);
                    updateTransactionsList(transactions);
                } else {
                    showError("Failed to load star history");
                }
            });
        }
    }

    private void updateTransactionsList(List<StarTransaction> transactions) {
        allTransactions.clear();
        allTransactions.addAll(transactions);
        filterTransactions(currentFilter);
    }

    private void filterTransactions(String filter) {
        currentFilter = filter;
        filteredTransactions.clear();

        switch (filter) {
            case "all":
                filteredTransactions.addAll(allTransactions);
                updateChipSelection(chipAll);
                break;
            case "earned":
                for (StarTransaction transaction : allTransactions) {
                    if (transaction.getAmount() > 0) {
                        filteredTransactions.add(transaction);
                    }
                }
                updateChipSelection(chipEarned);
                break;
            case "spent":
                for (StarTransaction transaction : allTransactions) {
                    if (transaction.getAmount() < 0) {
                        filteredTransactions.add(transaction);
                    }
                }
                updateChipSelection(chipSpent);
                break;
        }

        historyAdapter.updateTransactions(filteredTransactions);

        if (filteredTransactions.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(selectedChip == chipAll);
        chipEarned.setChecked(selectedChip == chipEarned);
        chipSpent.setChecked(selectedChip == chipSpent);
    }

    private void showEmptyState() {
        recyclerViewHistory.setVisibility(View.GONE);
        layoutNoHistory.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        recyclerViewHistory.setVisibility(View.VISIBLE);
        layoutNoHistory.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}