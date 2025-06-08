package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Reward;

public class RewardRedemptionDialog extends Dialog {
    private Reward reward;
    private int currentStarBalance;
    private OnRewardRedemptionListener listener;

    private TextView tvRewardName;
    private TextView tvStarCost;
    private TextView tvRemainingStars;
    private ImageView ivRewardIcon;
    private Button btnRedeem;
    private Button btnCancel;

    public interface OnRewardRedemptionListener {
        void onRewardRedeemed(Reward reward);
    }

    public RewardRedemptionDialog(@NonNull Context context, Reward reward, int currentStarBalance, OnRewardRedemptionListener listener) {
        super(context);
        this.reward = reward;
        this.currentStarBalance = currentStarBalance;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_reward_redemption);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        tvRewardName = findViewById(R.id.tv_reward_name);
        tvStarCost = findViewById(R.id.tv_star_cost);
        tvRemainingStars = findViewById(R.id.tv_remaining_stars);
        ivRewardIcon = findViewById(R.id.iv_reward_icon);
        btnRedeem = findViewById(R.id.btn_redeem);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupData() {
        tvRewardName.setText(reward.getName());
        tvStarCost.setText("Cost: " + reward.getStarCost() + " ⭐");

        int remainingStars = currentStarBalance - reward.getStarCost();
        tvRemainingStars.setText("Remaining stars: " + remainingStars + " ⭐");

        // Enable/disable redeem button based on star balance
        boolean canRedeem = currentStarBalance >= reward.getStarCost();
        btnRedeem.setEnabled(canRedeem);

        if (!canRedeem) {
            btnRedeem.setText("Not enough stars");
        }

        // Set reward icon
        ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
    }

    private void setupClickListeners() {
        btnRedeem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardRedeemed(reward);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
