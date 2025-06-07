package com.chores.app.kids.chores_app_for_kids.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification model class
 */
@IgnoreExtraProperties
public class Notification implements Serializable {

    private String notificationId;
    private String userId;
    private String type; // "task_reminder", "task_completed", "reward_redeemed", "new_task", "reward_approved"
    private String title;
    private String message;
    private Map<String, Object> data; // Additional data for the notification
    private boolean isRead;
    private long createdAt;
    private String actionType; // "open_task", "open_reward", "open_profile", etc.
    private String actionId; // ID of the related entity (taskId, rewardId, etc.)

    // Default constructor required for Firebase
    public Notification() {
        data = new HashMap<>();
        isRead = false;
    }

    // Constructor for task reminder
    public static Notification createTaskReminder(String userId, String taskName, String taskId) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.type = "task_reminder";
        notification.title = "Task Reminder";
        notification.message = "Don't forget to complete: " + taskName;
        notification.actionType = "open_task";
        notification.actionId = taskId;
        notification.createdAt = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("taskName", taskName);
        notification.data = data;

        return notification;
    }

    // Constructor for task completion
    public static Notification createTaskCompletion(String parentId, String kidName,
                                                    String taskName, String taskId) {
        Notification notification = new Notification();
        notification.userId = parentId;
        notification.type = "task_completed";
        notification.title = "Task Completed";
        notification.message = kidName + " completed: " + taskName;
        notification.actionType = "open_task";
        notification.actionId = taskId;
        notification.createdAt = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("taskName", taskName);
        data.put("kidName", kidName);
        notification.data = data;

        return notification;
    }

    // Constructor for reward redemption
    public static Notification createRewardRedemption(String parentId, String kidName,
                                                      String rewardName, String rewardId) {
        Notification notification = new Notification();
        notification.userId = parentId;
        notification.type = "reward_redeemed";
        notification.title = "Reward Redeemed";
        notification.message = kidName + " wants to redeem: " + rewardName;
        notification.actionType = "open_reward";
        notification.actionId = rewardId;
        notification.createdAt = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("rewardId", rewardId);
        data.put("rewardName", rewardName);
        data.put("kidName", kidName);
        notification.data = data;

        return notification;
    }

    // Convert to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("notificationId", notificationId);
        result.put("userId", userId);
        result.put("type", type);
        result.put("title", title);
        result.put("message", message);
        result.put("data", data);
        result.put("isRead", isRead);
        result.put("createdAt", createdAt);
        result.put("actionType", actionType);
        result.put("actionId", actionId);
        return result;
    }

    // Get formatted time ago
    @Exclude
    public String getTimeAgo() {
        long timeDiff = System.currentTimeMillis() - createdAt;
        long seconds = timeDiff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @PropertyName("isRead")
    public boolean isRead() {
        return isRead;
    }

    @PropertyName("isRead")
    public void setRead(boolean read) {
        isRead = read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
}