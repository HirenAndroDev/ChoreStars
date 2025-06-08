package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reward {
    private String rewardId;
    private String name;
    private String description;
    private String icon;
    private String familyId;
    private int starCost;
    private String category;
    private List<String> availableForKids;
    private String renewalPeriod; // "none", "daily", "weekly", "monthly"
    private boolean isActive;
    private String createdBy;
    private long createdAt;
    private long updatedAt;

    public Reward() {
        this.availableForKids = new ArrayList<>();
    }

    public Reward(String rewardId, String name, String familyId, String createdBy) {
        this.rewardId = rewardId;
        this.name = name;
        this.familyId = familyId;
        this.createdBy = createdBy;
        this.availableForKids = new ArrayList<>();
        this.starCost = 1;
        this.renewalPeriod = "none";
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public int getStarCost() { return starCost; }
    public void setStarCost(int starCost) { this.starCost = starCost; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getAvailableForKids() { return availableForKids; }
    public void setAvailableForKids(List<String> availableForKids) { this.availableForKids = availableForKids; }

    public String getRenewalPeriod() { return renewalPeriod; }
    public void setRenewalPeriod(String renewalPeriod) { this.renewalPeriod = renewalPeriod; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("updatedAt")
    public long getUpdatedAt() { return updatedAt; }
    @PropertyName("updatedAt")
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("rewardId", rewardId);
        result.put("name", name);
        result.put("description", description);
        result.put("icon", icon);
        result.put("familyId", familyId);
        result.put("starCost", starCost);
        result.put("category", category);
        result.put("availableForKids", availableForKids);
        result.put("renewalPeriod", renewalPeriod);
        result.put("isActive", isActive);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}