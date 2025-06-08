package com.chores.app.kids.chores_app_for_kids.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
    private List<Reward> rewards;
    private OnRewardClickListener listener;
    private boolean isKidView;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);
        void onRewardRedeem(Reward reward);
        void onRewardEdit(Reward reward);
    }

    public RewardAdapter(List<Reward> rewards, OnRewardClickListener listener, boolean isKidView) {
        this.rewards = rewards;
        this.listener = listener;
        this.isKidView = isKidView;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewards.get(position);
        holder.bind(reward);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public void updateRewards(List<Reward> newRewards) {
        this.rewards.clear();
        this.rewards.addAll(newRewards);
        notifyDataSetChanged();
    }

    class RewardViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRewardIcon;
        private TextView tvRewardName;
        private TextView tvRewardDescription;
        private TextView tvStarCost;
        private Button btnRedeem;
        private Button btnEdit;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRewardIcon = itemView.findViewById(R.id.iv_reward_icon);
            tvRewardName = itemView.findViewById(R.id.tv_reward_name);
            tvRewardDescription = itemView.findViewById(R.id.tv_reward_description);
            tvStarCost = itemView.findViewById(R.id.tv_star_cost);
            btnRedeem = itemView.findViewById(R.id.btn_redeem);
            btnEdit = itemView.findViewById(R.id.btn_edit);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRewardClick(rewards.get(position));
                    }
                }
            });

            btnRedeem.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRewardRedeem(rewards.get(position));
                    }
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRewardEdit(rewards.get(position));
                    }
                }
            });
        }

        public void bind(Reward reward) {
            tvRewardName.setText(reward.getName());
            tvRewardDescription.setText(reward.getDescription());
            tvStarCost.setText(String.valueOf(reward.getStarCost()));

            // Set reward icon based on reward type
            setRewardIcon(reward.getIcon());

            // Configure view based on user type
            if (isKidView) {
                btnEdit.setVisibility(View.GONE);
                btnRedeem.setVisibility(View.VISIBLE);
            } else {
                btnEdit.setVisibility(View.VISIBLE);
                btnRedeem.setVisibility(View.GONE);
            }
        }

        private void setRewardIcon(String iconName) {
            int iconResource = R.drawable.ic_reward_default;

            if (iconName != null) {
                switch (iconName) {
                    case Constants.ICON_ICE_CREAM:
                        iconResource = R.drawable.ic_ice_cream;
                        break;
                    case Constants.ICON_BUY_TOY:
                        iconResource = R.drawable.ic_toy;
                        break;
                    case Constants.ICON_PIZZA:
                        iconResource = R.drawable.ic_pizza;
                        break;
                    case Constants.ICON_MONEY:
                        iconResource = R.drawable.ic_money;
                        break;
                    case Constants.ICON_WATCH_MOVIE:
                        iconResource = R.drawable.ic_movie;
                        break;
                    case Constants.ICON_PLAY_GAMES:
                        iconResource = R.drawable.ic_games;
                        break;
                    default:
                        iconResource = R.drawable.ic_reward_default;
                        break;
                }
            }

            ivRewardIcon.setImageResource(iconResource);
        }
    }
}