package com.chores.app.kids.chores_app_for_kids.models;

public class DashboardStats {
    private int totalTasksToday;
    private int completedTasksToday;
    private int totalStarsEarned;
    private int activeChildren;
    private int tasksCompletedThisWeek;
    private int starsEarnedThisWeek;
    private int totalStarBalance;
    private int childCount;

    // Constructors
    public DashboardStats() {}

    // Getters and Setters
    public int getTotalTasksToday() { return totalTasksToday; }
    public void setTotalTasksToday(int totalTasksToday) { this.totalTasksToday = totalTasksToday; }

    public int getCompletedTasksToday() { return completedTasksToday; }
    public void setCompletedTasksToday(int completedTasksToday) { this.completedTasksToday = completedTasksToday; }

    public int getTotalStarsEarned() { return totalStarsEarned; }
    public void setTotalStarsEarned(int totalStarsEarned) { this.totalStarsEarned = totalStarsEarned; }

    public int getActiveChildren() { return activeChildren; }
    public void setActiveChildren(int activeChildren) { this.activeChildren = activeChildren; }

    public int getTasksCompletedThisWeek() { return tasksCompletedThisWeek; }
    public void setTasksCompletedThisWeek(int tasksCompletedThisWeek) { this.tasksCompletedThisWeek = tasksCompletedThisWeek; }

    public int getStarsEarnedThisWeek() { return starsEarnedThisWeek; }
    public void setStarsEarnedThisWeek(int starsEarnedThisWeek) { this.starsEarnedThisWeek = starsEarnedThisWeek; }

    public int getTotalStarBalance() { return totalStarBalance; }
    public void setTotalStarBalance(int totalStarBalance) { this.totalStarBalance = totalStarBalance; }

    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }
}