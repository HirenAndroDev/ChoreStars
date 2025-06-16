package com.chores.app.kids.chores_app_for_kids.models;

public class ChildProfile {
    private String childId;
    private String name;
    private String familyId;
    private String inviteCode;
    private long inviteCodeExpiry;
    private String profileImageUrl;
    private int starBalance;
    private boolean isActive;
    private long createdAt;

    public ChildProfile() {
    }

    public ChildProfile(String childId, String name, String familyId) {
        this.childId = childId;
        this.name = name;
        this.familyId = familyId;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public long getInviteCodeExpiry() {
        return inviteCodeExpiry;
    }

    public void setInviteCodeExpiry(long inviteCodeExpiry) {
        this.inviteCodeExpiry = inviteCodeExpiry;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getStarBalance() {
        return starBalance;
    }

    public void setStarBalance(int starBalance) {
        this.starBalance = starBalance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isInviteCodeValid() {
        return inviteCode != null && !inviteCode.isEmpty() &&
                inviteCodeExpiry > System.currentTimeMillis();
    }
}