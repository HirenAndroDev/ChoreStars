package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role; // "parent" or "kid"
    private String familyId;
    private String profileImage;
    private long createdAt;
    private long lastLoginAt;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("lastLoginAt")
    public long getLastLoginAt() { return lastLoginAt; }
    @PropertyName("lastLoginAt")
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("email", email);
        result.put("name", name);
        result.put("role", role);
        result.put("familyId", familyId);
        result.put("profileImage", profileImage);
        result.put("createdAt", createdAt);
        result.put("lastLoginAt", lastLoginAt);
        return result;
    }
}