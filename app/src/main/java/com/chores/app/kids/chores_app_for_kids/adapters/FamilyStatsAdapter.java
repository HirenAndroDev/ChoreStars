package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.DashboardStats;

public class FamilyStatsAdapter extends RecyclerView.Adapter<FamilyStatsAdapter.ViewHolder> {
    private Context context;
    private DashboardStats stats;
    private String[] statTitles = {"Weekly Tasks", "Weekly Stars", "Total Balance", "Family Size"};
    private int[] statIcons = {R.drawable.ic_task_default, R.drawable.ic_star, R.drawable.ic_reward_default, R.drawable.ic_family};

    public FamilyStatsAdapter(Context context) {
        this.context = context;
        this.stats = new DashboardStats();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_family_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(statTitles[position]);
        holder.ivIcon.setImageResource(statIcons[position]);

        switch (position) {
            case 0:
                holder.tvValue.setText(String.valueOf(stats.getTasksCompletedThisWeek()));
                break;
            case 1:
                holder.tvValue.setText(String.valueOf(stats.getStarsEarnedThisWeek()));
                break;
            case 2:
                holder.tvValue.setText(String.valueOf(stats.getTotalStarBalance()));
                break;
            case 3:
                holder.tvValue.setText(String.valueOf(stats.getChildCount()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return statTitles.length;
    }

    public void updateStats(DashboardStats stats) {
        this.stats = stats;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvValue;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvValue = itemView.findViewById(R.id.tv_value);
        }
    }
}