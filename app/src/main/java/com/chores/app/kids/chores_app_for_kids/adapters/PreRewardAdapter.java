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
import com.chores.app.kids.chores_app_for_kids.models.PreReward;

import java.util.List;

public class PreRewardAdapter extends RecyclerView.Adapter<PreRewardAdapter.PreRewardViewHolder> {

    private List<PreReward> preRewardList;
    private Context context;
    private OnPreRewardClickListener clickListener;

    public interface OnPreRewardClickListener {
        void onPreRewardClick(PreReward preReward);
    }

    public PreRewardAdapter(List<PreReward> preRewardList, Context context, OnPreRewardClickListener clickListener) {
        this.preRewardList = preRewardList;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PreRewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pre_reward, parent, false);
        return new PreRewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreRewardViewHolder holder, int position) {
        PreReward preReward = preRewardList.get(position);

        holder.tvRewardName.setText(preReward.getName());
        holder.tvStarCost.setText(String.valueOf(preReward.getStarCost()));

        // Set icon - prioritize URL over drawable name
        if (preReward.getIconUrl() != null && !preReward.getIconUrl().isEmpty()) {
            // Check if it's a URL or drawable resource name
            if (preReward.getIconUrl().startsWith("http://") || preReward.getIconUrl().startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(context)
                        .load(preReward.getIconUrl())
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(holder.ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = context.getResources().getIdentifier(preReward.getIconUrl(), "drawable", context.getPackageName());
                if (drawableResId != 0) {
                    holder.ivRewardIcon.setImageResource(drawableResId);
                } else {
                    holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        } else if (preReward.getIconName() != null && !preReward.getIconName().isEmpty()) {
            // Fallback to iconName field
            if (preReward.getIconName().startsWith("http://") || preReward.getIconName().startsWith("https://")) {
                // Load from URL using Glide
                Glide.with(context)
                        .load(preReward.getIconName())
                        .placeholder(R.drawable.ic_reward_default)
                        .error(R.drawable.ic_reward_default)
                        .into(holder.ivRewardIcon);
            } else {
                // Check if it's a drawable resource name
                int drawableResId = context.getResources().getIdentifier(preReward.getIconName(), "drawable", context.getPackageName());
                if (drawableResId != 0) {
                    holder.ivRewardIcon.setImageResource(drawableResId);
                } else {
                    holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
                }
            }
        }  else {
            holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
        }

        holder.cardReward.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPreRewardClick(preReward);
            }
        });
    }

    @Override
    public int getItemCount() {
        return preRewardList.size();
    }

    public static class PreRewardViewHolder extends RecyclerView.ViewHolder {
        CardView cardReward;
        ImageView ivRewardIcon;
        TextView tvRewardName, tvStarCost;

        public PreRewardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardReward = itemView.findViewById(R.id.card_reward);
            ivRewardIcon = itemView.findViewById(R.id.iv_reward_icon);
            tvRewardName = itemView.findViewById(R.id.tv_reward_name);
            tvStarCost = itemView.findViewById(R.id.tv_star_cost);
        }
    }
}
