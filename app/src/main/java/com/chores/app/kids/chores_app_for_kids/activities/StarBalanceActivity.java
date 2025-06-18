package com.chores.app.kids.chores_app_for_kids.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.StarTransactionAdapter;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.StarTransaction;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StarBalanceActivity extends AppCompatActivity {

    private static final String TAG = "StarBalanceActivity";
    public static final String EXTRA_CHILD_ID = "child_id";
    public static final String EXTRA_CHILD_NAME = "child_name";
    public static final String EXTRA_CHILD_PROFILE_URL = "child_profile_url";
    public static final String EXTRA_CURRENT_BALANCE = "current_balance";

    private ImageView ivBack;
    private CircleImageView ivKidProfile;
    private TextView tvKidName, tvStarsBalance;
    private LinearLayout layoutAdjust, layoutReset;
    private TextView tvClearHistory, tvFilterLabel, tvTodayLabel;
    private RecyclerView rvTransactionHistory;

    private String childId;
    private String childName;
    private String childProfileUrl;
    private int currentBalance;

    private FirebaseHelper firebaseHelper;
    private StarTransactionAdapter transactionAdapter;
    private List<StarTransaction> allTransactions;
    private List<StarTransaction> filteredTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_balance);

        // Get data from intent
        getIntentData();

        // Initialize Firebase
        firebaseHelper = new FirebaseHelper();

        // Initialize views
        initViews();

        // Setup UI
        setupUI();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load transaction history
        loadTransactionHistory();
    }

    private void getIntentData() {
        if (getIntent() != null) {
            childId = getIntent().getStringExtra(EXTRA_CHILD_ID);
            childName = getIntent().getStringExtra(EXTRA_CHILD_NAME);
            childProfileUrl = getIntent().getStringExtra(EXTRA_CHILD_PROFILE_URL);
            currentBalance = getIntent().getIntExtra(EXTRA_CURRENT_BALANCE, 0);
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivKidProfile = findViewById(R.id.ivKidProfile);
        tvKidName = findViewById(R.id.tvKidName);
        tvStarsBalance = findViewById(R.id.tvStarsBalance);
        layoutAdjust = findViewById(R.id.layoutAdjust);
        layoutReset = findViewById(R.id.layoutReset);
        tvClearHistory = findViewById(R.id.tvClearHistory);
        tvFilterLabel = findViewById(R.id.tvFilterLabel);
        tvTodayLabel = findViewById(R.id.tvTodayLabel);
        rvTransactionHistory = findViewById(R.id.rvTransactionHistory);
    }

    private void setupUI() {
        // Set kid name and balance
        tvKidName.setText(childName != null ? childName : "Kid");
        tvStarsBalance.setText(String.valueOf(currentBalance));

        // Load profile image
        if (!TextUtils.isEmpty(childProfileUrl)) {
            Glide.with(this)
                    .load(childProfileUrl)
                    .circleCrop()
                    .into(ivKidProfile);
        } else {
            ivKidProfile.setImageResource(R.drawable.default_avatar);
        }
    }

    private void setupRecyclerView() {
        allTransactions = new ArrayList<>();
        filteredTransactions = new ArrayList<>();

        transactionAdapter = new StarTransactionAdapter(this, filteredTransactions);
        rvTransactionHistory.setLayoutManager(new LinearLayoutManager(this));
        rvTransactionHistory.setAdapter(transactionAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        layoutAdjust.setOnClickListener(v -> showAdjustDialog());

        layoutReset.setOnClickListener(v -> showResetConfirmDialog());

        tvClearHistory.setOnClickListener(v -> showClearHistoryDialog());

        tvFilterLabel.setOnClickListener(v -> showFilterDialog());
    }

    private void loadTransactionHistory() {
        if (TextUtils.isEmpty(childId)) {
            Log.e(TAG, "Child ID is null or empty");
            return;
        }

        firebaseHelper.getStarTransactions(childId, new FirebaseHelper.OnStarTransactionsLoadedListener() {
            @Override
            public void onTransactionsLoaded(List<StarTransaction> transactions) {
                allTransactions.clear();
                allTransactions.addAll(transactions);

                // Sort by timestamp (newest first)
                Collections.sort(allTransactions, (t1, t2) ->
                        Long.compare(t2.getTimestamp(), t1.getTimestamp()));

                // Apply current filter
                applyFilter("All Records");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading transactions: " + error);
                Toast.makeText(StarBalanceActivity.this, "Error loading transaction history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAdjustDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adjust_stars, null);

        TextView tvCurrentBalance = dialogView.findViewById(R.id.tvCurrentBalance);
        EditText etStarAmount = dialogView.findViewById(R.id.etStarAmount);
        EditText etReason = dialogView.findViewById(R.id.etReason);
        ImageView ivDecrease = dialogView.findViewById(R.id.ivDecrease);
        ImageView ivIncrease = dialogView.findViewById(R.id.ivIncrease);
        TextView tvQuickAdd1 = dialogView.findViewById(R.id.tvQuickAdd1);
        TextView tvQuickAdd5 = dialogView.findViewById(R.id.tvQuickAdd5);
        TextView tvQuickRemove1 = dialogView.findViewById(R.id.tvQuickRemove1);
        TextView tvQuickRemove5 = dialogView.findViewById(R.id.tvQuickRemove5);
        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        TextView tvConfirm = dialogView.findViewById(R.id.tvConfirm);

        tvCurrentBalance.setText(String.valueOf(currentBalance));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Setup amount input listeners
        ivDecrease.setOnClickListener(v -> {
            try {
                int currentAmount = Integer.parseInt(etStarAmount.getText().toString());
                etStarAmount.setText(String.valueOf(currentAmount - 1));
            } catch (NumberFormatException e) {
                etStarAmount.setText("-1");
            }
        });

        ivIncrease.setOnClickListener(v -> {
            try {
                int currentAmount = Integer.parseInt(etStarAmount.getText().toString());
                etStarAmount.setText(String.valueOf(currentAmount + 1));
            } catch (NumberFormatException e) {
                etStarAmount.setText("1");
            }
        });

        // Quick amount buttons
        tvQuickAdd1.setOnClickListener(v -> etStarAmount.setText("1"));
        tvQuickAdd5.setOnClickListener(v -> etStarAmount.setText("5"));
        tvQuickRemove1.setOnClickListener(v -> etStarAmount.setText("-1"));
        tvQuickRemove5.setOnClickListener(v -> etStarAmount.setText("-5"));

        tvCancel.setOnClickListener(v -> dialog.dismiss());

        tvConfirm.setOnClickListener(v -> {
            String amountStr = etStarAmount.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (TextUtils.isEmpty(amountStr) || amountStr.equals("0")) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int amount = Integer.parseInt(amountStr);
                if (amount == 0) {
                    Toast.makeText(this, "Amount cannot be zero", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if balance would go negative
                if (currentBalance + amount < 0) {
                    Toast.makeText(this, "Balance cannot go below zero", Toast.LENGTH_SHORT).show();
                    return;
                }

                String description = TextUtils.isEmpty(reason) ?
                        "Balance Adjustment" : "Balance Adjustment: " + reason;

                adjustStarBalance(amount, description);
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showResetConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Star Balance?")
                .setMessage("This will reset " + childName + "'s star balance to 0 and clear all transaction history. This action cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> resetStarBalance())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Transaction History?")
                .setMessage("This will remove all transaction history for " + childName + ". The current star balance will remain unchanged.")
                .setPositiveButton("Clear", (dialog, which) -> clearTransactionHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFilterDialog() {
        String[] filterOptions = {"All Records", "Earned Stars", "Spent Stars", "Adjustments"};

        new AlertDialog.Builder(this)
                .setTitle("Filter Transactions")
                .setItems(filterOptions, (dialog, which) -> {
                    String selectedFilter = filterOptions[which];
                    tvFilterLabel.setText(selectedFilter);
                    applyFilter(selectedFilter);
                })
                .show();
    }

    private void applyFilter(String filter) {
        filteredTransactions.clear();

        switch (filter) {
            case "All Records":
                filteredTransactions.addAll(allTransactions);
                break;
            case "Earned Stars":
                for (StarTransaction transaction : allTransactions) {
                    if (transaction.getAmount() > 0) {
                        filteredTransactions.add(transaction);
                    }
                }
                break;
            case "Spent Stars":
                for (StarTransaction transaction : allTransactions) {
                    if (transaction.getAmount() < 0) {
                        filteredTransactions.add(transaction);
                    }
                }
                break;
            case "Adjustments":
                for (StarTransaction transaction : allTransactions) {
                    if ("adjustment".equals(transaction.getType()) || "reset".equals(transaction.getType())) {
                        filteredTransactions.add(transaction);
                    }
                }
                break;
        }

        transactionAdapter.notifyDataSetChanged();
    }

    private void adjustStarBalance(int amount, String description) {
        firebaseHelper.adjustChildStarBalance(childId, amount, description, new FirebaseHelper.OnStarBalanceUpdatedListener() {
            @Override
            public void onSuccess(int newBalance) {
                currentBalance = newBalance;
                tvStarsBalance.setText(String.valueOf(currentBalance));
                loadTransactionHistory(); // Refresh transaction history

                String message = amount > 0 ?
                        "Added " + amount + " stars" :
                        "Removed " + Math.abs(amount) + " stars";
                Toast.makeText(StarBalanceActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error adjusting balance: " + error);
                Toast.makeText(StarBalanceActivity.this, "Error adjusting balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetStarBalance() {
        firebaseHelper.resetChildStarBalance(childId, new FirebaseHelper.OnStarBalanceResetListener() {
            @Override
            public void onSuccess() {
                currentBalance = 0;
                tvStarsBalance.setText("0");
                loadTransactionHistory(); // Refresh transaction history
                Toast.makeText(StarBalanceActivity.this, "Star balance reset successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error resetting balance: " + error);
                Toast.makeText(StarBalanceActivity.this, "Error resetting balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTransactionHistory() {
        firebaseHelper.clearStarTransactionHistory(childId, new FirebaseHelper.OnTransactionHistoryClearedListener() {
            @Override
            public void onSuccess() {
                allTransactions.clear();
                filteredTransactions.clear();
                transactionAdapter.notifyDataSetChanged();
                Toast.makeText(StarBalanceActivity.this, "Transaction history cleared", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error clearing history: " + error);
                Toast.makeText(StarBalanceActivity.this, "Error clearing transaction history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}