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
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import java.util.List;

public class KidRewardAdapter extends RecyclerView.Adapter<KidRewardAdapter.KidRewardViewHolder> {

    private List<Reward> rewardList;
    private Context context;
    private OnRewardRedeemListener listener;
    private int userStarBalance = 0;

    public interface OnRewardRedeemListener {
        void onRewardRedeem(Reward reward);
    }

    public KidRewardAdapter(List<Reward> rewardList, Context context, OnRewardRedeemListener listener) {
        this.rewardList = rewardList;
        this.context = context;
        this.listener = listener;
    }

    public void setUserStarBalance(int balance) {
        this.userStarBalance = balance;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KidRewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kid_reward, parent, false);
        return new KidRewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidRewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);

        holder.tvRewardName.setText(reward.getName());
        holder.tvStarCost.setText(reward.getStarCost() + " ⭐");

        // Set reward icon
        int iconResId = context.getResources().getIdentifier(reward.getIconName(), "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivRewardIcon.setImageResource(iconResId);
        } else {
            holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
        }

        // Check if user can afford this reward
        boolean canAfford = userStarBalance >= reward.getStarCost();

        if (canAfford) {
            holder.btnRedeem.setText("Redeem");
            holder.btnRedeem.setEnabled(true);
            holder.cardView.setAlpha(1.0f);
        } else {
            holder.btnRedeem.setText("Need " + (reward.getStarCost() - userStarBalance) + " more ⭐");
            holder.btnRedeem.setEnabled(false);
            holder.cardView.setAlpha(0.6f);
        }

        // Set up redeem listener
        holder.btnRedeem.setOnClickListener(v -> {
            if (canAfford && listener != null) {
                listener.onRewardRedeem(reward);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    static class KidRewardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivRewardIcon;
        TextView tvRewardName;
        TextView tvStarCost;
        Button btnRedeem;

        public KidRewardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_kid_reward);
            ivRewardIcon = itemView.findViewById(R.id.iv_reward_icon_large);
            tvRewardName = itemView.findViewById(R.id.tv_reward_name_large);
            tvStarCost = itemView.findViewById(R.id.tv_star_cost_large);
            btnRedeem = itemView.findViewById(R.id.btn_redeem_reward);
        }
    }
}
