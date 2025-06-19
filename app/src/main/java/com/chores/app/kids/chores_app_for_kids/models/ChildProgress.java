package com.chores.app.kids.chores_app_for_kids.models;

public class ChildProgress {
    private String childId;
    private String childName;
    private String profileImageUrl;
    private int totalTasks;
    private int completedTasks;
    private int totalStars;
    private int earnedStarsToday;
    private int progressPercentage;

    // Constructors
    public ChildProgress() {}

    public ChildProgress(String childId, String childName, String profileImageUrl,
                         int totalTasks, int completedTasks, int totalStars, int earnedStarsToday) {
        this.childId = childId;
        this.childName = childName;
        this.profileImageUrl = profileImageUrl;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.totalStars = totalStars;
        this.earnedStarsToday = earnedStarsToday;
        this.progressPercentage = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
    }

    // Getters and Setters
    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }

    public int getEarnedStarsToday() { return earnedStarsToday; }
    public void setEarnedStarsToday(int earnedStarsToday) { this.earnedStarsToday = earnedStarsToday; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
}