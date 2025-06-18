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
                if (description.contains("task")) {
                    return "Task \"" + extractTaskName(description) + "\" completed";
                } else {
                    return description;
                }
            case "spent":
                if (description.contains("reward")) {
                    return "Reward \"" + extractRewardName(description) + "\" redeemed";
                } else {
                    return description;
                }
            case "adjustment":
                return "Balance Adjustment";
            case "reset":
                return "Balance Reset";
            default:
                return description != null ? description : "Star Transaction";
        }
    }

    private void setTransactionIcon(TransactionViewHolder holder, StarTransaction transaction) {
        String type = transaction.getType();
        int iconResource;
        int backgroundTint;

        switch (type) {
            case "earned":
                iconResource = R.drawable.ic_checkbox_checked;
                backgroundTint = ContextCompat.getColor(context, R.color.success_green);
                break;
            case "spent":
                iconResource = R.drawable.ic_star;
                backgroundTint = ContextCompat.getColor(context, R.color.orange);
                break;
            case "adjustment":
                iconResource = R.drawable.ic_plus;
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
        // Extract task name from description like "Task completed: Brush teeth"
        if (description.contains(":")) {
            return description.substring(description.indexOf(":") + 1).trim();
        }
        return "Task";
    }

    private String extractRewardName(String description) {
        // Extract reward name from description like "Reward redeemed: Ice cream"
        if (description.contains(":")) {
            return description.substring(description.indexOf(":") + 1).trim();
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