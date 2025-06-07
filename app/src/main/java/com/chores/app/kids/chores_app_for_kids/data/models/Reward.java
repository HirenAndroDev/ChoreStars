package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reward model class
 */
@IgnoreExtraProperties
public class Reward implements Serializable {

    private String rewardId;
    private String rewardName;
    private String iconId;
    private int starsRequired;
    private String renewalPeriod; // "none", "weekly", "monthly"
    private String familyId;
    private List<String> assignedTo; // List of kid IDs
    private String createdBy;
    private long createdAt;
    private boolean isCustom;
    private boolean isActive;

    // Default constructor required for Firebase
    public Reward() {
        assignedTo = new ArrayList<>();
        isActive = true;
    }

    public Reward(String rewardName, int starsRequired, String familyId, String createdBy) {
        this.rewardName = rewardName;
        this.starsRequired = starsRequired;
        this.familyId = familyId;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.renewalPeriod = "none";
        this.isCustom = true;
        this.isActive = true;
        this.assignedTo = new ArrayList<>();
    }

    // Check if reward can be redeemed by kid
    @Exclude
    public boolean canRedeem(int kidStarWallet) {
        return isActive && kidStarWallet >= starsRequired;
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("rewardId", rewardId);
        result.put("rewardName", rewardName);
        result.put("iconId", iconId);
        result.put("starsRequired", starsRequired);
        result.put("renewalPeriod", renewalPeriod);
        result.put("familyId", familyId);
        result.put("assignedTo", assignedTo);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        result.put("isCustom", isCustom);
        result.put("isActive", isActive);
        return result;
    }

    // Getters and Setters
    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public int getStarsRequired() {
        return starsRequired;
    }

    public void setStarsRequired(int starsRequired) {
        this.starsRequired = starsRequired;
    }

    public String getRenewalPeriod() {
        return renewalPeriod;
    }

    public void setRenewalPeriod(String renewalPeriod) {
        this.renewalPeriod = renewalPeriod;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public List<String> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<String> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("isCustom")
    public boolean isCustom() {
        return isCustom;
    }

    @PropertyName("isCustom")
    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }
}
