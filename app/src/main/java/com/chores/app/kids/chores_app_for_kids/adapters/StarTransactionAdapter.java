package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.StarTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StarTransactionAdapter extends RecyclerView.Adapter<StarTransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<StarTransaction> transactions;
    private final SimpleDateFormat dateFormat;

    public StarTransactionAdapter(Context context, List<StarTransaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_star_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        StarTransaction transaction = transactions.get(position);

        // Set transaction title based on type
        String title = getTransactionTitle(transaction);
        holder.tvTransactionTitle.setText(title);

        // Set transaction icon and background color
        setTransactionIcon(holder, transaction);

        // Set star amount with appropriate color and sign
        int amount = transaction.getAmount();
        String amountText = (amount > 0 ? "+" : "") + amount;
        holder.tvStarAmount.setText(amountText);

        // Set amount color based on positive/negative
        int amountColor = amount > 0 ?
                ContextCompat.getColor(context, R.color.success_green) :
                ContextCompat.getColor(context, R.color.error_red);
        holder.tvStarAmount.setTextColor(amountColor);

        // Set star icon color based on amount
        int starTint = amount > 0 ?
                ContextCompat.getColor(context, R.color.success_green) :
                ContextCompat.getColor(context, R.color.error_red);
        holder.ivStarIcon.setColorFilter(starTint);

        // Format and set transaction time (currently hidden as per design)
        String timeText = getRelativeTime(transaction.getTimestamp());
        holder.tvTransactionTime.setText(timeText);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String getTransactionTitle(StarTransaction transaction) {
        String type = transaction.getType();
        String description = transaction.getDescription();

        switch (type) {
            case "earned":
                if (description != null && description.contains("Task completed:")) {
                    return "Task \"" + extractTaskName(description) + "\" completed";
                } else {
                    return description != null ? description : "Stars Earned";
                }
            case "spent":
                if (description != null && description.contains("Task uncompleted:")) {
                    return "Task \"" + extractTaskName(description) + "\" uncompleted";
                } else if (description != null && description.contains("Redeemed:")) {
                    return description.replace("Redeemed:", "Reward") + " redeemed";
                } else {
                    return description != null ? description : "Stars Spent";
                }
            case "reward_redemption":
                if (description != null && description.contains("Redeemed:")) {
                    return description.replace("Redeemed:", "Reward") + " redeemed";
                } else {
                    return description != null ? description : "Reward Redeemed";
                }
            case "adjustment":
                return description != null ? description : "Balance Adjustment";
            case "reset":
                return "Balance Reset";
            default:
                return description != null ? description : "Star Transaction";
        }
    }

    private void setTransactionIcon(TransactionViewHolder holder, StarTransaction transaction) {
        String type = transaction.getType();
        int amount = transaction.getAmount();
        int iconResource;
        int backgroundTint;

        // Reset rotation first
        holder.ivTransactionIcon.setRotation(0);

        switch (type) {
            case "earned":
                iconResource = R.drawable.ic_checkbox_checked;
                backgroundTint = ContextCompat.getColor(context, R.color.success_green);
                break;
            case "spent":
                if (transaction.getDescription() != null && transaction.getDescription().contains("uncompleted")) {
                    // Task uncompleted - use different icon
                    iconResource = R.drawable.ic_close;
                    backgroundTint = ContextCompat.getColor(context, R.color.error_red);
                } else {
                    // Regular spending (reward)
                    iconResource = R.drawable.ic_star;
                    backgroundTint = ContextCompat.getColor(context, R.color.orange);
                }
                break;
            case "reward_redemption":
                iconResource = R.drawable.ic_star;
                backgroundTint = ContextCompat.getColor(context, R.color.orange);
                break;
            case "adjustment":
                iconResource = amount > 0 ? R.drawable.ic_plus : R.drawable.ic_minus;
                backgroundTint = ContextCompat.getColor(context, R.color.orange);
                break;
            case "reset":
                iconResource = R.drawable.ic_volume_up;
                backgroundTint = ContextCompat.getColor(context, R.color.orange);
                holder.ivTransactionIcon.setRotation(180);
                break;
            default:
                iconResource = R.drawable.ic_star;
                backgroundTint = ContextCompat.getColor(context, R.color.orange);
                break;
        }

        holder.ivTransactionIcon.setImageResource(iconResource);
        holder.layoutTransactionIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(backgroundTint));
    }

    private String extractTaskName(String description) {
        // Extract task name from description like "Task completed: Brush teeth" or "Task uncompleted: Brush teeth"
        if (description != null && description.contains(": ")) {
            String[] parts = description.split(": ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return "Task";
    }

    private String extractRewardName(String description) {
        // Extract reward name from description like "Redeemed: Ice cream"
        if (description != null && description.contains(": ")) {
            String[] parts = description.split(": ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return "Reward";
    }

    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Convert to minutes, hours, days
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            return dateFormat.format(new Date(timestamp));
        }
    }

    public void updateTransactions(List<StarTransaction> newTransactions) {
        this.transactions.clear();
        this.transactions.addAll(newTransactions);
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutTransactionIcon;
        ImageView ivTransactionIcon;
        TextView tvTransactionTitle;
        TextView tvTransactionTime;
        TextView tvStarAmount;
        ImageView ivStarIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutTransactionIcon = itemView.findViewById(R.id.layoutTransactionIcon);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionTime = itemView.findViewById(R.id.tvTransactionTime);
            tvStarAmount = itemView.findViewById(R.id.tvStarAmount);
            ivStarIcon = itemView.findViewById(R.id.ivStarIcon);
        }
    }
}
