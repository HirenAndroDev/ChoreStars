package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.RedeemedReward;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RedeemHistoryAdapter extends RecyclerView.Adapter<RedeemHistoryAdapter.RedeemHistoryViewHolder> {

    private List<RedeemedReward> redeemedRewardList;
    private Context context;
    private SimpleDateFormat dateFormatter;

    public RedeemHistoryAdapter(List<RedeemedReward> redeemedRewardList, Context context) {
        this.redeemedRewardList = redeemedRewardList;
        this.context = context;
        this.dateFormatter = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public RedeemHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_redeem_history, parent, false);
        return new RedeemHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RedeemHistoryViewHolder holder, int position) {
        RedeemedReward redeemedReward = redeemedRewardList.get(position);

        holder.tvRewardName.setText(redeemedReward.getRewardName());
        holder.tvStarCost.setText(String.valueOf(redeemedReward.getStarCost()));
        holder.tvChildName.setText(redeemedReward.getChildName());

        // Format and set the redeemed time
        if (redeemedReward.getRedeemedAt() != null) {
            holder.tvRedeemedTime.setText(dateFormatter.format(redeemedReward.getRedeemedAt()));
        } else {
            holder.tvRedeemedTime.setText("Unknown time");
        }

        // Set reward icon - prioritize URL over drawable name
        if (redeemedReward.getIconUrl() != null && !redeemedReward.getIconUrl().isEmpty()) {
            // Check if it's a URL or drawable resource name
            if (redeemedReward.getIconUrl().startsWith("http://") || redeemedReward.getIconUrl().startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(context)
                        .load(redeemedReward.getIconUrl())
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(holder.ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = context.getResources().getIdentifier(redeemedReward.getIconUrl(), "drawable", context.getPackageName());
                if (drawableResId != 0) {
                    holder.ivRewardIcon.setImageResource(drawableResId);
                } else {
                    holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        } else if (redeemedReward.getIconName() != null && !redeemedReward.getIconName().isEmpty()) {
            // Fallback to iconName field
            if (redeemedReward.getIconName().startsWith("http://") || redeemedReward.getIconName().startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(context)
                        .load(redeemedReward.getIconName())
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(holder.ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = context.getResources().getIdentifier(redeemedReward.getIconName(), "drawable", context.getPackageName());
                if (drawableResId != 0) {
                    holder.ivRewardIcon.setImageResource(drawableResId);
                } else {
                    holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        } else {
            holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
        }
    }

    @Override
    public int getItemCount() {
        return redeemedRewardList.size();
    }

    static class RedeemHistoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivRewardIcon;
        TextView tvRewardName;
        TextView tvStarCost;
        TextView tvChildName;
        TextView tvRedeemedTime;

        public RedeemHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_redeem_history);
            ivRewardIcon = itemView.findViewById(R.id.iv_reward_icon);
            tvRewardName = itemView.findViewById(R.id.tv_reward_name);
            tvStarCost = itemView.findViewById(R.id.tv_star_cost);
            tvChildName = itemView.findViewById(R.id.tv_child_name);
            tvRedeemedTime = itemView.findViewById(R.id.tv_redeemed_time);
        }
    }
}