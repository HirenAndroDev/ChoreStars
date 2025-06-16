package com.chores.app.kids.chores_app_for_kids.models;

public class PreReward {
    private String id;
    private String name;
    private String iconName;
    private String iconUrl;
    private int starCost;

    public PreReward() {
        // Empty constructor required for Firestore
    }

    public PreReward(String id, String name, String iconName, int starCost) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.starCost = starCost;
    }

    public PreReward(String id, String name, String iconName, String iconUrl, int starCost) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.iconUrl = iconUrl;
        this.starCost = starCost;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
