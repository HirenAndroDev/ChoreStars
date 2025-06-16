package com.chores.app.kids.chores_app_for_kids.models;

import java.util.Date;

public class RedeemedReward {
    private String redeemedRewardId;
    private String rewardId;
    private String rewardName;
    private String iconName;
    private String iconUrl;
    private int starCost;
    private String childId;
    private String childName;
    private String familyId;
    private Date redeemedAt;
    private long timestamp;

    public RedeemedReward() {
        // Empty constructor required for Firestore
    }

    public RedeemedReward(Reward reward, String childId, String childName) {
        this.rewardId = reward.getRewardId();
        this.rewardName = reward.getName();
        this.iconName = reward.getIconName();
        this.iconUrl = reward.getIconUrl();
        this.starCost = reward.getStarCost();
        this.childId = childId;
        this.childName = childName;
        this.familyId = reward.getFamilyId();
        this.redeemedAt = new Date();
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRedeemedRewardId() {
        return redeemedRewardId;
    }

    public void setRedeemedRewardId(String redeemedRewardId) {
        this.redeemedRewardId = redeemedRewardId;
    }

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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getStarCost() {
        return starCost;
    }

    public void setStarCost(int starCost) {
        this.starCost = starCost;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public Date getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(Date redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}