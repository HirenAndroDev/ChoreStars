package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task {
    private String taskId;
    private String name;
    private String description;
    private String icon;
    private String familyId;
    private List<String> assignedKids;
    private int starsPerCompletion;
    private long startDate;
    private String repeatFrequency; // "daily", "weekly", "monthly"
    private String reminderTime;
    private boolean photoProofRequired;
    private boolean isActive;
    private String createdBy;
    private long createdAt;
    private long updatedAt;

    public Task() {
        this.assignedKids = new ArrayList<>();
    }

    public Task(String taskId, String name, String familyId, String createdBy) {
        this.taskId = taskId;
        this.name = name;
        this.familyId = familyId;
        this.createdBy = createdBy;
        this.assignedKids = new ArrayList<>();
        this.starsPerCompletion = 1;
        this.startDate = System.currentTimeMillis();
        this.repeatFrequency = "daily";
        this.photoProofRequired = false;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public List<String> getAssignedKids() { return assignedKids; }
    public void setAssignedKids(List<String> assignedKids) { this.assignedKids = assignedKids; }

    public int getStarsPerCompletion() { return starsPerCompletion; }
    public void setStarsPerCompletion(int starsPerCompletion) { this.starsPerCompletion = starsPerCompletion; }

    @PropertyName("startDate")
    public long getStartDate() { return startDate; }
    @PropertyName("startDate")
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public String getRepeatFrequency() { return repeatFrequency; }
    public void setRepeatFrequency(String repeatFrequency) { this.repeatFrequency = repeatFrequency; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public boolean isPhotoProofRequired() { return photoProofRequired; }
    public void setPhotoProofRequired(boolean photoProofRequired) { this.photoProofRequired = photoProofRequired; }

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
        result.put("taskId", taskId);
        result.put("name", name);
        result.put("description", description);
        result.put("icon", icon);
        result.put("familyId", familyId);
        result.put("assignedKids", assignedKids);
        result.put("starsPerCompletion", starsPerCompletion);
        result.put("startDate", startDate);
        result.put("repeatFrequency", repeatFrequency);
        result.put("reminderTime", reminderTime);
        result.put("photoProofRequired", photoProofRequired);
        result.put("isActive", isActive);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}