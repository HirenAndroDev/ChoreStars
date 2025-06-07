package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Task Completion model class
 */
@IgnoreExtraProperties
public class TaskCompletion implements Serializable {

    private String completionId;
    private String taskId;
    private String kidId;
    private long completedAt;
    private String photoProofUrl;
    private String verifiedBy; // Parent ID who verified
    private int starsAwarded;
    private String date; // YYYY-MM-DD format
    private String taskName; // For quick reference

    // Default constructor required for Firebase
    public TaskCompletion() {
    }

    public TaskCompletion(String taskId, String kidId, int starsAwarded, String taskName) {
        this.taskId = taskId;
        this.kidId = kidId;
        this.starsAwarded = starsAwarded;
        this.taskName = taskName;
        this.completedAt = System.currentTimeMillis();
        this.date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("completionId", completionId);
        result.put("taskId", taskId);
        result.put("kidId", kidId);
        result.put("completedAt", completedAt);
        result.put("photoProofUrl", photoProofUrl);
        result.put("verifiedBy", verifiedBy);
        result.put("starsAwarded", starsAwarded);
        result.put("date", date);
        result.put("taskName", taskName);
        return result;
    }

    // Getters and Setters
    public String getCompletionId() {
        return completionId;
    }

    public void setCompletionId(String completionId) {
        this.completionId = completionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getKidId() {
        return kidId;
    }

    public void setKidId(String kidId) {
        this.kidId = kidId;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public String getPhotoProofUrl() {
        return photoProofUrl;
    }

    public void setPhotoProofUrl(String photoProofUrl) {
        this.photoProofUrl = photoProofUrl;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public int getStarsAwarded() {
        return starsAwarded;
    }

    public void setStarsAwarded(int starsAwarded) {
        this.starsAwarded = starsAwarded;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}