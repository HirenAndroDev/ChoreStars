package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User model class for both parents and kids
 */
@IgnoreExtraProperties
public class User implements Serializable {

    private String userId;
    private String email;
    private String name;
    private String role; // "parent" or "kid"
    private String familyId;
    private String profileImage;
    private long createdAt;
    private String googleId;
    private String parentId; // for kids only
    private int starWallet; // for kids only
    private boolean notificationsEnabled; // for parents
    private String fcmToken;
    private boolean isOnline;
    private long lastSeenAt;

    // Default constructor required for Firebase
    public User() {
    }


    // Constructor for Parent
    public User(String userId, String email, String name, String familyId) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = "parent";
        this.familyId = familyId;
        this.createdAt = System.currentTimeMillis();
        this.notificationsEnabled = true;
        this.starWallet = 0;
    }

    // Constructor for Kid
    public User(String userId, String name, String familyId, String parentId) {
        this.userId = userId;
        this.name = name;
        this.role = "kid";
        this.familyId = familyId;
        this.parentId = parentId;
        this.createdAt = System.currentTimeMillis();
        this.starWallet = 0;
        this.notificationsEnabled = false;
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("email", email);
        result.put("name", name);
        result.put("role", role);
        result.put("familyId", familyId);
        result.put("profileImage", profileImage);
        result.put("createdAt", createdAt);
        result.put("googleId", googleId);
        result.put("parentId", parentId);
        result.put("starWallet", starWallet);
        result.put("notificationsEnabled", notificationsEnabled);
        result.put("fcmToken", fcmToken);
        result.put("isOnline", isOnline);
        result.put("lastSeenAt", lastSeenAt);
        return result;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getStarWallet() {
        return starWallet;
    }

    public void setStarWallet(int starWallet) {
        this.starWallet = starWallet;
    }

    @PropertyName("notificationsEnabled")
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    @PropertyName("notificationsEnabled")
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @PropertyName("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    @PropertyName("isOnline")
    public void setOnline(boolean online) {
        isOnline = online;
    }

    public long getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(long lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    @Exclude
    public boolean isParent() {
        return "parent".equals(role);
    }

    @Exclude
    public boolean isKid() {
        return "kid".equals(role);
    }
}