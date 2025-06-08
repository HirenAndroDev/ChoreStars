package com.chores.app.kids.chores_app_for_kids.models;

import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Family {
    private String familyId;
    private String ownerId;
    private String familyName;
    private String inviteCode;
    private List<String> members;
    private List<String> kids;
    private long createdAt;
    private long updatedAt;

    public Family() {
        this.members = new ArrayList<>();
        this.kids = new ArrayList<>();
    }

    public Family(String familyId, String ownerId, String familyName) {
        this.familyId = familyId;
        this.ownerId = ownerId;
        this.familyName = familyName;
        this.members = new ArrayList<>();
        this.kids = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public List<String> getKids() { return kids; }
    public void setKids(List<String> kids) { this.kids = kids; }

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
        result.put("familyId", familyId);
        result.put("ownerId", ownerId);
        result.put("familyName", familyName);
        result.put("inviteCode", inviteCode);
        result.put("members", members);
        result.put("kids", kids);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}

