package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class RewardRedemption {
    private String redemptionId;
    private String rewardId;
    private String kidId;
    private String familyId;
    private int starsSpent;
    private long redeemedAt;
    private String approvedBy;
    private boolean isApproved;
    private long approvedAt;
    private String status; // "pending", "approved", "rejected"

    public RewardRedemption() {
        // Default constructor required for Firestore
    }

    public RewardRedemption(String redemptionId, String rewardId, String kidId, String familyId, int starsSpent) {
        this.redemptionId = redemptionId;
        this.rewardId = rewardId;
        this.kidId = kidId;
        this.familyId = familyId;
        this.starsSpent = starsSpent;
        this.redeemedAt = System.currentTimeMillis();
        this.isApproved = false;
        this.status = "pending";
    }

    // Getters and Setters
    public String getRedemptionId() { return redemptionId; }
    public void setRedemptionId(String redemptionId) { this.redemptionId = redemptionId; }

    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public String getKidId() { return kidId; }
    public void setKidId(String kidId) { this.kidId = kidId; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public int getStarsSpent() { return starsSpent; }
    public void setStarsSpent(int starsSpent) { this.starsSpent = starsSpent; }

    @PropertyName("redeemedAt")
    public long getRedeemedAt() { return redeemedAt; }
    @PropertyName("redeemedAt")
    public void setRedeemedAt(long redeemedAt) { this.redeemedAt = redeemedAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    @PropertyName("approvedAt")
    public long getApprovedAt() { return approvedAt; }
    @PropertyName("approvedAt")
    public void setApprovedAt(long approvedAt) { this.approvedAt = approvedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("redemptionId", redemptionId);
        result.put("rewardId", rewardId);
        result.put("kidId", kidId);
        result.put("familyId", familyId);
        result.put("starsSpent", starsSpent);
        result.put("redeemedAt", redeemedAt);
        result.put("approvedBy", approvedBy);
        result.put("isApproved", isApproved);
        result.put("approvedAt", approvedAt);
        result.put("status", status);
        return result;
    }
}
