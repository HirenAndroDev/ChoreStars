package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Family model class
 */
@IgnoreExtraProperties
public class Family implements Serializable {

    private String familyId;
    private String familyName;
    private long createdAt;
    private String ownerId;
    private String adultInviteCode;
    private Map<String, String> kidInviteCodes;
    private Map<String, Boolean> members; // userId -> true

    // Default constructor required for Firebase
    public Family() {
        kidInviteCodes = new HashMap<>();
        members = new HashMap<>();
    }

    public Family(String familyId, String familyName, String ownerId) {
        this.familyId = familyId;
        this.familyName = familyName;
        this.ownerId = ownerId;
        this.createdAt = System.currentTimeMillis();
        this.adultInviteCode = generateInviteCode();
        this.kidInviteCodes = new HashMap<>();
        this.members = new HashMap<>();
        this.members.put(ownerId, true);
    }

    // Generate random 6-digit invite code
    @Exclude
    private String generateInviteCode() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    // Add kid invite code
    @Exclude
    public void addKidInviteCode(String kidId) {
        if (kidInviteCodes == null) {
            kidInviteCodes = new HashMap<>();
        }
        kidInviteCodes.put(kidId, generateInviteCode());
    }

    // Add member to family
    @Exclude
    public void addMember(String userId) {
        if (members == null) {
            members = new HashMap<>();
        }
        members.put(userId, true);
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("familyId", familyId);
        result.put("familyName", familyName);
        result.put("createdAt", createdAt);
        result.put("ownerId", ownerId);
        result.put("adultInviteCode", adultInviteCode);
        result.put("kidInviteCodes", kidInviteCodes);
        result.put("members", members);
        return result;
    }

    // Getters and Setters
    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getAdultInviteCode() {
        return adultInviteCode;
    }

    public void setAdultInviteCode(String adultInviteCode) {
        this.adultInviteCode = adultInviteCode;
    }

    public Map<String, String> getKidInviteCodes() {
        return kidInviteCodes;
    }

    public void setKidInviteCodes(Map<String, String> kidInviteCodes) {
        this.kidInviteCodes = kidInviteCodes;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }
}