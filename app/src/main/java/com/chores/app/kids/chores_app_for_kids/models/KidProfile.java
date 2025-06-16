package com.chores.app.kids.chores_app_for_kids.models;

public class KidProfile {
    private String kidId;
    private String name;
    private String familyId;
    private String profileImageUrl;
    private int starBalance;
    private String inviteCode;
    private long inviteCodeExpiry;
    private boolean isSelected;

    public KidProfile() {
    }

    public KidProfile(String kidId, String name, String familyId) {
        this.kidId = kidId;
        this.name = name;
        this.familyId = familyId;
        this.starBalance = 0;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getKidId() {
        return kidId;
    }

    public void setKidId(String kidId) {
        this.kidId = kidId;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}