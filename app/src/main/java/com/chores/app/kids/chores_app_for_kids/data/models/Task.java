package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task model class
 */
@IgnoreExtraProperties
public class Task implements Serializable {

    private String taskId;
    private String taskName;
    private String notes;
    private String iconId;
    private int starsPerCompletion;
    private long startDate;
    private String repeat; // "once", "daily", "specific_days"
    private List<String> specificDays; // ["MON", "TUE", "WED", etc.]
    private String reminderTime; // "HH:mm" format
    private boolean requiresPhotoProof;
    private String familyId;
    private String createdBy;
    private List<String> assignedTo; // List of kid IDs
    private long createdAt;
    private boolean isActive;

    // Default constructor required for Firebase
    public Task() {
        specificDays = new ArrayList<>();
        assignedTo = new ArrayList<>();
        isActive = true;
    }

    public Task(String taskName, String familyId, String createdBy) {
        this.taskName = taskName;
        this.familyId = familyId;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.startDate = System.currentTimeMillis();
        this.repeat = "daily";
        this.starsPerCompletion = 1;
        this.requiresPhotoProof = false;
        this.isActive = true;
        this.specificDays = new ArrayList<>();
        this.assignedTo = new ArrayList<>();
    }

    // Check if task is due today
    @Exclude
    public boolean isDueToday() {
        if (!isActive) return false;

        long today = System.currentTimeMillis();
        if (today < startDate) return false;

        switch (repeat) {
            case "once":
                // Check if start date is today
                return isSameDay(startDate, today);

            case "daily":
                return true;

            case "specific_days":
                // Check if today is in specific days
                String todayDay = new java.text.SimpleDateFormat("EEE").format(new java.util.Date()).toUpperCase();
                return specificDays != null && specificDays.contains(todayDay);

            default:
                return false;
        }
    }

    @Exclude
    private boolean isSameDay(long date1, long date2) {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        return fmt.format(new java.util.Date(date1)).equals(fmt.format(new java.util.Date(date2)));
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("taskName", taskName);
        result.put("notes", notes);
        result.put("iconId", iconId);
        result.put("starsPerCompletion", starsPerCompletion);
        result.put("startDate", startDate);
        result.put("repeat", repeat);
        result.put("specificDays", specificDays);
        result.put("reminderTime", reminderTime);
        result.put("requiresPhotoProof", requiresPhotoProof);
        result.put("familyId", familyId);
        result.put("createdBy", createdBy);
        result.put("assignedTo", assignedTo);
        result.put("createdAt", createdAt);
        result.put("isActive", isActive);
        return result;
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public int getStarsPerCompletion() {
        return starsPerCompletion;
    }

    public void setStarsPerCompletion(int starsPerCompletion) {
        this.starsPerCompletion = starsPerCompletion;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public List<String> getSpecificDays() {
        return specificDays;
    }

    public void setSpecificDays(List<String> specificDays) {
        this.specificDays = specificDays;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    @PropertyName("requiresPhotoProof")
    public boolean isRequiresPhotoProof() {
        return requiresPhotoProof;
    }

    @PropertyName("requiresPhotoProof")
    public void setRequiresPhotoProof(boolean requiresPhotoProof) {
        this.requiresPhotoProof = requiresPhotoProof;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<String> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }
}
