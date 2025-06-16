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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        // Don't load rewards immediately, wait for selected child
        // Get parent fragment reference
        if (getParentFragment() instanceof MainRewardFragment) {
            parentFragment = (MainRewardFragment) getParentFragment();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("RewardsFragment", "onViewCreated called");

        // Ensure parent fragment reference is set
        if (getParentFragment() instanceof MainRewardFragment) {
            parentFragment = (MainRewardFragment) getParentFragment();
            Log.d("RewardsFragment", "Parent fragment set successfully");
        } else {
            Log.w("RewardsFragment", "Parent fragment is not MainRewardFragment: " +
                    (getParentFragment() != null ? getParentFragment().getClass().getSimpleName() : "null"));
        }

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
        Log.d("RewardsFragment", "initializeViews - familyId from AuthHelper: " + familyId);
    }

    private void setupRecyclerView() {
        rewardList = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewardList, getContext());
        rewardAdapter.setOnRewardClickListener(new RewardAdapter.OnRewardClickListener() {
            @Override
            public void onRewardClick(Reward reward) {
                // Handle reward item click (maybe show details)
            }

            @Override
            public void onRedeemClick(Reward reward) {
                // Handle redeem button click
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

    private ChildProfile getSelectedChild() {
        // Get selected child from parent fragment
//        Fragment parentFragment = getParentFragment();
//        if (parentFragment instanceof MainRewardFragment) {
//            MainRewardFragment mainRewardFragment = (MainRewardFragment) parentFragment;
//            return mainRewardFragment.getSelectedKid();
//        }

        if (parentFragment != null) {
            ChildProfile selectedKid = parentFragment.getSelectedKid();
            if (selectedKid != null) {
                if (!selectedKid.getChildId().equals(childId)) {
                    childId = selectedKid.getChildId();
                }
            }
        }

        return null;
    }

    private void loadRewards() {
        ChildProfile selectedChild = getSelectedChild();

        Log.d("RewardsFragment", "loadRewards() called - selectedChild: " +
                (selectedChild != null ? selectedChild.getName() : "null"));

        if (selectedChild == null) {
            // No child selected, show empty state with message
            updateEmptyState(true, "Please select a child to view rewards");
            return;
        }

        if (familyId == null || familyId.isEmpty()) {
            familyId = selectedChild.getFamilyId();
        }

        Log.d("RewardsFragment", "Using familyId: " + familyId);

        if (familyId == null || familyId.isEmpty()) {
            // Try to get familyId from current user
            FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(com.chores.app.kids.chores_app_for_kids.models.User user) {
                    if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                        familyId = user.getFamilyId();
                        Log.d("RewardsFragment", "Got familyId from current user: " + familyId);
                        loadRewardsForSelectedChild();
                    } else {
                        Log.e("RewardsFragment", "No family found for current user");
                        updateEmptyState(true, "No family found");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("RewardsFragment", "Error loading current user: " + error);
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
            Log.e("RewardsFragment", "loadRewardsForSelectedChild: selectedChild is null");
            updateEmptyState(true, "Please select a child to view rewards");
            return;
        }

        Log.d("RewardsFragment", "Loading rewards for familyId: " + familyId +
                ", child: " + selectedChild.getName());

        // Load all rewards for the family
        FirebaseHelper.getFamilyRewards(familyId, new FirebaseHelper.RewardsCallback() {
            @Override
            public void onRewardsLoaded(List<Reward> rewards) {
                Log.d("RewardsFragment", "Rewards loaded successfully, count: " + rewards.size());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        rewardList.clear();
                        rewardList.addAll(rewards);
                        rewardAdapter.notifyDataSetChanged();
                        updateEmptyState(rewards.isEmpty(), "No rewards available for " + selectedChild.getName());

                        // Log each reward for debugging
                        for (Reward reward : rewards) {
                            Log.d("RewardsFragment", "Reward: " + reward.getName() +
                                    ", Stars: " + reward.getStarCost());
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("RewardsFragment", "Error loading rewards: " + error);
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

        // Check if selected child has enough stars
        if (selectedChildStarBalance >= reward.getStarCost()) {
            // Child has enough stars, proceed with redemption
            FirebaseHelper.redeemRewardWithSelectedChild(reward.getRewardId(), selectedChildId, task -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Reward redeemed successfully for " + selectedChildName + "!", Toast.LENGTH_SHORT).show();

                            // Refresh the selected child's star balance in parent fragment
                            Fragment parentFragment = getParentFragment();
                            if (parentFragment instanceof MainRewardFragment) {
                                ((MainRewardFragment) parentFragment).updateKidProfileUI();
                            }

                            // Refresh rewards list to update availability
                            loadRewards();

                            // Refresh redeem history in the other tab
                            refreshRedeemHistoryTab();
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Failed to redeem reward";
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(getContext(), selectedChildName + " needs " +
                            (reward.getStarCost() - selectedChildStarBalance) + " more stars for this reward!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void refreshRedeemHistoryTab() {
        // Try to refresh the redeem history tab
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof MainRewardFragment) {
            MainRewardFragment mainRewardFragment = (MainRewardFragment) parentFragment;
            // Access the ViewPager2 and find the RewardRedeemFragment
            try {
                androidx.viewpager2.widget.ViewPager2 viewPager = mainRewardFragment.getView().findViewById(R.id.viewPager);
                if (viewPager != null) {
                    androidx.fragment.app.FragmentActivity activity = mainRewardFragment.requireActivity();
                    androidx.fragment.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();

                    // Find the RewardRedeemFragment
                    String fragmentTag = "f" + 1; // ViewPager2 uses "f" + position as tag
                    Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
                    if (fragment instanceof RewardRedeemFragment) {
                        ((RewardRedeemFragment) fragment).refreshRedeemedRewards();
                    }
                }
            } catch (Exception e) {
                // Ignore errors in refresh
            }
        }
    }

    private void loadUserStarBalance() {
        // Load star balance for the selected child
        ChildProfile selectedChild = getSelectedChild();
        if (selectedChild != null && tvStarBalance != null) {
            tvStarBalance.setText(String.valueOf(selectedChild.getStarBalance()));
        }
    }

    // Public method to refresh rewards when child selection changes
    public void onChildSelectionChanged() {
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
}
