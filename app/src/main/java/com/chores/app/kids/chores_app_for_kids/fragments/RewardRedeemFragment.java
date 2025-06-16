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
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class RewardRedeemFragment extends Fragment {

    private RecyclerView recyclerViewRedeemHistory;
    private LinearLayout layoutEmptyState, layoutLoading;
    private RedeemHistoryAdapter redeemHistoryAdapter;
    private List<RedeemedReward> redeemedRewardList;
    private String familyId;

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

        initializeViews(view);
        setupRecyclerView();
        loadRedeemedRewards();

    }

    private void initializeViews(View view) {
        recyclerViewRedeemHistory = view.findViewById(R.id.recycler_view_redeem_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutLoading = view.findViewById(R.id.layout_loading);

        familyId = AuthHelper.getFamilyId(getContext());
    }

    private void setupRecyclerView() {
        redeemedRewardList = new ArrayList<>();
        redeemHistoryAdapter = new RedeemHistoryAdapter(redeemedRewardList, getContext());
        recyclerViewRedeemHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRedeemHistory.setAdapter(redeemHistoryAdapter);
    }

    private void loadRedeemedRewards() {
        if (familyId == null || familyId.isEmpty()) {
            // Try to get familyId from current user
            FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
                @Override
                public void onUserLoaded(com.chores.app.kids.chores_app_for_kids.models.User user) {
                    if (user.getFamilyId() != null && !user.getFamilyId().isEmpty()) {
                        familyId = user.getFamilyId();
                        loadFamilyRedeemedRewards();
                    } else {
                        updateEmptyState(true);
                    }
                }

                @Override
                public void onError(String error) {
                    updateEmptyState(true);
                }
            });
            return;
        }

        loadFamilyRedeemedRewards();
    }

    private void loadFamilyRedeemedRewards() {
        showLoading(true);

        FirebaseHelper.getRedeemedRewards(familyId, new FirebaseHelper.RedeemedRewardsCallback() {
            @Override
            public void onRedeemedRewardsLoaded(List<RedeemedReward> redeemedRewards) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        redeemedRewardList.clear();
                        redeemedRewardList.addAll(redeemedRewards);
                        redeemHistoryAdapter.notifyDataSetChanged();
                        updateEmptyState(redeemedRewards.isEmpty());
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        updateEmptyState(true);
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
        if (isEmpty) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewRedeemHistory.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewRedeemHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh redeemed rewards when fragment becomes visible
        loadRedeemedRewards();
    }

    // Public method to refresh the list (can be called from parent activity/fragment)
    public void refreshRedeemedRewards() {
        loadRedeemedRewards();
    }
}
