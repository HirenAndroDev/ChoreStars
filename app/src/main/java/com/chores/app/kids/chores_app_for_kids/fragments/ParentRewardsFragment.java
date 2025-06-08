package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.parent.CreateRewardActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.RewardAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.ConfirmationDialog;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class ParentRewardsFragment extends Fragment implements RewardAdapter.OnRewardClickListener {
    private static final String TAG = "ParentRewardsFragment";

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewRewards;
    private TextView tvEmptyState;
    private View layoutEmptyState;
    private ChipGroup chipGroupCategory;
    private Chip chipAll, chipTreats, chipToys, chipActivities, chipMoney, chipScreenTime;
    private MaterialButton btnCreateFirstReward;

    // Summary Cards
    private TextView tvTotalRewards, tvRedeemedWeek, tvPopularReward;

    // Data
    private RewardAdapter rewardAdapter;
    private List<Reward> allRewards;
    private List<Reward> filteredRewards;
    private String currentCategory = "all";

    // Managers
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;

    public ParentRewardsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_rewards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initManagers();
        setupRecyclerView();
        setupSwipeRefresh();
        setupCategoryChips();
        setupEmptyState();
        loadRewards();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerViewRewards = view.findViewById(R.id.recycler_view_rewards);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        btnCreateFirstReward = view.findViewById(R.id.btn_create_first_reward);

        // Category chips
        chipAll = view.findViewById(R.id.chip_all);
        chipTreats = view.findViewById(R.id.chip_treats);
        chipToys = view.findViewById(R.id.chip_toys);
        chipActivities = view.findViewById(R.id.chip_activities);
        chipMoney = view.findViewById(R.id.chip_money);
        chipScreenTime = view.findViewById(R.id.chip_screen_time);

        // Summary cards
        tvTotalRewards = view.findViewById(R.id.tv_total_rewards);
        tvRedeemedWeek = view.findViewById(R.id.tv_redeemed_week);
        tvPopularReward = view.findViewById(R.id.tv_popular_reward);
    }

    private void initManagers() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(requireContext());
        allRewards = new ArrayList<>();
        filteredRewards = new ArrayList<>();
    }

    private void setupRecyclerView() {
        rewardAdapter = new RewardAdapter(filteredRewards, this, false); // false for parent view
        recyclerViewRewards.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerViewRewards.setAdapter(rewardAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRewards);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_primary);
    }

    private void setupCategoryChips() {
        chipAll.setOnClickListener(v -> filterByCategory("all"));
        chipTreats.setOnClickListener(v -> filterByCategory("treats"));
        chipToys.setOnClickListener(v -> filterByCategory("toys"));
        chipActivities.setOnClickListener(v -> filterByCategory("activities"));
        chipMoney.setOnClickListener(v -> filterByCategory("money"));
        chipScreenTime.setOnClickListener(v -> filterByCategory("screen_time"));

        // Set initial selection
        chipAll.setChecked(true);
    }

    private void setupEmptyState() {
        btnCreateFirstReward.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateRewardActivity.class);
            startActivity(intent);
        });
    }

    private void loadRewards() {
        showLoading(true);
        String familyId = prefManager.getFamilyId();

        if (familyId != null) {
            firebaseManager.getFamilyRewards(familyId, task -> {
                showLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Reward> rewards = task.getResult().toObjects(Reward.class);
                    updateRewardsList(rewards);
                } else {
                    showError("Failed to load rewards");
                }
            });
        } else {
            showLoading(false);
            showError("Family not found");
        }
    }

    private void updateRewardsList(List<Reward> rewards) {
        allRewards.clear();
        allRewards.addAll(rewards);
        updateSummaryCards();
        filterByCategory(currentCategory);
        updateEmptyState();
    }

    private void updateSummaryCards() {
        int totalRewards = allRewards.size();
        int redeemedThisWeek = 7; // This would be calculated from redemption records
        String popularReward = "Ice Cream"; // This would be calculated from redemption frequency

        tvTotalRewards.setText(String.valueOf(totalRewards));
        tvRedeemedWeek.setText(String.valueOf(redeemedThisWeek));
        tvPopularReward.setText(popularReward);
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        filteredRewards.clear();

        if ("all".equals(category)) {
            filteredRewards.addAll(allRewards);
            updateChipSelection(chipAll);
        } else {
            for (Reward reward : allRewards) {
                if (category.equals(reward.getCategory()) && reward.isActive()) {
                    filteredRewards.add(reward);
                }
            }
            updateChipSelection(getChipForCategory(category));
        }

        rewardAdapter.updateRewards(filteredRewards);
        updateEmptyState();
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(selectedChip == chipAll);
        chipTreats.setChecked(selectedChip == chipTreats);
        chipToys.setChecked(selectedChip == chipToys);
        chipActivities.setChecked(selectedChip == chipActivities);
        chipMoney.setChecked(selectedChip == chipMoney);
        chipScreenTime.setChecked(selectedChip == chipScreenTime);
    }

    private Chip getChipForCategory(String category) {
        switch (category) {
            case "treats": return chipTreats;
            case "toys": return chipToys;
            case "activities": return chipActivities;
            case "money": return chipMoney;
            case "screen_time": return chipScreenTime;
            default: return chipAll;
        }
    }

    private void updateEmptyState() {
        if (filteredRewards.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        recyclerViewRewards.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);

        String emptyMessage;
        String emptySubtitle;

        switch (currentCategory) {
            case "treats":
                emptyMessage = "No treats rewards yet";
                emptySubtitle = "Create sweet treats to motivate your kids!";
                break;
            case "toys":
                emptyMessage = "No toy rewards yet";
                emptySubtitle = "Add exciting toy rewards for big achievements!";
                break;
            case "activities":
                emptyMessage = "No activity rewards yet";
                emptySubtitle = "Create fun activity rewards like movie nights!";
                break;
            case "money":
                emptyMessage = "No money rewards yet";
                emptySubtitle = "Set up allowance and money-based rewards!";
                break;
            case "screen_time":
                emptyMessage = "No screen time rewards yet";
                emptySubtitle = "Create extra screen time rewards for digital fun!";
                break;
            default:
                emptyMessage = "No rewards created yet";
                emptySubtitle = "Create exciting rewards to motivate your kids!";
                break;
        }

        tvEmptyState.setText(emptyMessage);
        // You can also update the subtitle if you have that view
    }

    private void hideEmptyState() {
        recyclerViewRewards.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // RewardAdapter.OnRewardClickListener implementation
    @Override
    public void onRewardClick(Reward reward) {
        // Navigate to reward details or edit
        Intent intent = new Intent(requireContext(), CreateRewardActivity.class);
        intent.putExtra("reward_id", reward.getRewardId());
        intent.putExtra("edit_mode", true);
        startActivity(intent);
    }

    @Override
    public void onRewardRedeem(Reward reward) {
        // Not applicable for parent view
    }

    @Override
    public void onRewardEdit(Reward reward) {
        onRewardClick(reward); // Same as reward click
    }

    public void deleteReward(Reward reward) {
        ConfirmationDialog dialog = new ConfirmationDialog(
                requireContext(),
                "Delete Reward",
                "Are you sure you want to delete '" + reward.getName() + "'? This action cannot be undone.",
                "Delete",
                "Cancel",
                new ConfirmationDialog.OnConfirmationListener() {
                    @Override
                    public void onConfirm() {
                        performRewardDeletion(reward);
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing
                    }
                }
        );
        dialog.show();
    }

    private void performRewardDeletion(Reward reward) {
        // Mark reward as inactive instead of deleting
        reward.setActive(false);
        reward.setUpdatedAt(System.currentTimeMillis());

        firebaseManager.createReward(reward, deleteTask -> {
            if (deleteTask.isSuccessful()) {
                Toast.makeText(requireContext(), "Reward deleted successfully", Toast.LENGTH_SHORT).show();
                loadRewards(); // Refresh the list
            } else {
                Toast.makeText(requireContext(), "Failed to delete reward", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Public method to refresh data (called from parent activity)
    public void refreshData() {
        loadRewards();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRewards(); // Refresh when fragment becomes visible
    }
}