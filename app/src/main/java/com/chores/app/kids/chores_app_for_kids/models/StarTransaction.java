package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class StarTransaction {
    private String transactionId;
    private String kidId;
    private String familyId;
    private String type; // "earned", "spent", "adjustment"
    private int amount;
    private String reason;
    private String relatedId; // taskId or rewardId
    private String createdBy;
    private long createdAt;

    public StarTransaction() {
        // Default constructor required for Firestore
    }

    public StarTransaction(String transactionId, String kidId, String familyId, String type, int amount, String reason) {
        this.transactionId = transactionId;
        this.kidId = kidId;
        this.familyId = familyId;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getKidId() { return kidId; }
    public void setKidId(String kidId) { this.kidId = kidId; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", transactionId);
        result.put("kidId", kidId);
        result.put("familyId", familyId);
        result.put("type", type);
        result.put("amount", amount);
        result.put("reason", reason);
        result.put("relatedId", relatedId);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        return result;
    }
}