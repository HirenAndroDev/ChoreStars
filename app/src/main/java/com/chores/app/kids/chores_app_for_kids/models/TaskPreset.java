package com.chores.app.kids.chores_app_for_kids.models;

public class TaskPreset {
    private String id;
    private String name;
    private String iconUrl;
    private int starReward;
    private String description;
    private long createdTimestamp;

    public TaskPreset() {
        // Default constructor required for calls to DataSnapshot.getValue(TaskPreset.class)
    }

    public TaskPreset(String name, String iconUrl, int starReward, String description) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.starReward = starReward;
        this.description = description;
        this.createdTimestamp = System.currentTimeMillis();
    }

    // Getters and setters
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getStarReward() {
        return starReward;
    }

    public void setStarReward(int starReward) {
        this.starReward = starReward;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}