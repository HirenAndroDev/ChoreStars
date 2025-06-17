package com.chores.app.kids.chores_app_for_kids.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.RedeemHistoryAdapter;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.RedeemedReward;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class RewardRedeemFragment extends Fragment {

    private static final String TAG = "RewardRedeemFragment";

    private RecyclerView recyclerViewRedeemHistory;
    private LinearLayout layoutEmptyState, layoutLoading;
    private RedeemHistoryAdapter redeemHistoryAdapter;
    private List<RedeemedReward> redeemedRewardList;
    private String familyId;
    private String currentUserId;
    private boolean isChildAccount;
    private ChildProfile selectedChild;
    private String childId;

    public RewardRedeemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reward_redeem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called");
        initializeViews(view);
        setupRecyclerView();
        loadRedeemedRewards();
    }

    private void initializeViews(View view) {
        recyclerViewRedeemHistory = view.findViewById(R.id.recycler_view_redeem_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutLoading = view.findViewById(R.id.layout_loading);

        // Get user information from AuthHelper
        familyId = AuthHelper.getFamilyId(getContext());
        isChildAccount = AuthHelper.isChildAccount(getContext());
        currentUserId = AuthHelper.getCurrentUserId(getContext());

        Log.d(TAG, "Family ID: " + familyId);
        Log.d(TAG, "Is Child Account: " + isChildAccount);
        Log.d(TAG, "Current User ID: " + currentUserId);
    }

    private void setupRecyclerView() {
        redeemedRewardList = new ArrayList<>();
        redeemHistoryAdapter = new RedeemHistoryAdapter(redeemedRewardList, getContext());
        recyclerViewRedeemHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRedeemHistory.setAdapter(redeemHistoryAdapter);
        Log.d(TAG, "RecyclerView setup completed");
    }

    private void loadRedeemedRewards() {
        Log.d(TAG, "loadRedeemedRewards called");

        // Validate required data
        if ((familyId == null || familyId.isEmpty()) && (currentUserId == null || currentUserId.isEmpty())) {
            Log.d(TAG, "Both family ID and user ID are null/empty, trying to get from current user");
            loadUserAndRetry();
            return;
        }

        if (familyId == null || familyId.isEmpty()) {
            Log.e(TAG, "Family ID is still null/empty after checks");
            showError("No family associated with this account");
            updateEmptyState(true);
            return;
        }

        loadFamilyRedeemedRewards();
    }

    private void loadUserAndRetry() {
        Log.d(TAG, "Loading current user to get family information");
        showLoading(true);

        FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
            @Override
            public void onUserLoaded(com.chores.app.kids.chores_app_for_kids.models.User user) {
                Log.d(TAG, "Current user loaded: " + user.getName() + ", Role: " + user.getRole());
                if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                    familyId = user.getFamilyId();
                    currentUserId = user.getUserId();
                    isChildAccount = "child".equals(user.getRole());

                    Log.d(TAG, "Updated - Family ID: " + familyId + ", User ID: " + currentUserId + ", Is Child: " + isChildAccount);
                    loadFamilyRedeemedRewards();
                } else {
                    Log.w(TAG, "User has no family ID");
                    showError("No family associated with this account");
                    updateEmptyState(true);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error getting current user: " + error);
                showError("Failed to load user information: " + error);
                updateEmptyState(true);
            }
        });
    }

    private void loadFamilyRedeemedRewards() {
        Log.d(TAG, "Loading redeemed rewards for family: " + familyId);
        showLoading(true);

        if (isChildAccount && currentUserId != null && !currentUserId.isEmpty()) {
            // Load child-specific redeemed rewards
            Log.d(TAG, "Loading child-specific redeemed rewards for user: " + currentUserId);
            loadChildRedeemedRewards();
        } else {
            // Load all family redeemed rewards
            Log.d(TAG, "Loading all family redeemed rewards");
            loadAllFamilyRedeemedRewards();
        }
    }

    private void loadChildRedeemedRewards() {
        FirebaseHelper.getRedeemedRewardsForChild(currentUserId, familyId, new FirebaseHelper.RedeemedRewardsCallback() {
            @Override
            public void onRedeemedRewardsLoaded(List<RedeemedReward> redeemedRewards) {
                Log.d(TAG, "Child-specific redeemed rewards loaded successfully. Count: " + redeemedRewards.size());
                handleRedeemedRewardsLoaded(redeemedRewards);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading child-specific redeemed rewards: " + error);
                // Fallback to loading all family rewards
                Log.d(TAG, "Falling back to load all family rewards");
                loadAllFamilyRedeemedRewards();
            }
        });
    }

    private void loadAllFamilyRedeemedRewards() {
        FirebaseHelper.getRedeemedRewards(familyId, new FirebaseHelper.RedeemedRewardsCallback() {
            @Override
            public void onRedeemedRewardsLoaded(List<RedeemedReward> redeemedRewards) {
                Log.d(TAG, "All family redeemed rewards loaded successfully. Count: " + redeemedRewards.size());
                handleRedeemedRewardsLoaded(redeemedRewards);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading family redeemed rewards: " + error);

                if (getActivity() != null && !isDetached()) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showError("Failed to load reward history: " + error);
                        updateEmptyState(true);
                    });
                }
            }
        });
    }

    private void handleRedeemedRewardsLoaded(List<RedeemedReward> redeemedRewards) {
        if (getActivity() != null && !isDetached()) {
            getActivity().runOnUiThread(() -> {
                showLoading(false);
                redeemedRewardList.clear();
                redeemedRewardList.addAll(redeemedRewards);
                redeemHistoryAdapter.notifyDataSetChanged();
                updateEmptyState(redeemedRewards.isEmpty());

                Log.d(TAG, "UI updated with " + redeemedRewards.size() + " redeemed rewards");

                // Log some details about the loaded rewards for debugging
                for (int i = 0; i < Math.min(3, redeemedRewards.size()); i++) {
                    RedeemedReward reward = redeemedRewards.get(i);
                    Log.d(TAG, "Reward " + i + ": " + reward.getRewardName() +
                            " by " + reward.getChildName() +
                            " at " + reward.getRedeemedAt());
                }
            });
        }
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading: " + show);
        if (getActivity() != null && !isDetached()) {
            if (show) {
                layoutLoading.setVisibility(View.VISIBLE);
                recyclerViewRedeemHistory.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.GONE);
            } else {
                layoutLoading.setVisibility(View.GONE);
            }
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        Log.d(TAG, "updateEmptyState: " + isEmpty);
        if (getActivity() != null && !isDetached()) {
            if (isEmpty) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                recyclerViewRedeemHistory.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                recyclerViewRedeemHistory.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        if (getContext() != null && !isDetached()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - refreshing redeemed rewards");
        // Refresh redeemed rewards when fragment becomes visible
        loadRedeemedRewards();
    }

    // Public method to refresh the list (can be called from parent activity/fragment)
    public void refreshRedeemedRewards() {
        Log.d(TAG, "refreshRedeemedRewards called");
        loadRedeemedRewards();
    }

    public void onChildSelectionChanged() {
        Log.d("RewardsFragment", "onChildSelectionChanged called");

        // Clear the stored selected child so it gets refreshed
        selectedChild = null;

        if (getView() != null && isAdded()) {
            loadChildRedeemedRewards();
        } else {
            // If view is not ready, schedule for later
            if (getView() != null) {
                getView().post(() -> {
                    if (isAdded()) {
                        loadChildRedeemedRewards();
                    }
                });
            }
        }
    }


    public void setSelectedChild(ChildProfile child) {
        Log.d("RewardsFragment", "setSelectedChild called with: " +
                (child != null ? child.getName() : "null"));
        this.selectedChild = child;
        if (child != null) {
            this.childId = child.getChildId();
        }
    }
}