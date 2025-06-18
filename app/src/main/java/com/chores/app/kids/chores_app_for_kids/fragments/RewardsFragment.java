package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.NewRewardActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.RewardAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.fragments.MainRewardFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.RewardRedeemFragment;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class RewardsFragment extends Fragment {

    private static final String TAG = "RewardsFragment";

    private RecyclerView recyclerViewRewards;
    private FloatingActionButton fabAddReward;
    private CardView btnAddReward;
    private LinearLayout layoutEmptyState, layoutAddRewardButton;
    private TextView tvStarBalance;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewardList;
    private String familyId;
    private String childId;

    private MainRewardFragment parentFragment;
    private ChildProfile selectedChild; // Add this field to store selected child

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        // Get parent fragment reference
        setupParentFragmentReference();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called");

        // Get parent fragment reference through fragment manager since we're in ViewPager2
        setupParentFragmentReference();

        // Load rewards after view is created
        loadRewards();
        loadUserStarBalance();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load rewards when fragment becomes visible
        loadRewards();
        loadUserStarBalance();
    }

    private void initializeViews(View view) {
        recyclerViewRewards = view.findViewById(R.id.recycler_view_rewards);
        fabAddReward = view.findViewById(R.id.fab_add_reward);
        btnAddReward = view.findViewById(R.id.btn_add_reward);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutAddRewardButton = view.findViewById(R.id.layout_add_reward_button);
        tvStarBalance = view.findViewById(R.id.tv_star_balance);

        familyId = AuthHelper.getFamilyId(getContext());
        Log.d(TAG, "initializeViews - familyId from AuthHelper: " + familyId);
    }

    private void setupRecyclerView() {
        rewardList = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewardList, getContext());
        rewardAdapter.setOnRewardClickListener(new RewardAdapter.OnRewardClickListener() {
            @Override
            public void onRewardClick(Reward reward) {
                // Handle reward item click (maybe show details)
                Log.d(TAG, "Reward clicked: " + reward.getName());
            }

            @Override
            public void onRedeemClick(Reward reward) {
                // Handle redeem button click
                Log.d(TAG, "Redeem clicked for reward: " + reward.getName() + " (Cost: " + reward.getStarCost() + " stars)");
                handleRedeemClick(reward);
            }
        });
        recyclerViewRewards.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRewards.setAdapter(rewardAdapter);
    }

    private void setupClickListeners() {
        btnAddReward.setOnClickListener(v -> openNewRewardActivity());
        fabAddReward.setOnClickListener(v -> openNewRewardActivity());
    }

    private void openNewRewardActivity() {
        Intent intent = new Intent(getActivity(), NewRewardActivity.class);
        startActivity(intent);
    }

    // ADD THIS METHOD - This is what's missing and causing the compilation error
    public void setSelectedChild(ChildProfile childProfile) {
        Log.d(TAG, "setSelectedChild called with: " + (childProfile != null ? childProfile.getName() : "null"));
        this.selectedChild = childProfile;
        if (childProfile != null) {
            this.childId = childProfile.getChildId();
        }
    }

    private ChildProfile getSelectedChild() {
        // First check if we have a locally stored selected child
        if (selectedChild != null) {
            return selectedChild;
        }

        // Otherwise get from parent fragment
        if (parentFragment != null) {
            ChildProfile selectedKid = parentFragment.getSelectedKid();
            if (selectedKid != null) {
                if (!selectedKid.getChildId().equals(childId)) {
                    childId = selectedKid.getChildId();
                }
                selectedChild = selectedKid; // Cache it locally
                return selectedKid;
            }
        }
        return null;
    }

    private void loadRewards() {
        ChildProfile selectedChild = getSelectedChild();

        Log.d(TAG, "loadRewards() called - selectedChild: " +
                (selectedChild != null ? selectedChild.getName() : "null"));

        if (selectedChild == null) {
            // No child selected, show empty state with message
            updateEmptyState(true, "Please select a child to view rewards");
            return;
        }

        if (familyId == null || familyId.isEmpty()) {
            familyId = selectedChild.getFamilyId();
        }

        Log.d(TAG, "Using familyId: " + familyId);

        if (familyId == null || familyId.isEmpty()) {
            // Try to get familyId from current user
            FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(com.chores.app.kids.chores_app_for_kids.models.User user) {
                    if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                        familyId = user.getFamilyId();
                        Log.d(TAG, "Got familyId from current user: " + familyId);
                        loadRewardsForSelectedChild();
                    } else {
                        Log.e(TAG, "No family found for current user");
                        updateEmptyState(true, "No family found");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading current user: " + error);
                    updateEmptyState(true, "Error loading family data");
                }
            });
            return;
        }

        loadRewardsForSelectedChild();
    }

    private void loadRewardsForSelectedChild() {
        ChildProfile selectedChild = getSelectedChild();
        if (selectedChild == null) {
            Log.e(TAG, "loadRewardsForSelectedChild: selectedChild is null");
            updateEmptyState(true, "Please select a child to view rewards");
            return;
        }

        Log.d(TAG, "Loading rewards for familyId: " + familyId +
                ", child: " + selectedChild.getName());

        // Load all rewards for the family
        FirebaseHelper.getFamilyRewards(familyId, new FirebaseHelper.RewardsCallback() {
            @Override
            public void onRewardsLoaded(List<Reward> rewards) {
                Log.d(TAG, "Rewards loaded successfully, count: " + rewards.size());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        rewardList.clear();
                        rewardList.addAll(rewards);
                        rewardAdapter.notifyDataSetChanged();
                        updateEmptyState(rewards.isEmpty(), "No rewards available for " + selectedChild.getName());

                        // Log each reward for debugging
                        for (Reward reward : rewards) {
                            Log.d(TAG, "Reward: " + reward.getName() +
                                    ", Stars: " + reward.getStarCost());
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading rewards: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateEmptyState(true, "Error loading rewards: " + error);
                    });
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        updateEmptyState(isEmpty, "No rewards available");
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutAddRewardButton.setVisibility(View.VISIBLE);
            recyclerViewRewards.setVisibility(View.GONE);
            fabAddReward.setVisibility(View.GONE);

            // Update empty state message if there's a TextView for it
            TextView emptyMessage = layoutEmptyState.findViewById(R.id.tv_empty_message);
            if (emptyMessage != null) {
                emptyMessage.setText(message);
            }
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            layoutAddRewardButton.setVisibility(View.GONE);
            recyclerViewRewards.setVisibility(View.VISIBLE);
            fabAddReward.setVisibility(View.VISIBLE);
        }
    }

    private void handleRedeemClick(Reward reward) {
        ChildProfile selectedChild = getSelectedChild();

        if (selectedChild == null) {
            Toast.makeText(getContext(), "Please select a child first", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedChildId = selectedChild.getChildId();
        String selectedChildName = selectedChild.getName();
        int selectedChildStarBalance = selectedChild.getStarBalance();

        Log.d(TAG, "handleRedeemClick - Child: " + selectedChildName +
                ", Current Stars: " + selectedChildStarBalance +
                ", Reward Cost: " + reward.getStarCost());

        // Check if selected child has enough stars
        if (selectedChildStarBalance >= reward.getStarCost()) {
            // Show confirmation dialog before redemption
            showRedemptionConfirmationDialog(reward, selectedChild);
        } else {
            int starsNeeded = reward.getStarCost() - selectedChildStarBalance;
            Toast.makeText(getContext(), selectedChildName + " needs " + starsNeeded +
                    " more stars for this reward!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Insufficient stars - Need " + starsNeeded + " more stars");
        }
    }

    private void showRedemptionConfirmationDialog(Reward reward, ChildProfile selectedChild) {
        if (getContext() == null) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Redeem Reward");
        builder.setMessage("Are you sure you want to redeem '" + reward.getName() +
                "' for " + reward.getStarCost() + " stars?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            processRewardRedemption(reward, selectedChild);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void processRewardRedemption(Reward reward, ChildProfile selectedChild) {
        String selectedChildId = selectedChild.getChildId();
        String selectedChildName = selectedChild.getName();

        Log.d(TAG, "Processing reward redemption for " + selectedChildName +
                " - Reward: " + reward.getName() + " (" + reward.getStarCost() + " stars)");

        // Show loading state (optional)
        if (getContext() != null) {
            Toast.makeText(getContext(), "Redeeming reward...", Toast.LENGTH_SHORT).show();
        }

        // Child has enough stars, proceed with redemption
        FirebaseHelper.redeemRewardWithSelectedChild(reward.getRewardId(), selectedChildId, task -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Reward redeemed successfully for " + selectedChildName);

                        // Calculate new star balance
                        int newStarBalance = selectedChild.getStarBalance() - reward.getStarCost();
                        Log.d(TAG, "Star balance updated: " + selectedChild.getStarBalance() +
                                " -> " + newStarBalance);

                        // Update the selected child's star balance locally
                        selectedChild.setStarBalance(newStarBalance);
                        // Also update our local copy
                        if (this.selectedChild != null) {
                            this.selectedChild.setStarBalance(newStarBalance);
                        }

                        // Show success message
                        Toast.makeText(getContext(),
                                "🎉 " + reward.getName() + " redeemed successfully for " + selectedChildName + "! " +
                                        "Stars remaining: " + newStarBalance,
                                Toast.LENGTH_LONG).show();

                        // Update UI elements
                        updateStarBalanceDisplay(newStarBalance);

                        // INSTANT UPDATE: Update star balance in parent fragment immediately
                        updateParentFragmentStarBalanceInstantly(newStarBalance);

                        // Refresh rewards list to update availability based on new balance
                        loadRewards();

                        // Update star balance from server to ensure consistency (background operation)
                        refreshChildStarBalanceFromServer(selectedChildId);

                        // Refresh redeem history in the other tab
                        refreshRedeemHistoryTab();

                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to redeem reward";
                        Log.e(TAG, "Failed to redeem reward: " + errorMessage);
                        Toast.makeText(getContext(), "Failed to redeem reward: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateStarBalanceDisplay(int newBalance) {
        if (tvStarBalance != null) {
            tvStarBalance.setText(String.valueOf(newBalance));
            Log.d(TAG, "Updated star balance display to: " + newBalance);
        }
    }

    private void updateParentFragmentStarBalanceInstantly(int newStarBalance) {
        try {
            if (parentFragment != null) {
                parentFragment.setSelectedKidStarBalanceInstantly(newStarBalance);
                Log.d(TAG, "Instantly updated parent fragment's star balance to: " + newStarBalance);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating parent fragment star balance", e);
        }
    }

    private void refreshChildStarBalanceFromServer(String childId) {
        // Refresh the child's star balance from the server to ensure accuracy
        FirebaseHelper.getChildProfile(childId, new FirebaseHelper.ChildProfileCallback() {
            @Override
            public void onChildProfileLoaded(ChildProfile childProfile) {
                if (getActivity() != null && childProfile != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update our local selected child
                        selectedChild = childProfile;

                        // Update the selected child in parent fragment
                        if (parentFragment != null) {
                            parentFragment.updateSelectedChildStarBalance(childProfile.getStarBalance());
                        }

                        // Update local display
                        updateStarBalanceDisplay(childProfile.getStarBalance());

                        Log.d(TAG, "Refreshed star balance from server: " + childProfile.getStarBalance());
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Could not refresh star balance from server: " + error);
            }
        });
    }

    private void refreshRedeemHistoryTab() {
        // Try to refresh the redeem history tab
        try {
            if (parentFragment != null && parentFragment.getView() != null) {
                androidx.viewpager2.widget.ViewPager2 viewPager = parentFragment.getView().findViewById(R.id.viewPager);
                if (viewPager != null) {
                    androidx.fragment.app.FragmentActivity activity = parentFragment.requireActivity();
                    androidx.fragment.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();

                    // Find the RewardRedeemFragment
                    String fragmentTag = "f" + 1; // ViewPager2 uses "f" + position as tag
                    Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
                    if (fragment instanceof RewardRedeemFragment) {
                        ((RewardRedeemFragment) fragment).refreshRedeemedRewards();
                        Log.d(TAG, "Refreshed redeem history tab");
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not refresh redeem history tab", e);
        }
    }

    private void loadUserStarBalance() {
        // Load star balance for the selected child
        ChildProfile selectedChild = getSelectedChild();
        if (selectedChild != null && tvStarBalance != null) {
            int starBalance = selectedChild.getStarBalance();
            tvStarBalance.setText(String.valueOf(starBalance));
            Log.d(TAG, "Loaded star balance for " + selectedChild.getName() + ": " + starBalance);
        }
    }

    // Public method to refresh rewards when child selection changes
    public void onChildSelectionChanged() {
        Log.d(TAG, "Child selection changed");
        if (getView() != null && isAdded()) {
            loadRewards();
            loadUserStarBalance();
        } else {
            // If view is not ready, schedule for later
            if (getView() != null) {
                getView().post(() -> {
                    if (isAdded()) {
                        loadRewards();
                        loadUserStarBalance();
                    }
                });
            }
        }
    }

    // Public method to update star balance when it changes
    public void updateStarBalance(int newBalance) {
        Log.d(TAG, "updateStarBalance called with: " + newBalance);
        updateStarBalanceDisplay(newBalance);
        // Also update our local selected child if we have one
        if (selectedChild != null) {
            selectedChild.setStarBalance(newBalance);
        }
    }

    private void setupParentFragmentReference() {
        try {
            // Since we're inside ViewPager2, getParentFragment() returns null
            // We need to find the MainRewardFragment through the fragment manager
            Fragment fragment = this;
            while (fragment != null) {
                if (fragment instanceof MainRewardFragment) {
                    parentFragment = (MainRewardFragment) fragment;
                    Log.d(TAG, "Found MainRewardFragment as ancestor");
                    return;
                }
                fragment = fragment.getParentFragment();
            }

            // Alternative approach: look through fragment manager
            if (parentFragment == null && getActivity() != null) {
                List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
                for (Fragment f : fragments) {
                    if (f instanceof MainRewardFragment) {
                        parentFragment = (MainRewardFragment) f;
                        Log.d(TAG, "Found MainRewardFragment in fragment manager");
                        return;
                    }
                }
            }

            Log.w(TAG, "MainRewardFragment not found");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up parent fragment reference", e);
        }
    }

    // Method for MainRewardFragment to set itself as parent
    public void setParentFragment(MainRewardFragment parent) {
        this.parentFragment = parent;
        Log.d(TAG, "Parent fragment set directly by MainRewardFragment");
    }
}
