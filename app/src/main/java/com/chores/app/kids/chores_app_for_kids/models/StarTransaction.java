package com.chores.app.kids.chores_app_for_kids.models;

public class StarTransaction {
    private String transactionId;
    private String userId;
    private String type; // "earned", "spent", "adjustment"
    private int amount;
    private String description;
    private long timestamp;
    private String relatedTaskId;
    private String relatedRewardId;
    private String familyId;

    // Add these new fields
    private int balanceBefore;
    private int balanceAfter;


    public StarTransaction() {
        // Empty constructor required for Firestore
    }

    public StarTransaction(String userId, String type, int amount, String description, String familyId) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.familyId = familyId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters

    public int getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(int balanceBefore) { this.balanceBefore = balanceBefore; }

    public int getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }


    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRelatedTaskId() { return relatedTaskId; }
    public void setRelatedTaskId(String relatedTaskId) { this.relatedTaskId = relatedTaskId; }

    public String getRelatedRewardId() { return relatedRewardId; }
    public void setRelatedRewardId(String relatedRewardId) { this.relatedRewardId = relatedRewardId; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
}
