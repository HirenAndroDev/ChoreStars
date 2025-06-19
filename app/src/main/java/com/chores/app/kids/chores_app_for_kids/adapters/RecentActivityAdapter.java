package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.RecentActivity;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
    private List<RecentActivity> activities;
    private Context context;

    public RecentActivityAdapter(List<RecentActivity> activities, Context context) {
        this.activities = activities;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity activity = activities.get(position);

        holder.tvDescription.setText(activity.getDescription());
        holder.tvStarAmount.setText((activity.getStarAmount() > 0 ? "+" : "") + activity.getStarAmount());
        holder.tvTimestamp.setText(DateUtils.getRelativeTimeSpanString(activity.getTimestamp()));

        // Set icon and color based on type
        switch (activity.getType()) {
            case "earned":
                holder.ivIcon.setImageResource(R.drawable.ic_star);
                holder.ivIcon.setColorFilter(context.getColor(R.color.success_color));
                holder.tvStarAmount.setTextColor(context.getColor(R.color.success_color));
                break;
            case "spent":
                holder.ivIcon.setImageResource(R.drawable.ic_settings);
                holder.ivIcon.setColorFilter(context.getColor(R.color.error_color));
                holder.tvStarAmount.setTextColor(context.getColor(R.color.error_color));
                break;
            default:
                holder.ivIcon.setImageResource(R.drawable.ic_shower);
                holder.ivIcon.setColorFilter(context.getColor(R.color.warning_color));
                holder.tvStarAmount.setTextColor(context.getColor(R.color.warning_color));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDescription, tvStarAmount, tvTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvStarAmount = itemView.findViewById(R.id.tv_star_amount);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}