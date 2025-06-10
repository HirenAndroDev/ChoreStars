package com.chores.app.kids.chores_app_for_kids.models;

import java.util.List;
import java.util.ArrayList;

public class Task {
    private String taskId;
    private String name;
    private String iconName;
    private int starReward;
    private List<String> assignedKids;
    private String startDate;
    private String repeatType;
    private String reminderTime;
    private boolean photoProofRequired;
    private String status;
    private long createdTimestamp;
    private String familyId;
    private String createdBy;

    public Task() {
        // Empty constructor required for Firestore
        this.assignedKids = new ArrayList<>();
        this.status = "pending";
    }

    public Task(String name, String iconName, int starReward) {
        this.name = name;
        this.iconName = iconName;
        this.starReward = starReward;
        this.assignedKids = new ArrayList<>();
        this.status = "pending";
        this.createdTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getStarReward() { return starReward; }
    public void setStarReward(int starReward) { this.starReward = starReward; }

    public List<String> getAssignedKids() { return assignedKids; }
    public void setAssignedKids(List<String> assignedKids) { this.assignedKids = assignedKids; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public boolean isPhotoProofRequired() { return photoProofRequired; }
    public void setPhotoProofRequired(boolean photoProofRequired) { this.photoProofRequired = photoProofRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(long createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

