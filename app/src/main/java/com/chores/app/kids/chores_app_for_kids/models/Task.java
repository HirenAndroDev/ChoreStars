package com.chores.app.kids.chores_app_for_kids.models;

import java.util.List;
import java.util.ArrayList;

public class Task {
    private String taskId;
    private String name;
    private String notes;
    private String iconName;
    private String iconUrl;
    private int starReward;
    private List<String> assignedKids;
    private long startDateTimestamp; // Use timestamp for better date handling
    private String repeatType; // "once", "everyday", "specific"
    private List<Integer> customDays; // For "specific" repeat type (1=Sunday, 2=Monday, etc.)
    private String reminderTime;
    private boolean photoProofRequired;
    private String status;
    private long createdTimestamp;
    private String familyId;
    private String createdBy;

    // Runtime completion status (not stored in Firebase)
    private boolean completed;

    public Task() {
        // Empty constructor required for Firestore
        this.assignedKids = new ArrayList<>();
        this.customDays = new ArrayList<>();
        this.status = "active";
        this.completed = false;
        this.repeatType = "everyday"; // Default
    }

    public Task(String name, String iconName, int starReward) {
        this.name = name;
        this.iconName = iconName;
        this.starReward = starReward;
        this.assignedKids = new ArrayList<>();
        this.customDays = new ArrayList<>();
        this.status = "active";
        this.createdTimestamp = System.currentTimeMillis();
        this.completed = false;
        this.repeatType = "everyday"; // Default
    }

    // Method to check if task should be shown on a specific date
    public boolean isScheduledForDate(long dateTimestamp) {
        // Normalize both timestamps to start of day for comparison
        long normalizedTaskStart = normalizeToStartOfDay(startDateTimestamp);
        long normalizedTargetDate = normalizeToStartOfDay(dateTimestamp);

        // If task hasn't started yet, don't show it
        if (normalizedTargetDate < normalizedTaskStart) {
            return false;
        }

        switch (repeatType) {
            case "once":
                // Show only on the start date
                return normalizedTargetDate == normalizedTaskStart;

            case "everyday":
                // Show every day from start date onwards
                return normalizedTargetDate >= normalizedTaskStart;

            case "specific":
                // Show only on specified days of the week, from start date onwards
                if (normalizedTargetDate < normalizedTaskStart) {
                    return false;
                }
                if (customDays == null || customDays.isEmpty()) {
                    return false;
                }

                // Get day of week for the given date
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(dateTimestamp);
                int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);

                return customDays.contains(dayOfWeek);

            default:
                return false;
        }
    }

    private long normalizeToStartOfDay(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case java.util.Calendar.SUNDAY:
                return "Sunday";
            case java.util.Calendar.MONDAY:
                return "Monday";
            case java.util.Calendar.TUESDAY:
                return "Tuesday";
            case java.util.Calendar.WEDNESDAY:
                return "Wednesday";
            case java.util.Calendar.THURSDAY:
                return "Thursday";
            case java.util.Calendar.FRIDAY:
                return "Friday";
            case java.util.Calendar.SATURDAY:
                return "Saturday";
            default:
                return "Unknown";
        }
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getStarReward() { return starReward; }
    public void setStarReward(int starReward) { this.starReward = starReward; }

    public List<String> getAssignedKids() { return assignedKids; }
    public void setAssignedKids(List<String> assignedKids) { this.assignedKids = assignedKids; }

    // Deprecated - use getStartDateTimestamp() instead
    public String getStartDate() {
        return startDateTimestamp > 0 ? String.valueOf(startDateTimestamp) : null;
    }

    // Deprecated - use setStartDateTimestamp() instead
    public void setStartDate(String startDate) {
        if (startDate != null && !startDate.isEmpty()) {
            try {
                this.startDateTimestamp = Long.parseLong(startDate);
            } catch (NumberFormatException e) {
                this.startDateTimestamp = System.currentTimeMillis();
            }
        }
    }

    public long getStartDateTimestamp() {
        return startDateTimestamp;
    }

    public void setStartDate(long startDateTimestamp) {
        this.startDateTimestamp = startDateTimestamp;
    }

    public void setStartDateTimestamp(long startDateTimestamp) {
        this.startDateTimestamp = startDateTimestamp;
    }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public List<Integer> getCustomDays() {
        return customDays;
    }

    public void setCustomDays(List<Integer> customDays) {
        this.customDays = customDays;
    }

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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
