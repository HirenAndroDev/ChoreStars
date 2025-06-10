package com.chores.app.kids.chores_app_for_kids.models;

import java.util.List;
import java.util.ArrayList;

public class Family {
    private String familyId;
    private String ownerId;
    private List<String> parentIds;
    private List<String> childIds;
    private String inviteCode;
    private long inviteCodeExpiry;

    public Family() {
        // Empty constructor required for Firestore
        this.parentIds = new ArrayList<>();
        this.childIds = new ArrayList<>();
    }

    public Family(String familyId, String ownerId) {
        this.familyId = familyId;
        this.ownerId = ownerId;
        this.parentIds = new ArrayList<>();
        this.childIds = new ArrayList<>();
        this.parentIds.add(ownerId);
    }

    // Getters and Setters
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getParentIds() { return parentIds; }
    public void setParentIds(List<String> parentIds) { this.parentIds = parentIds; }

    public List<String> getChildIds() { return childIds; }
    public void setChildIds(List<String> childIds) { this.childIds = childIds; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public long getInviteCodeExpiry() { return inviteCodeExpiry; }
    public void setInviteCodeExpiry(long inviteCodeExpiry) { this.inviteCodeExpiry = inviteCodeExpiry; }
}
