package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private List<Reward> rewardList;
    private Context context;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);

        void onRedeemClick(Reward reward);
    }

    public RewardAdapter(List<Reward> rewardList, Context context) {
        this.rewardList = rewardList;
        this.context = context;
    }

    public void setOnRewardClickListener(OnRewardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward_new, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);

        holder.tvRewardName.setText(reward.getName());
        holder.tvStarCost.setText(String.valueOf(reward.getStarCost()));

        // Set reward icon - prioritize URL over drawable name
        if (reward.getIconUrl() != null && !reward.getIconUrl().isEmpty()) {
            // Check if it's a URL or drawable resource name
            if (reward.getIconUrl().startsWith("http://") || reward.getIconUrl().startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(context)
                        .load(reward.getIconUrl())
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(holder.ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = context.getResources().getIdentifier(reward.getIconUrl(), "drawable", context.getPackageName());
                if (drawableResId != 0) {
                    holder.ivRewardIcon.setImageResource(drawableResId);
                } else {
                    holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        } else if (reward.getIconName() != null && !reward.getIconName().isEmpty()) {
            // Fallback to iconName field
            if (reward.getIconName().startsWith("http://") || reward.getIconName().startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(context)
                        .load(reward.getIconName())
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(holder.ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = context.getResources().getIdentifier(reward.getIconName(), "drawable", context.getPackageName());
                if (drawableResId != 0) {
                    holder.ivRewardIcon.setImageResource(drawableResId);
                } else {
                    holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        } else {
            holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
        }

        // Set up click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardClick(reward);
            }
        });

        holder.btnRedeem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRedeemClick(reward);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivRewardIcon;
        TextView tvRewardName;
        TextView tvStarCost;
        Button btnRedeem;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_reward);
            ivRewardIcon = itemView.findViewById(R.id.iv_reward_icon);
            tvRewardName = itemView.findViewById(R.id.tv_reward_name);
            tvStarCost = itemView.findViewById(R.id.tv_star_cost);
            btnRedeem = itemView.findViewById(R.id.btn_redeem);
        }
    }
}
