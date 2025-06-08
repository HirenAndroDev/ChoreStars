package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class TaskCompletion {
    private String completionId;
    private String taskId;
    private String kidId;
    private String familyId;
    private long completedAt;
    private int starsEarned;
    private String photoProof;
    private String verifiedBy;
    private boolean isVerified;
    private String notes;

    public TaskCompletion() {
        // Default constructor required for Firestore
    }

    public TaskCompletion(String completionId, String taskId, String kidId, String familyId, int starsEarned) {
        this.completionId = completionId;
        this.taskId = taskId;
        this.kidId = kidId;
        this.familyId = familyId;
        this.starsEarned = starsEarned;
        this.completedAt = System.currentTimeMillis();
        this.isVerified = false;
    }

    // Getters and Setters
    public String getCompletionId() { return completionId; }
    public void setCompletionId(String completionId) { this.completionId = completionId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getKidId() { return kidId; }
    public void setKidId(String kidId) { this.kidId = kidId; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    @PropertyName("completedAt")
    public long getCompletedAt() { return completedAt; }
    @PropertyName("completedAt")
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public String getPhotoProof() { return photoProof; }
    public void setPhotoProof(String photoProof) { this.photoProof = photoProof; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("completionId", completionId);
        result.put("taskId", taskId);
        result.put("kidId", kidId);
        result.put("familyId", familyId);
        result.put("completedAt", completedAt);
        result.put("starsEarned", starsEarned);
        result.put("photoProof", photoProof);
        result.put("verifiedBy", verifiedBy);
        result.put("isVerified", isVerified);
        result.put("notes", notes);
        return result;
    }
}
