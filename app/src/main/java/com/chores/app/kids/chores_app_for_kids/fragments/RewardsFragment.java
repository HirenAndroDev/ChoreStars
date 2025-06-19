package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private ProgressBar progressBar; // Added
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
        progressBar = view.findViewById(R.id.progress_bar_loading); // Fixed ID

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

    public void setSelectedChild(ChildProfile childProfile) {
        Log.d(TAG, "=== setSelectedChild called ===");
        Log.d(TAG, "Previous child: " + (this.selectedChild != null ? this.selectedChild.getName() : "null"));
        Log.d(TAG, "New child: " + (childProfile != null ? childProfile.getName() + " (ID: " + childProfile.getChildId() + ")" : "null"));

        // Immediately clear previous data to prevent showing old kid's rewards
        if (rewardList != null && rewardAdapter != null) {
            rewardList.clear();
            rewardAdapter.notifyDataSetChanged();
            Log.d(TAG, "Cleared previous rewards data immediately");
        }

        this.selectedChild = childProfile;
        if (childProfile != null) {
            this.childId = childProfile.getChildId();
            Log.d(TAG, "Updated childId to: " + this.childId);
        }

        // Immediately update UI if view is ready
        if (getView() != null && isAdded()) {
            Log.d(TAG, "View is ready, updating UI immediately for new child selection");
            showLoadingState(); // Show loading immediately
            loadRewards();
            loadUserStarBalance();
        } else {
            Log.d(TAG, "View not ready yet, rewards will be loaded when view is available");
        }

        Log.d(TAG, "=== setSelectedChild completed ===");
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
        Log.d(TAG, "loadRewards() called");

        // First try to get selected child
        ChildProfile selectedChild = getSelectedChild();
        Log.d(TAG, "Selected child from getSelectedChild(): " +
                (selectedChild != null ? selectedChild.getName() + " (ID: " + selectedChild.getChildId() + ")" : "null"));

        // If still null, try to get from parent fragment directly
        if (selectedChild == null && parentFragment != null) {
            selectedChild = parentFragment.getSelectedKid();
            Log.d(TAG, "Selected child from parent fragment: " +
                    (selectedChild != null ? selectedChild.getName() + " (ID: " + selectedChild.getChildId() + ")" : "null"));

            // Cache it locally if found
            if (selectedChild != null) {
                this.selectedChild = selectedChild;
                this.childId = selectedChild.getChildId();
            }
        }

        if (selectedChild == null) {
            Log.w(TAG, "No child selected, showing empty state");
            updateEmptyState(true, "Please select a child to view rewards");
            hideLoadingState(); // Added
            return;
        }

        // Ensure we have familyId
        if (familyId == null || familyId.isEmpty()) {
            familyId = selectedChild.getFamilyId();
            Log.d(TAG, "Updated familyId from selected child: " + familyId);
        }

        Log.d(TAG, "Using familyId: " + familyId + " for child: " + selectedChild.getName());

        if (familyId == null || familyId.isEmpty()) {
            Log.w(TAG, "FamilyId is null, trying to get from current user");
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
                        hideLoadingState(); // Added
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading current user: " + error);
                    updateEmptyState(true, "Error loading family data");
                    hideLoadingState(); // Added
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
            hideLoadingState(); // Added
            return;
        }

        Log.d(TAG, "Loading rewards for familyId: " + familyId +
                ", child: " + selectedChild.getName() + ", childId: " + selectedChild.getChildId());

        // Load only child-specific rewards (no fallback to all family rewards)
        FirebaseHelper.getRewardsForChild(selectedChild.getChildId(), familyId, new FirebaseHelper.RewardsCallback() {
            @Override
            public void onRewardsLoaded(List<Reward> childRewards) {
                Log.d(TAG, "Child-specific rewards found for " + selectedChild.getName() + ": " + childRewards.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        rewardList.clear();
                        rewardList.addAll(childRewards);
                        rewardAdapter.notifyDataSetChanged();

                        // Show empty state if no rewards found for this child
                        if (childRewards.isEmpty()) {
                            updateEmptyState(true, "No rewards assigned to " + selectedChild.getName());
                        } else {
                            updateEmptyState(false, null);
                        }

                        // Log each reward for debugging
                        for (Reward reward : childRewards) {
                            Log.d(TAG, "Reward for " + selectedChild.getName() + ": " + reward.getName() +
                                    ", Stars: " + reward.getStarCost());
                        }

                        hideLoadingState(); // Added
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading child-specific rewards for " + selectedChild.getName() + ": " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        rewardList.clear();
                        rewardAdapter.notifyDataSetChanged();
                        updateEmptyState(true, "Error loading rewards for " + selectedChild.getName() + ": " + error);
                        hideLoadingState(); // Added
                    });
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        updateEmptyState(isEmpty, "No rewards available");
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        // Hide loading state first
        hideLoadingState();

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
        Log.d(TAG, "=== onChildSelectionChanged called ===");
        Log.d(TAG, "Fragment isAdded: " + isAdded() + ", hasView: " + (getView() != null));
        Log.d(TAG, "Selected child: " + (selectedChild != null ? selectedChild.getName() + " (ID: " + selectedChild.getChildId() + ")" : "null"));
        Log.d(TAG, "Current familyId: " + familyId);

        // Immediately clear previous data to prevent showing old kid's rewards
        if (rewardList != null && rewardAdapter != null) {
            rewardList.clear();
            rewardAdapter.notifyDataSetChanged();
            Log.d(TAG, "Cleared previous rewards data immediately in onChildSelectionChanged");
        }

        if (getView() != null && isAdded()) {
            Log.d(TAG, "Conditions met, loading rewards and star balance");
            showLoadingState(); // Show loading immediately
            loadRewards();
            loadUserStarBalance();
        } else {
            // If view is not ready, schedule for later with multiple attempts
            Log.d(TAG, "View not ready, scheduling for later");
            if (getView() != null) {
                getView().post(() -> {
                    if (isAdded()) {
                        Log.d(TAG, "Delayed execution: loading rewards and star balance");
                        // Clear data again in case of delayed execution
                        if (rewardList != null && rewardAdapter != null) {
                            rewardList.clear();
                            rewardAdapter.notifyDataSetChanged();
                        }
                        showLoadingState();
                        loadRewards();
                        loadUserStarBalance();
                    } else {
                        Log.w(TAG, "Fragment not added during delayed execution");
                    }
                });
            } else {
                // Try again after a short delay
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded() && getView() != null) {
                        Log.d(TAG, "Handler delayed execution: loading rewards and star balance");
                        // Clear data again in case of delayed execution
                        if (rewardList != null && rewardAdapter != null) {
                            rewardList.clear();
                            rewardAdapter.notifyDataSetChanged();
                        }
                        showLoadingState();
                        loadRewards();
                        loadUserStarBalance();
                    } else {
                        Log.w(TAG, "Fragment still not ready after handler delay");
                    }
                }, 100);
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

        // If we have a view and the fragment is added, refresh immediately
        if (getView() != null && isAdded()) {
            Log.d(TAG, "Fragment ready, refreshing rewards after parent set");
            showLoadingState(); // Added
            loadRewards();
            loadUserStarBalance();
        }
    }

    // Public method to force refresh rewards - useful for external calls
    public void forceRefreshRewards() {
        Log.d(TAG, "forceRefreshRewards called");
        if (isAdded() && getView() != null) {
            showLoadingState(); // Added
            loadRewards();
            loadUserStarBalance();
        } else {
            Log.w(TAG, "Cannot force refresh - fragment not ready");
        }
    }

    // Show loading state
    private void showLoadingState() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (recyclerViewRewards != null) {
            recyclerViewRewards.setVisibility(View.GONE);
        }
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }
        if (layoutAddRewardButton != null) {
            layoutAddRewardButton.setVisibility(View.GONE);
        }
        if (fabAddReward != null) {
            fabAddReward.setVisibility(View.GONE);
        }
        Log.d(TAG, "Showing loading state");
    }

    // Hide loading state
    private void hideLoadingState() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        // Note: Don't automatically show recyclerView here
        // Let updateEmptyState handle the proper visibility based on data availability
        Log.d(TAG, "Hidden loading state");
    }
}
