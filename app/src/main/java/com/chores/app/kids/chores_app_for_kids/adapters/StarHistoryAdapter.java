package com.chores.app.kids.chores_app_for_kids.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.StarTransaction;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.DateTimeUtils;

import java.util.List;

public class StarHistoryAdapter extends RecyclerView.Adapter<StarHistoryAdapter.TransactionViewHolder> {
    private List<StarTransaction> transactions;

    public StarHistoryAdapter(List<StarTransaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_star_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        StarTransaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<StarTransaction> newTransactions) {
        this.transactions.clear();
        this.transactions.addAll(newTransactions);
        notifyDataSetChanged();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTransactionIcon;
        private TextView tvTransactionReason;
        private TextView tvTransactionAmount;
        private TextView tvTransactionDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransactionIcon = itemView.findViewById(R.id.iv_transaction_icon);
            tvTransactionReason = itemView.findViewById(R.id.tv_transaction_reason);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvTransactionDate = itemView.findViewById(R.id.tv_transaction_date);
        }

        public void bind(StarTransaction transaction) {
            tvTransactionReason.setText(transaction.getReason());
            tvTransactionDate.setText(DateTimeUtils.getRelativeTimeString(transaction.getCreatedAt()));

            // Set amount with proper formatting and color
            String amountText;
            int amountColor;

            if (transaction.getAmount() > 0) {
                amountText = "+" + transaction.getAmount() + " ⭐";
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.success_green);
                ivTransactionIcon.setImageResource(R.drawable.ic_star_earned);
            } else {
                amountText = transaction.getAmount() + " ⭐";
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.error_red);
                ivTransactionIcon.setImageResource(R.drawable.ic_star_spent);
            }

            tvTransactionAmount.setText(amountText);
            tvTransactionAmount.setTextColor(amountColor);

            // Set icon based on transaction type
            setTransactionIcon(transaction.getType());
        }

        private void setTransactionIcon(String transactionType) {
            int iconResource;
            int backgroundColor;

            switch (transactionType) {
                case Constants.TRANSACTION_EARNED:
                    iconResource = R.drawable.ic_star_earned;
                    backgroundColor = R.color.success_green;
                    break;
                case Constants.TRANSACTION_SPENT:
                    iconResource = R.drawable.ic_star_spent;
                    backgroundColor = R.color.error_red;
                    break;
                case Constants.TRANSACTION_ADJUSTMENT:
                    iconResource = R.drawable.ic_adjustment;
                    backgroundColor = R.color.warning_yellow;
                    break;
                default:
                    iconResource = R.drawable.ic_star;
                    backgroundColor = R.color.gray_medium;
                    break;
            }

            ivTransactionIcon.setImageResource(iconResource);
            ivTransactionIcon.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), backgroundColor)
            );
        }
    }
}
