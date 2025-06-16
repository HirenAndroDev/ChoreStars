package com.chores.app.kids.chores_app_for_kids.models;

public class TaskIcon {
    private String id;
    private String name;
    private String iconUrl;
    private String category;
    private boolean isDefault;
    private long createdTimestamp;
    private String drawableName; // For default drawable icons

    public TaskIcon() {
        // Default constructor required for calls to DataSnapshot.getValue(TaskIcon.class)
    }

    public TaskIcon(String name, String iconUrl, String category, boolean isDefault) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.category = category;
        this.isDefault = isDefault;
        this.createdTimestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getDrawableName() {
        return drawableName;
    }

    public void setDrawableName(String drawableName) {
        this.drawableName = drawableName;
    }
}
