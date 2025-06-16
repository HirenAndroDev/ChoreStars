package com.chores.app.kids.chores_app_for_kids.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.KidDashboardActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.KidRewardAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ValidationHelper;
import java.util.ArrayList;
import java.util.List;

public class KidRewardsFragment extends Fragment {

    private RecyclerView recyclerViewRewards;
    private TextView tvRewardsTitle;
    private TextView tvStarBalanceDisplay;
    private TextView tvEmptyMessage;
    private LinearLayout layoutEmptyState;
    private ImageView ivTreasureChest;

    private KidRewardAdapter rewardAdapter;
    private List<Reward> rewardList;
    private List<Reward> affordableRewards;
    private List<Reward> expensiveRewards;
    private String childId;
    private String familyId;
    private int currentStarBalance = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kid_rewards, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadUserData();
        loadRewards();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewRewards = view.findViewById(R.id.recycler_view_kid_rewards);
        tvRewardsTitle = view.findViewById(R.id.tv_rewards_title);
        tvStarBalanceDisplay = view.findViewById(R.id.tv_star_balance_display);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        ivTreasureChest = view.findViewById(R.id.iv_treasure_chest);

        // Set fun title
        tvRewardsTitle.setText("Reward Treasure! 🏆");

        // Animate treasure chest
        animateTreasureChest();
    }

    private void animateTreasureChest() {
        if (ivTreasureChest != null) {
            ObjectAnimator bounce = ObjectAnimator.ofFloat(ivTreasureChest, "translationY", 0f, -20f, 0f);
            bounce.setDuration(1500);
            bounce.setRepeatCount(ObjectAnimator.INFINITE);
            bounce.setRepeatMode(ObjectAnimator.RESTART);
            bounce.start();
        }
    }

    private void setupRecyclerView() {
        rewardList = new ArrayList<>();
        affordableRewards = new ArrayList<>();
        expensiveRewards = new ArrayList<>();

        rewardAdapter = new KidRewardAdapter(rewardList, getContext(), new KidRewardAdapter.OnRewardRedeemListener() {
            @Override
            public void onRewardRedeem(Reward reward) {
                handleRewardRedemption(reward);
            }
        });

        // Use 2 columns for kid-friendly grid
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewRewards.setLayoutManager(gridLayoutManager);
        recyclerViewRewards.setAdapter(rewardAdapter);

        // Add spacing between items
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_small);
        recyclerViewRewards.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
    }

    private void loadUserData() {
        childId = AuthHelper.getCurrentUserId(getContext());
        familyId = AuthHelper.getFamilyId(getContext());

        Log.d("KidRewardsFragment", "Loading user data - childId: " + childId + ", familyId: " + familyId);

        // Load current star balance for this specific child
        if (childId != null && !childId.isEmpty()) {
            FirebaseHelper.getUserStarBalanceById(childId, balance -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("KidRewardsFragment", "Loaded star balance for child " + childId + ": " + balance);
                        currentStarBalance = balance;
                        updateStarBalanceDisplay();
                        rewardAdapter.setUserStarBalance(balance);
                    });
                }
            });
        } else {
            Log.w("KidRewardsFragment", "No child ID found, using fallback method");
            // Fallback to generic method if no child ID
            FirebaseHelper.getUserStarBalance(balance -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("KidRewardsFragment", "Loaded star balance (fallback): " + balance);
                        currentStarBalance = balance;
                        updateStarBalanceDisplay();
                        rewardAdapter.setUserStarBalance(balance);
                    });
                }
            });
        }
    }

    private void updateStarBalanceDisplay() {
        tvStarBalanceDisplay.setText(String.format("Your Stars: %d ⭐", currentStarBalance));

        // Change color based on balance
        if (currentStarBalance >= 20) {
            tvStarBalanceDisplay.setTextColor(getResources().getColor(R.color.success_green));
        } else if (currentStarBalance >= 10) {
            tvStarBalanceDisplay.setTextColor(getResources().getColor(R.color.star_yellow));
        } else {
            tvStarBalanceDisplay.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void loadRewards() {
        if (childId == null || familyId == null) {
            showEmptyState("Let's load your awesome rewards!");
            return;
        }

        FirebaseHelper.getRewardsForChild(childId, familyId, new FirebaseHelper.RewardsCallback() {
            @Override
            public void onRewardsLoaded(List<Reward> rewards) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateRewardList(rewards);
                        announceRewardStatus();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showEmptyState("Let's try loading your rewards again!");
                    });
                }
            }
        });
    }

    private void updateRewardList(List<Reward> rewards) {
        rewardList.clear();
        affordableRewards.clear();
        expensiveRewards.clear();

        // Separate rewards by affordability
        for (Reward reward : rewards) {
            if (reward.getStarCost() <= currentStarBalance) {
                affordableRewards.add(reward);
            } else {
                expensiveRewards.add(reward);
            }
        }

        // Show affordable rewards first, then expensive ones
        rewardList.addAll(affordableRewards);
        rewardList.addAll(expensiveRewards);

        rewardAdapter.notifyDataSetChanged();

        // Show appropriate view
        if (rewardList.isEmpty()) {
            showEmptyState("No rewards available right now.\nKeep completing tasks! 🌟");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewRewards.setVisibility(View.VISIBLE);
        }
    }

    private void handleRewardRedemption(Reward reward) {
        // Check if user has enough stars (double-check)
        if (currentStarBalance < reward.getStarCost()) {
            int needed = reward.getStarCost() - currentStarBalance;
            handleInsufficientStars(reward, needed);
            return;
        }

        // Show confirmation dialog for expensive rewards
        if (reward.getStarCost() >= 20) {
            showRedemptionConfirmation(reward);
        } else {
            performRedemption(reward);
        }
    }

    private void showRedemptionConfirmation(Reward reward) {
        // Create kid-friendly confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.reward_confirmation_dialog, null);
        TextView tvRewardName = dialogView.findViewById(R.id.tv_reward_name);
        TextView tvStarCost = dialogView.findViewById(R.id.tv_star_cost);
        TextView tvRemainingStars = dialogView.findViewById(R.id.tv_remaining_stars);
        ImageView ivRewardIcon = dialogView.findViewById(R.id.iv_reward_icon);

        tvRewardName.setText(reward.getName());
        tvStarCost.setText(String.format("Costs: %d ⭐", reward.getStarCost()));
        int remaining = currentStarBalance - reward.getStarCost();
        tvRemainingStars.setText(String.format("You'll have: %d ⭐ left", remaining));

        // Set reward icon
        int iconResId = getContext().getResources().getIdentifier(reward.getIconName(), "drawable", getContext().getPackageName());
        if (iconResId != 0) {
            ivRewardIcon.setImageResource(iconResId);
        }

        builder.setView(dialogView)
                .setPositiveButton("Yes, Get It! 🎉", (dialog, which) -> {
                    performRedemption(reward);
                    SoundHelper.playSuccessSound(getContext());
                })
                .setNegativeButton("Not Now", (dialog, which) -> {
                    dialog.dismiss();
                    SoundHelper.playClickSound(getContext());
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Make buttons kid-friendly
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.success_green));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(R.color.text_secondary));
    }

    private void performRedemption(Reward reward) {
        // Show loading state
        showRedemptionInProgress(reward);

        // The existing FirebaseHelper.redeemReward method has been updated to save to both
        // rewardRedemptions and redeemedRewards collections, so no changes needed here
        FirebaseHelper.redeemReward(reward.getRewardId(), childId, result -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (result.isSuccessful()) {
                        // Redemption successful
                        celebrateRedemption(reward);

                        // Update star balance
                        currentStarBalance -= reward.getStarCost();
                        updateStarBalanceDisplay();
                        rewardAdapter.setUserStarBalance(currentStarBalance);

                        // Reorganize rewards by affordability
                        updateRewardList(rewardList);

                        // Notify parent activity
                        if (getActivity() instanceof KidDashboardActivity) {
                            ((KidDashboardActivity) getActivity()).onRewardRedeemed(reward);
                        }

                    } else {
                        // Redemption failed
                        SoundHelper.playErrorSound(getContext());


                    }
                });
            }
        });
    }

    private void showRedemptionInProgress(Reward reward) {
        // Update adapter to show loading state for this reward
        // For now, just play a sound
        SoundHelper.playClickSound(getContext());
    }

    private void celebrateRedemption(Reward reward) {
        // Play celebration sound
        SoundHelper.playRewardSound(getContext());

        // Show reward redemption animation
        showRedemptionAnimation(reward);


    }

    private void showRedemptionAnimation(Reward reward) {
        // Create celebration animation
        View celebrationView = LayoutInflater.from(getContext())
                .inflate(R.layout.reward_redemption_celebration, null);

        TextView tvCelebrationMessage = celebrationView.findViewById(R.id.tv_celebration_message);
        TextView tvRewardName = celebrationView.findViewById(R.id.tv_reward_name_celebration);
        ImageView ivCelebrationIcon = celebrationView.findViewById(R.id.iv_celebration_icon);

        tvCelebrationMessage.setText("Awesome! You got:");
        tvRewardName.setText(reward.getName());

        // Set reward icon
        int iconResId = getContext().getResources().getIdentifier(reward.getIconName(), "drawable", getContext().getPackageName());
        if (iconResId != 0) {
            ivCelebrationIcon.setImageResource(iconResId);
        }

        // Add to parent view with animation
        if (getActivity() != null) {
            ViewGroup parent = getActivity().findViewById(R.id.fragment_container_kid);
            if (parent != null) {
                parent.addView(celebrationView);

                // Animate celebration view
                celebrationView.setAlpha(0f);
                celebrationView.setScaleX(0.3f);
                celebrationView.setScaleY(0.3f);

                celebrationView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(600)
                        .withEndAction(() -> {
                            // Remove after delay
                            celebrationView.postDelayed(() -> {
                                celebrationView.animate()
                                        .alpha(0f)
                                        .scaleX(0.3f)
                                        .scaleY(0.3f)
                                        .setDuration(400)
                                        .withEndAction(() -> parent.removeView(celebrationView))
                                        .start();
                            }, 2500);
                        })
                        .start();
            }
        }
    }

    private void handleInsufficientStars(Reward reward, int needed) {
        SoundHelper.playErrorSound(getContext());

        // Show encouraging message
        String message = String.format("You need %d more stars for %s!\nKeep completing tasks! 💪",
                needed, reward.getName());

        // Create encouraging dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Keep Going! 🌟")
                .setMessage(message)
                .setPositiveButton("OK, I'll Keep Trying!", (dialog, which) -> {
                    dialog.dismiss();
                    SoundHelper.playSuccessSound(getContext());
                })
                .show();


        // Shake the reward item
        shakeRewardItem(reward);
    }

    private void shakeRewardItem(Reward reward) {
        // Find the reward view and shake it
        for (int i = 0; i < recyclerViewRewards.getChildCount(); i++) {
            View child = recyclerViewRewards.getChildAt(i);
            if (child != null) {
                ObjectAnimator shake = ObjectAnimator.ofFloat(child, "translationX", 0, 15, -15, 10, -10, 5, -5, 0);
                shake.setDuration(600);
                shake.start();
                break; // Shake first visible item as example
            }
        }
    }

    private void showEmptyState(String message) {
        recyclerViewRewards.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    private void announceRewardStatus() {
        if (getActivity() instanceof KidDashboardActivity) {
            KidDashboardActivity dashboard = (KidDashboardActivity) getActivity();

            if (rewardList.isEmpty()) {

            } else if (affordableRewards.isEmpty()) {


            } else {


            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh rewards and star balance when fragment becomes visible
        loadUserData();
        loadRewards();


    }

    // Custom ItemDecoration for grid spacing
    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}
