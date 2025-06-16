package com.chores.app.kids.chores_app_for_kids.fragments;

import android.os.Bundle;
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
import com.chores.app.kids.chores_app_for_kids.models.RedeemedReward;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.fragments.MainRewardFragment;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class RewardRedeemFragment extends Fragment {

    private RecyclerView recyclerViewRedeemHistory;
    private LinearLayout layoutEmptyState, layoutLoading;
    private RedeemHistoryAdapter redeemHistoryAdapter;
    private List<RedeemedReward> redeemedRewardList;
    private String familyId;
    private String childId;

    // Store the selected child directly in this fragment
    private ChildProfile selectedChild;

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

        Log.d("RewardRedeemFragment", "onViewCreated called");

        initializeViews(view);
        setupRecyclerView();

        // Don't load rewards immediately, wait for parent to notify us of selected child
    }

    private void initializeViews(View view) {
        recyclerViewRedeemHistory = view.findViewById(R.id.recycler_view_redeem_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutLoading = view.findViewById(R.id.layout_loading);

        familyId = AuthHelper.getFamilyId(getContext());
        Log.d("RewardRedeemFragment", "initializeViews - familyId from AuthHelper: " + familyId);
    }

    private void setupRecyclerView() {
        redeemedRewardList = new ArrayList<>();
        redeemHistoryAdapter = new RedeemHistoryAdapter(redeemedRewardList, getContext());
        recyclerViewRedeemHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRedeemHistory.setAdapter(redeemHistoryAdapter);
    }

    // Method to get the MainRewardFragment and its selected child
    private MainRewardFragment getMainRewardFragment() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof MainRewardFragment) {
            return (MainRewardFragment) parentFragment;
        }

        // Try to find it through the activity's fragment manager
        // This handles the ViewPager2 case where getParentFragment() returns null
        try {
            if (getActivity() != null) {
                List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof MainRewardFragment) {
                        return (MainRewardFragment) fragment;
                    }
                }
            }
        } catch (Exception e) {
            Log.w("RewardRedeemFragment", "Error finding MainRewardFragment", e);
        }

        return null;
    }

    private ChildProfile getSelectedChild() {
        // First, try to use our stored selected child
        if (selectedChild != null) {
            Log.d("RewardRedeemFragment", "getSelectedChild - returning stored: " + selectedChild.getName());
            return selectedChild;
        }

        // Try to get from MainRewardFragment
        MainRewardFragment mainRewardFragment = getMainRewardFragment();
        if (mainRewardFragment != null) {
            ChildProfile child = mainRewardFragment.getSelectedKid();
            if (child != null) {
                selectedChild = child; // Store it locally
                if (!child.getChildId().equals(childId)) {
                    childId = child.getChildId();
                }
                Log.d("RewardRedeemFragment", "getSelectedChild - got from MainRewardFragment: " + child.getName());
                return child;
            } else {
                Log.d("RewardRedeemFragment", "getSelectedChild - MainRewardFragment.getSelectedKid() returned null");
            }
        } else {
            Log.d("RewardRedeemFragment", "getSelectedChild - MainRewardFragment not found");
        }

        return null;
    }

    private void loadRedeemedRewards() {
        ChildProfile selectedChild = getSelectedChild();

        Log.d("RewardRedeemFragment", "loadRedeemedRewards() called - selectedChild: " +
                (selectedChild != null ? selectedChild.getName() : "null"));

        if (selectedChild == null) {
            // No child selected, show empty state with message
            updateEmptyState(true, "Please select a child to view redeem history");
            return;
        }

        if (familyId == null || familyId.isEmpty()) {
            familyId = selectedChild.getFamilyId();
        }

        Log.d("RewardRedeemFragment", "Using familyId: " + familyId);

        if (familyId == null || familyId.isEmpty()) {
            // Try to get familyId from current user
            FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(com.chores.app.kids.chores_app_for_kids.models.User user) {
                    if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                        familyId = user.getFamilyId();
                        Log.d("RewardRedeemFragment", "Got familyId from current user: " + familyId);
                        loadRedeemedRewardsForSelectedChild();
                    } else {
                        Log.e("RewardRedeemFragment", "No family found for current user");
                        updateEmptyState(true, "No family found");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("RewardRedeemFragment", "Error loading current user: " + error);
                    updateEmptyState(true, "Error loading family data");
                }
            });
            return;
        }

        loadRedeemedRewardsForSelectedChild();
    }

    private void loadRedeemedRewardsForSelectedChild() {
        ChildProfile selectedChild = getSelectedChild();
        if (selectedChild == null) {
            Log.e("RewardRedeemFragment", "loadRedeemedRewardsForSelectedChild: selectedChild is null");
            updateEmptyState(true, "Please select a child to view redeem history");
            return;
        }

        String childId = selectedChild.getChildId();
        Log.d("RewardRedeemFragment", "Loading redeemed rewards for childId: " + childId +
                ", familyId: " + familyId + ", child: " + selectedChild.getName());

        showLoading(true);

        // Load redeemed rewards specifically for this child
        FirebaseHelper.getRedeemedRewardsForChild(childId, familyId, new FirebaseHelper.RedeemedRewardsCallback() {
            @Override
            public void onRedeemedRewardsLoaded(List<RedeemedReward> redeemedRewards) {
                Log.d("RewardRedeemFragment", "Child-specific redeemed rewards loaded successfully, count: " + redeemedRewards.size());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        redeemedRewardList.clear();
                        redeemedRewardList.addAll(redeemedRewards);
                        redeemHistoryAdapter.notifyDataSetChanged();

                        String emptyMessage = redeemedRewards.isEmpty() ?
                                selectedChild.getName() + " hasn't redeemed any rewards yet" :
                                "";
                        updateEmptyState(redeemedRewards.isEmpty(), emptyMessage);

                        // Log each redeemed reward for debugging
                        for (RedeemedReward redeemedReward : redeemedRewards) {
                            Log.d("RewardRedeemFragment", "Redeemed reward for " + selectedChild.getName() + ": " +
                                    redeemedReward.getRewardName() + ", Stars: " + redeemedReward.getStarCost());
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("RewardRedeemFragment", "Error loading child-specific redeemed rewards: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        updateEmptyState(true, "Error loading redeem history: " + error);
                    });
                }
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            layoutLoading.setVisibility(View.VISIBLE);
            recyclerViewRedeemHistory.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.GONE);
        } else {
            layoutLoading.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        updateEmptyState(isEmpty, "No redeem history available");
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewRedeemHistory.setVisibility(View.GONE);

            // Update empty state message if there's a TextView for it
            android.widget.TextView emptyMessage = layoutEmptyState.findViewById(R.id.tv_empty_message);
            if (emptyMessage != null) {
                emptyMessage.setText(message);
            }
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewRedeemHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only load if we have a selected child
        if (selectedChild != null) {
            loadRedeemedRewards();
        } else {
            // Show message asking to select a child
            updateEmptyState(true, "Please select a child to view redeem history");
        }
    }

    // Public method to refresh the list (can be called from parent activity/fragment)
    public void refreshRedeemedRewards() {
        Log.d("RewardRedeemFragment", "refreshRedeemedRewards called");

        // Clear the stored selected child so it gets refreshed
        selectedChild = null;

        if (getView() != null && isAdded()) {
            loadRedeemedRewards();
        } else {
            // If view is not ready, schedule for later
            if (getView() != null) {
                getView().post(() -> {
                    if (isAdded()) {
                        loadRedeemedRewards();
                    }
                });
            }
        }
    }

    // Method to set the selected child directly (called by MainRewardFragment)
    public void setSelectedChild(ChildProfile child) {
        Log.d("RewardRedeemFragment", "setSelectedChild called with: " +
                (child != null ? child.getName() : "null"));
        this.selectedChild = child;
        if (child != null) {
            this.childId = child.getChildId();
        }
    }

    // Public method to refresh rewards when child selection changes
    // This is called by MainRewardFragment when the selected child changes
    public void onChildSelectionChanged() {
        Log.d("RewardRedeemFragment", "onChildSelectionChanged called");

        // Clear the stored selected child so it gets refreshed
        selectedChild = null;

        if (getView() != null && isAdded()) {
            loadRedeemedRewards();
        } else {
            // If view is not ready, schedule for later
            if (getView() != null) {
                getView().post(() -> {
                    if (isAdded()) {
                        loadRedeemedRewards();
                    }
                });
            }
        }
    }
}