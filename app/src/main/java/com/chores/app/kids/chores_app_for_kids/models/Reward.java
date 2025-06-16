package com.chores.app.kids.chores_app_for_kids.models;

import java.util.List;
import java.util.ArrayList;

public class Reward {
    private String rewardId;
    private String name;
    private String iconName;
    private String iconUrl;
    private int starCost;
    private List<String> availableForKids;
    private String renewalPeriod;
    private boolean isCustom;
    private String familyId;

    public Reward() {
        // Empty constructor required for Firestore
        this.availableForKids = new ArrayList<>();
    }

    public Reward(String name, String iconName, int starCost, String familyId) {
        this.name = name;
        this.iconName = iconName;
        this.starCost = starCost;
        this.familyId = familyId;
        this.availableForKids = new ArrayList<>();
        this.isCustom = false;
    }

    // Getters and Setters
    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getStarCost() { return starCost; }
    public void setStarCost(int starCost) { this.starCost = starCost; }

    public List<String> getAvailableForKids() { return availableForKids; }
    public void setAvailableForKids(List<String> availableForKids) { this.availableForKids = availableForKids; }

    public String getRenewalPeriod() { return renewalPeriod; }
    public void setRenewalPeriod(String renewalPeriod) { this.renewalPeriod = renewalPeriod; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
}
