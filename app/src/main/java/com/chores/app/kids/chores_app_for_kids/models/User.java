package com.chores.app.kids.chores_app_for_kids.models;

import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String role;
    private String familyId;
    private String profileImageUrl;
    private boolean textToSpeechEnabled;
    private int starBalance;
    private String inviteCode;
    private Long inviteCodeExpires;

    public User() {
        // Empty constructor required for Firestore
    }

    public User(String userId, String name, String email, String role, String familyId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.familyId = familyId;
        this.starBalance = 0;
        this.textToSpeechEnabled = false;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public boolean isTextToSpeechEnabled() { return textToSpeechEnabled; }
    public void setTextToSpeechEnabled(boolean textToSpeechEnabled) { this.textToSpeechEnabled = textToSpeechEnabled; }

    public int getStarBalance() { return starBalance; }
    public void setStarBalance(int starBalance) { this.starBalance = starBalance; }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Long getInviteCodeExpires() {
        return inviteCodeExpires;
    }

    public void setInviteCodeExpires(Long inviteCodeExpires) {
        this.inviteCodeExpires = inviteCodeExpires;
    }
}
