package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Reward Redemption model class
 */
@IgnoreExtraProperties
public class RewardRedemption implements Serializable {

    private String redemptionId;
    private String rewardId;
    private String kidId;
    private long redeemedAt;
    private int starsUsed;
    private String approvedBy; // Parent ID who approved
    private String rewardName; // For quick reference
    private String kidName; // For quick reference
    private String status; // "pending", "approved", "rejected"

    // Default constructor required for Firebase
    public RewardRedemption() {
        status = "pending";
    }

    public RewardRedemption(String rewardId, String kidId, int starsUsed,
                            String rewardName, String kidName) {
        this.rewardId = rewardId;
        this.kidId = kidId;
        this.starsUsed = starsUsed;
        this.rewardName = rewardName;
        this.kidName = kidName;
        this.redeemedAt = System.currentTimeMillis();
        this.status = "pending";
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("redemptionId", redemptionId);
        result.put("rewardId", rewardId);
        result.put("kidId", kidId);
        result.put("redeemedAt", redeemedAt);
        result.put("starsUsed", starsUsed);
        result.put("approvedBy", approvedBy);
        result.put("rewardName", rewardName);
        result.put("kidName", kidName);
        result.put("status", status);
        return result;
    }

    // Getters and Setters
    public String getRedemptionId() {
        return redemptionId;
    }

    public void setRedemptionId(String redemptionId) {
        this.redemptionId = redemptionId;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getKidId() {
        return kidId;
    }

    public void setKidId(String kidId) {
        this.kidId = kidId;
    }

    public long getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(long redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public int getStarsUsed() {
        return starsUsed;
    }

    public void setStarsUsed(int starsUsed) {
        this.starsUsed = starsUsed;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getKidName() {
        return kidName;
    }

    public void setKidName(String kidName) {
        this.kidName = kidName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}