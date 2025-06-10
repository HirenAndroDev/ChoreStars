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

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private List<Reward> rewardList;
    private Context context;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);
        void onRewardEdit(Reward reward);
        void onRewardDelete(Reward reward);
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);

        holder.tvRewardName.setText(reward.getName());
        holder.tvStarCost.setText(String.valueOf(reward.getStarCost()));

        // Set reward icon
        int iconResId = context.getResources().getIdentifier(reward.getIconName(), "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivRewardIcon.setImageResource(iconResId);
        } else {
            holder.ivRewardIcon.setImageResource(R.drawable.ic_reward_default);
        }

        // Show custom badge if it's a custom reward
        if (reward.isCustom()) {
            holder.tvCustomBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvCustomBadge.setVisibility(View.GONE);
        }

        // Set up click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardClick(reward);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardEdit(reward);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardDelete(reward);
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
        TextView tvCustomBadge;
        Button btnEdit;
        Button btnDelete;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_reward);
            ivRewardIcon = itemView.findViewById(R.id.iv_reward_icon);
            tvRewardName = itemView.findViewById(R.id.tv_reward_name);
            tvStarCost = itemView.findViewById(R.id.tv_star_cost);
            tvCustomBadge = itemView.findViewById(R.id.tv_custom_badge);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
