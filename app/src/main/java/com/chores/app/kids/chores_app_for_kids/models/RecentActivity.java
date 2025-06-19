package com.chores.app.kids.chores_app_for_kids.models;

public class RecentActivity {
    private String userId;
    private String userName;
    private String description;
    private int starAmount;
    private long timestamp;
    private String type; // "earned", "spent", "adjustment"

    // Constructors
    public RecentActivity() {}

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStarAmount() { return starAmount; }
    public void setStarAmount(int starAmount) { this.starAmount = starAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}