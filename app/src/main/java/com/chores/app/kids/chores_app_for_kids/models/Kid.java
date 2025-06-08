package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class Kid {
    private String kidId;
    private String name;
    private String familyId;
    private String profileImage;
    private int starBalance;
    private int totalStarsEarned;
    private int totalRewardsRedeemed;
    private boolean textToSpeechEnabled;
    private long createdAt;
    private long updatedAt;

    public Kid() {
        // Default constructor required for Firestore
    }

    public Kid(String kidId, String name, String familyId) {
        this.kidId = kidId;
        this.name = name;
        this.familyId = familyId;
        this.starBalance = 0;
        this.totalStarsEarned = 0;
        this.totalRewardsRedeemed = 0;
        this.textToSpeechEnabled = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getKidId() { return kidId; }
    public void setKidId(String kidId) { this.kidId = kidId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public int getStarBalance() { return starBalance; }
    public void setStarBalance(int starBalance) { this.starBalance = starBalance; }

    public int getTotalStarsEarned() { return totalStarsEarned; }
    public void setTotalStarsEarned(int totalStarsEarned) { this.totalStarsEarned = totalStarsEarned; }

    public int getTotalRewardsRedeemed() { return totalRewardsRedeemed; }
    public void setTotalRewardsRedeemed(int totalRewardsRedeemed) { this.totalRewardsRedeemed = totalRewardsRedeemed; }

    public boolean isTextToSpeechEnabled() { return textToSpeechEnabled; }
    public void setTextToSpeechEnabled(boolean textToSpeechEnabled) { this.textToSpeechEnabled = textToSpeechEnabled; }

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
        result.put("kidId", kidId);
        result.put("name", name);
        result.put("familyId", familyId);
        result.put("profileImage", profileImage);
        result.put("starBalance", starBalance);
        result.put("totalStarsEarned", totalStarsEarned);
        result.put("totalRewardsRedeemed", totalRewardsRedeemed);
        result.put("textToSpeechEnabled", textToSpeechEnabled);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}