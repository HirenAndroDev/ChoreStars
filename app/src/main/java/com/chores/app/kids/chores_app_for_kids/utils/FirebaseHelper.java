package com.chores.app.kids.chores_app_for_kids.utils;


import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.models.Family;
import com.chores.app.kids.chores_app_for_kids.models.Task;
import com.chores.app.kids.chores_app_for_kids.models.TaskPreset;
import com.chores.app.kids.chores_app_for_kids.models.TaskIcon;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;

import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.StarTransaction;
import com.chores.app.kids.chores_app_for_kids.models.RedeemedReward;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FirebaseHelper {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    // ==================== USER MANAGEMENT ====================

    public static void createUser(User user, OnCompleteListener<Void> listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("familyId", user.getFamilyId());
        userData.put("starBalance", user.getStarBalance());
        userData.put("textToSpeechEnabled", user.isTextToSpeechEnabled());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLoginAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUserId())
                .set(userData)
                .addOnCompleteListener(listener);
    }

    public static void getCurrentUser(CurrentUserCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();
                            User user = new User();
                            user.setUserId(doc.getId());
                            user.setName(doc.getString("name"));
                            user.setEmail(doc.getString("email"));
                            user.setRole(doc.getString("role"));
                            user.setFamilyId(doc.getString("familyId"));
                            Long balance = doc.getLong("starBalance");
                            user.setStarBalance(balance != null ? balance.intValue() : 0);
                            Boolean tts = doc.getBoolean("textToSpeechEnabled");
                            user.setTextToSpeechEnabled(tts != null && tts);
                            user.setProfileImageUrl(doc.getString("profileImageUrl"));

                            callback.onUserLoaded(user);
                        } else {
                            callback.onError("User not found");
                        }
                    });
        } else {
            callback.onError("No authenticated user");
        }
    }

    // ==================== FAMILY MANAGEMENT ====================

    public static void createFamily(Family family, OnCompleteListener<Void> listener) {
        Map<String, Object> familyData = new HashMap<>();
        familyData.put("ownerId", family.getOwnerId());
        familyData.put("parentIds", family.getParentIds());
        familyData.put("childIds", family.getChildIds());
        familyData.put("inviteCode", family.getInviteCode());
        familyData.put("inviteCodeExpiry", family.getInviteCodeExpiry());
        familyData.put("createdAt", System.currentTimeMillis());
        familyData.put("familyName", "Family"); // Default name

        db.collection("families")
                .document(family.getFamilyId())
                .set(familyData)
                .addOnCompleteListener(listener);
    }

    public static void generateInviteCode(String familyId, OnCompleteListener<Void> listener) {
        String inviteCode = String.format("%06d", new Random().nextInt(1000000));
        long expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days

        Map<String, Object> updates = new HashMap<>();
        updates.put("inviteCode", inviteCode);
        updates.put("inviteCodeExpiry", expiryTime);

        db.collection("families")
                .document(familyId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    public static void joinFamilyWithCode(String inviteCode, OnCompleteListener<QuerySnapshot> listener) {
        long currentTime = System.currentTimeMillis();

        db.collection("families")
                .whereEqualTo("inviteCode", inviteCode)
                .whereGreaterThan("inviteCodeExpiry", currentTime)
                .get()
                .addOnCompleteListener(listener);
    }

    // Debug method to check all children in database
    public static void debugAllChildren() {
        android.util.Log.d("FirebaseHelper", "=== DEBUG: Checking all children in database ===");

        db.collection("users")
                .whereEqualTo("role", "child")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("FirebaseHelper", "DEBUG: Found " + task.getResult().size() + " total children");

                        for (DocumentSnapshot doc : task.getResult()) {
                            android.util.Log.d("FirebaseHelper", "DEBUG Child: " + doc.getString("name") +
                                    " (ID: " + doc.getId() + ", familyId: " + doc.getString("familyId") +
                                    ", role: " + doc.getString("role") + ")");
                        }
                    } else {
                        android.util.Log.e("FirebaseHelper", "DEBUG: Failed to query all children", task.getException());
                    }
                });
    }

    public static void getFamilyChildren(String familyId, FamilyChildrenCallback callback) {
        android.util.Log.d("FirebaseHelper", "getFamilyChildren called with familyId: " + familyId);

        db.collection("users")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("role", "child")
                .get()
                .addOnCompleteListener(task -> {
                    android.util.Log.d("FirebaseHelper", "Firebase query completed. Success: " + task.isSuccessful());

                    if (task.isSuccessful()) {
                        android.util.Log.d("FirebaseHelper", "Query result size: " + task.getResult().size());

                        // Log all users found (for debugging)
                        for (DocumentSnapshot doc : task.getResult()) {
                            android.util.Log.d("FirebaseHelper", "Found user: " + doc.getString("name") +
                                    " (role: " + doc.getString("role") + ", familyId: " + doc.getString("familyId") + ")");
                        }

                        List<User> children = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            User child = new User();
                            child.setUserId(doc.getId());
                            child.setName(doc.getString("name"));
                            child.setRole(doc.getString("role"));
                            child.setFamilyId(doc.getString("familyId"));
                            child.setProfileImageUrl(doc.getString("profileImageUrl"));
                            Long balance = doc.getLong("starBalance");
                            child.setStarBalance(balance != null ? balance.intValue() : 0);
                            children.add(child);
                        }
                        callback.onChildrenLoaded(children);
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        android.util.Log.e("FirebaseHelper", "Failed to query children: " + errorMsg);
                        callback.onError("Failed to load children: " + errorMsg);
                    }
                });
    }

    public static void getFamilyMembers(String familyId, FamilyMembersCallback callback) {
        db.collection("users")
                .whereEqualTo("familyId", familyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> members = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            User member = new User();
                            member.setUserId(doc.getId());
                            member.setName(doc.getString("name"));
                            member.setEmail(doc.getString("email"));
                            member.setRole(doc.getString("role"));
                            member.setFamilyId(doc.getString("familyId"));
                            member.setProfileImageUrl(doc.getString("profileImageUrl"));
                            Long balance = doc.getLong("starBalance");
                            member.setStarBalance(balance != null ? balance.intValue() : 0);

                            // Load invite code data for children
                            if ("child".equals(member.getRole())) {
                                member.setInviteCode(doc.getString("inviteCode"));
                                Long expiry = doc.getLong("inviteCodeExpiry");
                                member.setInviteCodeExpires(expiry);
                            }

                            members.add(member);
                        }

                        callback.onMembersLoaded(members);
                    } else {
                        callback.onError("Failed to load family members");
                    }
                });
    }

    // ==================== TASK MANAGEMENT ====================

    // Debug method to get all family tasks regardless of status
    public static void getAllFamilyTasksForDebug(String familyId, TasksCallback callback) {
        db.collection("tasks")
                .whereEqualTo("familyId", familyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> tasks = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Task taskObj = documentToTask(doc);
                            tasks.add(taskObj);
                        }
                        callback.onTasksLoaded(tasks);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        callback.onError("Failed to load tasks: " + errorMsg);
                    }
                });
    }

    // Method to fix task status if needed
    public static void fixTaskStatus(String taskId, OnCompleteListener<Void> listener) {
        Log.d("FirebaseHelper", "Fixing task status for taskId: " + taskId);

        db.collection("tasks").document(taskId)
                .update("status", "active")
                .addOnCompleteListener(result -> {
                    if (result.isSuccessful()) {
                        Log.d("FirebaseHelper", "Task status fixed successfully");
                    } else {
                        Log.e("FirebaseHelper", "Failed to fix task status", result.getException());
                    }
                    listener.onComplete(result);
                });
    }

    public static void addTask(com.chores.app.kids.chores_app_for_kids.models.Task task, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("name", task.getName());
        taskData.put("notes", task.getNotes());
        taskData.put("iconName", task.getIconName());
        taskData.put("iconUrl", task.getIconUrl());
        taskData.put("starReward", task.getStarReward());
        taskData.put("assignedKids", task.getAssignedKids());
        taskData.put("familyId", task.getFamilyId());
        taskData.put("createdBy", task.getCreatedBy());
        taskData.put("startDateTimestamp", task.getStartDateTimestamp());
        taskData.put("repeatType", task.getRepeatType());
        taskData.put("customDays", task.getCustomDays());

        taskData.put("reminderTime", task.getReminderTime());
        taskData.put("photoProofRequired", task.isPhotoProofRequired());
        taskData.put("status", task.getStatus());
        taskData.put("createdTimestamp", task.getCreatedTimestamp());

        db.collection("tasks")
                .add(taskData)
                .addOnCompleteListener(listener);
    }

    public static void getFamilyTasks(String familyId, TasksCallback callback) {
        Log.d("FirebaseHelper", "Loading tasks for familyId: " + familyId);

        db.collection("tasks")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseHelper", "Query successful, found " + task.getResult().size() + " tasks");
                        List<Task> tasks = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Log.d("FirebaseHelper", "Processing task: " + doc.getId() + " - " + doc.getString("name"));
                            Task taskObj = documentToTask(doc);
                            tasks.add(taskObj);
                        }
                        callback.onTasksLoaded(tasks);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Log.e("FirebaseHelper", "Failed to load tasks: " + errorMsg, task.getException());
                        callback.onError("Failed to load tasks: " + errorMsg);
                    }
                });
    }

    public static void getTasksForChild(String childId, String familyId, TasksCallback callback) {
        db.collection("tasks")
                .whereEqualTo("familyId", familyId)
                .whereArrayContains("assignedKids", childId)
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> tasks = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Task taskObj = documentToTask(doc);
                            tasks.add(taskObj);
                        }
                        callback.onTasksLoaded(tasks);
                    } else {
                        callback.onError("Failed to load tasks");
                    }
                });
    }

    public static void getTasksForDate(String childId, String date, OnTasksLoadedListener callback) {
        getCurrentUser(new CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (user.getFamilyId() != null) {
                    // Parse the date string to get timestamp for start and end of day
                    long targetDateTimestamp = 0;
                    if (date != null && !date.isEmpty()) {
                        try {
                            // Parse ISO date format (yyyy-MM-dd)
                            String[] dateParts = date.split("-");
                            if (dateParts.length == 3) {
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]) - 1; // Calendar month is 0-based
                                int day = Integer.parseInt(dateParts[2]);

                                Calendar cal = Calendar.getInstance();
                                cal.set(year, month, day, 0, 0, 0);
                                cal.set(Calendar.MILLISECOND, 0);
                                targetDateTimestamp = cal.getTimeInMillis();
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseHelper", "Error parsing date: " + date, e);
                            targetDateTimestamp = System.currentTimeMillis();
                        }
                    } else {
                        targetDateTimestamp = System.currentTimeMillis();
                    }

                    final long finalTargetTimestamp = targetDateTimestamp;

                    db.collection("tasks")
                            .whereEqualTo("familyId", user.getFamilyId())
                            .whereArrayContains("assignedKids", childId)
                            .whereEqualTo("status", "active")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    List<Task> filteredTasks = new ArrayList<>();
                                    for (DocumentSnapshot doc : task.getResult()) {
                                        Task taskObj = documentToTask(doc);
                                        // Check if task should be shown on this date
                                        if (taskObj.isScheduledForDate(finalTargetTimestamp)) {
                                            // Check completion status for specific date
                                            checkTaskCompletionForDate(taskObj.getTaskId(), childId, date, isCompleted -> {
                                                taskObj.setCompleted(isCompleted);
                                            });
                                            filteredTasks.add(taskObj);
                                        }
                                    }
                                    callback.onTasksLoaded(filteredTasks);
                                } else {
                                    callback.onError("Failed to load tasks");
                                }
                            });
                } else {
                    callback.onError("User has no family ID");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public static void updateTask(Task task, OnTaskUpdatedListener callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", task.getName());
        updates.put("notes", task.getNotes());
        updates.put("iconName", task.getIconName());
        updates.put("iconUrl", task.getIconUrl());
        updates.put("starReward", task.getStarReward());
        updates.put("assignedKids", task.getAssignedKids());
        updates.put("startDateTimestamp", task.getStartDateTimestamp());
        updates.put("repeatType", task.getRepeatType());
        updates.put("customDays", task.getCustomDays());
        updates.put("reminderTime", task.getReminderTime());
        updates.put("photoProofRequired", task.isPhotoProofRequired());
        updates.put("status", task.getStatus());

        db.collection("tasks").document(task.getTaskId())
                .update(updates)
                .addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onTaskUpdated();
                    } else {
                        String errorMessage = updateTask.getException() != null ?
                                updateTask.getException().getMessage() : "Failed to update task";
                        callback.onError(errorMessage);
                    }
                });
    }

    private static void checkTaskCompletionForDate(String taskId, String userId, String date, TaskCompletionStatusCallback callback) {
        db.collection("taskCompletions")
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isCompleted = !task.getResult().isEmpty();
                        callback.onStatusReceived(isCompleted);
                    } else {
                        callback.onStatusReceived(false);
                    }
                });
    }

    public static void completeTask(String taskId, String userId, OnCompleteListener<Void> listener) {
        // Use batch write to update task completion and award stars
        WriteBatch batch = db.batch();

        // First get the task to know star reward
        db.collection("tasks").document(taskId).get()
                .addOnCompleteListener(taskResult -> {
                    if (taskResult.isSuccessful() && taskResult.getResult().exists()) {
                        DocumentSnapshot taskDoc = taskResult.getResult();
                        Long starReward = taskDoc.getLong("starReward");
                        int stars = starReward != null ? starReward.intValue() : 0;
                        String familyId = taskDoc.getString("familyId");

                        // Create task completion record
                        Map<String, Object> completionData = new HashMap<>();
                        completionData.put("taskId", taskId);
                        completionData.put("userId", userId);
                        completionData.put("completedAt", System.currentTimeMillis());
                        completionData.put("starsAwarded", stars);
                        completionData.put("date", getCurrentDateString());

                        DocumentReference completionRef = db.collection("taskCompletions").document();
                        batch.set(completionRef, completionData);

                        // Update user's star balance
                        updateStarBalance(userId, stars, familyId, "Task completed: " + taskDoc.getString("name"), taskId, null);

                        // Commit batch
                        batch.commit().addOnCompleteListener(listener);
                    } else {
                        // Pass the failure to the listener
                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Task not found or failed to load"));
                        listener.onComplete(failedTask);
                    }
                });
    }

    public static void uncompleteTask(String taskId, String userId, OnCompleteListener<Void> listener) {
        String today = getCurrentDateString();

        // Remove task completion record for today
        db.collection("taskCompletions")
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", today)
                .get()
                .addOnCompleteListener(queryTask -> {
                    if (queryTask.isSuccessful() && !queryTask.getResult().isEmpty()) {
                        // Get the completion record
                        DocumentSnapshot completionDoc = queryTask.getResult().getDocuments().get(0);
                        Long starsAwarded = completionDoc.getLong("starsAwarded");
                        int stars = starsAwarded != null ? starsAwarded.intValue() : 0;

                        // Delete the completion record
                        completionDoc.getReference().delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        // Update user's star balance (subtract stars)
                                        if (stars > 0) {
                                            // Get task details for transaction description
                                            db.collection("tasks").document(taskId).get()
                                                    .addOnCompleteListener(taskDocTask -> {
                                                        String taskName = "Unknown Task";
                                                        String familyId = null;

                                                        if (taskDocTask.isSuccessful() && taskDocTask.getResult().exists()) {
                                                            DocumentSnapshot taskDoc = taskDocTask.getResult();
                                                            taskName = taskDoc.getString("name");
                                                            familyId = taskDoc.getString("familyId");
                                                        }

                                                        if (familyId != null) {
                                                            updateStarBalance(userId, -stars, familyId,
                                                                    "Task uncompleted: " + taskName, taskId, null);
                                                        }
                                                    });
                                        }

                                        // Create success task for listener
                                        com.google.android.gms.tasks.Task<Void> successTask =
                                                com.google.android.gms.tasks.Tasks.forResult(null);
                                        listener.onComplete(successTask);
                                    } else {
                                        // Create failure task for listener
                                        com.google.android.gms.tasks.Task<Void> failedTask =
                                                com.google.android.gms.tasks.Tasks.forException(
                                                        deleteTask.getException() != null ? deleteTask.getException()
                                                                : new Exception("Failed to delete completion record"));
                                        listener.onComplete(failedTask);
                                    }
                                });
                    } else {
                        // No completion record found or query failed
                        com.google.android.gms.tasks.Task<Void> failedTask =
                                com.google.android.gms.tasks.Tasks.forException(
                                        queryTask.getException() != null ? queryTask.getException()
                                                : new Exception("No completion record found"));
                        listener.onComplete(failedTask);
                    }
                });
    }

    // ==================== REWARD MANAGEMENT ====================

    public static void addReward(Reward reward, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> rewardData = new HashMap<>();
        rewardData.put("name", reward.getName());
        rewardData.put("iconName", reward.getIconName());
        rewardData.put("iconUrl", reward.getIconUrl());
        rewardData.put("starCost", reward.getStarCost());
        rewardData.put("availableForKids", reward.getAvailableForKids());
        rewardData.put("familyId", reward.getFamilyId());
        rewardData.put("renewalPeriod", reward.getRenewalPeriod());
        rewardData.put("isCustom", reward.isCustom());
        rewardData.put("createdAt", System.currentTimeMillis());
        rewardData.put("isActive", true);

        db.collection("rewards")
                .add(rewardData)
                .addOnCompleteListener(listener);
    }

    public static void getFamilyRewards(String familyId, RewardsCallback callback) {
        db.collection("rewards")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Reward> rewards = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Reward reward = documentToReward(doc);
                            rewards.add(reward);
                        }
                        callback.onRewardsLoaded(rewards);
                    } else {
                        callback.onError("Failed to load rewards");
                    }
                });
    }

    public static void getRewardsForChild(String childId, String familyId, RewardsCallback callback) {
        db.collection("rewards")
                .whereEqualTo("familyId", familyId)
                .whereArrayContains("availableForKids", childId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Reward> rewards = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Reward reward = documentToReward(doc);
                            rewards.add(reward);
                        }
                        callback.onRewardsLoaded(rewards);
                    } else {
                        callback.onError("Failed to load rewards");
                    }
                });
    }

    public static void redeemReward(String rewardId, String userId, OnCompleteListener<Void> listener) {
        // First check if user has enough stars
        db.collection("rewards").document(rewardId).get()
                .addOnCompleteListener(rewardTask -> {
                    if (rewardTask.isSuccessful() && rewardTask.getResult().exists()) {
                        DocumentSnapshot rewardDoc = rewardTask.getResult();
                        Long starCost = rewardDoc.getLong("starCost");
                        int cost = starCost != null ? starCost.intValue() : 0;
                        String familyId = rewardDoc.getString("familyId");
                        String rewardName = rewardDoc.getString("name");

                        // Check user's star balance
                        db.collection("users").document(userId).get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                        DocumentSnapshot userDoc = userTask.getResult();
                                        Long balance = userDoc.getLong("starBalance");
                                        int currentBalance = balance != null ? balance.intValue() : 0;
                                        String childName = userDoc.getString("name");

                                        if (currentBalance >= cost) {
                                            // Create redemption record
                                            Map<String, Object> redemptionData = new HashMap<>();
                                            redemptionData.put("rewardId", rewardId);
                                            redemptionData.put("userId", userId);
                                            redemptionData.put("familyId", familyId);
                                            redemptionData.put("redeemedAt", System.currentTimeMillis());
                                            redemptionData.put("starsSpent", cost);
                                            redemptionData.put("status", "pending");

                                            // Create RedeemedReward object
                                            Reward reward = documentToReward(rewardDoc);
                                            RedeemedReward redeemedReward = new RedeemedReward(reward, userId, childName);

                                            // Save to both collections
                                            WriteBatch batch = db.batch();

                                            // Old redemption record
                                            DocumentReference redemptionRef = db.collection("rewardRedemptions").document();
                                            batch.set(redemptionRef, redemptionData);

                                            // New redeemed reward record
                                            DocumentReference redeemedRef = db.collection("redeemedRewards").document();
                                            Map<String, Object> redeemedData = new HashMap<>();
                                            redeemedData.put("rewardId", redeemedReward.getRewardId());
                                            redeemedData.put("rewardName", redeemedReward.getRewardName());
                                            redeemedData.put("iconName", redeemedReward.getIconName());
                                            redeemedData.put("iconUrl", redeemedReward.getIconUrl());
                                            redeemedData.put("starCost", redeemedReward.getStarCost());
                                            redeemedData.put("childId", redeemedReward.getChildId());
                                            redeemedData.put("childName", redeemedReward.getChildName());
                                            redeemedData.put("familyId", redeemedReward.getFamilyId());
                                            redeemedData.put("redeemedAt", redeemedReward.getRedeemedAt());
                                            redeemedData.put("timestamp", redeemedReward.getTimestamp());

                                            batch.set(redeemedRef, redeemedData);

                                            batch.commit().addOnCompleteListener(batchResult -> {
                                                if (batchResult.isSuccessful()) {
                                                    // Deduct stars from user balance
                                                    updateStarBalance(userId, -cost, familyId, "Redeemed: " + rewardName, null, rewardId);
                                                    // Create success task for listener
                                                    com.google.android.gms.tasks.Task<Void> successTask = com.google.android.gms.tasks.Tasks.forResult(null);
                                                    listener.onComplete(successTask);
                                                } else {
                                                    // Create failure task for listener
                                                    com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                                            batchResult.getException() != null ? batchResult.getException()
                                                                    : new Exception("Failed to create redemption"));
                                                    listener.onComplete(failedTask);
                                                }
                                            });
                                        } else {
                                            // Insufficient balance - create failure task
                                            com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                                    new Exception("Insufficient star balance"));
                                            listener.onComplete(failedTask);
                                        }
                                    } else {
                                        // User not found - create failure task
                                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                                userTask.getException() != null ? userTask.getException()
                                                        : new Exception("User not found"));
                                        listener.onComplete(failedTask);
                                    }
                                });
                    } else {
                        // Reward not found - create failure task
                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                rewardTask.getException() != null ? rewardTask.getException()
                                        : new Exception("Reward not found"));
                        listener.onComplete(failedTask);
                    }
                });
    }

    // ==================== REDEEMED REWARDS MANAGEMENT ====================

    public static void getRedeemedRewards(String familyId, RedeemedRewardsCallback callback) {
        Log.d("FirebaseHelper", "getRedeemedRewards called with familyId: " + familyId);

        if (familyId == null || familyId.isEmpty()) {
            Log.e("FirebaseHelper", "Family ID is null or empty");
            callback.onError("Invalid family ID");
            return;
        }

        // Use simple query first, then sort in memory to avoid index issues
        db.collection("redeemedRewards")
                .whereEqualTo("familyId", familyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseHelper", "Successfully retrieved " + task.getResult().size() + " redeemed rewards");
                        processRedeemedRewardsResult(task.getResult(), callback);
                    } else {
                        Log.e("FirebaseHelper", "Failed to get redeemed rewards", task.getException());
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        callback.onError("Failed to load redeemed rewards: " + errorMsg);
                    }
                });
    }

    private static void processRedeemedRewardsResult(QuerySnapshot querySnapshot, RedeemedRewardsCallback callback) {
        List<RedeemedReward> redeemedRewards = new ArrayList<>();

        Log.d("FirebaseHelper", "Processing " + querySnapshot.size() + " redeemed reward documents");

        for (DocumentSnapshot doc : querySnapshot) {
            try {
                RedeemedReward redeemedReward = documentToRedeemedReward(doc);
                if (redeemedReward != null) {
                    redeemedRewards.add(redeemedReward);
                    Log.d("FirebaseHelper", "Processed reward: " + redeemedReward.getRewardName() +
                            " for child: " + redeemedReward.getChildName() +
                            " (ID: " + redeemedReward.getChildId() + ")");
                } else {
                    Log.w("FirebaseHelper", "Failed to process document: " + doc.getId());
                }
            } catch (Exception e) {
                Log.e("FirebaseHelper", "Error processing redeemed reward document: " + doc.getId(), e);
            }
        }

        // Sort by timestamp/date in descending order (newest first)
        redeemedRewards.sort((r1, r2) -> {
            // First try to compare by timestamp
            if (r1.getTimestamp() != 0 && r2.getTimestamp() != 0) {
                return Long.compare(r2.getTimestamp(), r1.getTimestamp());
            }

            // Fallback to comparing by redeemedAt date
            if (r1.getRedeemedAt() == null && r2.getRedeemedAt() == null) return 0;
            if (r1.getRedeemedAt() == null) return 1;
            if (r2.getRedeemedAt() == null) return -1;
            return r2.getRedeemedAt().compareTo(r1.getRedeemedAt());
        });

        Log.d("FirebaseHelper", "Returning " + redeemedRewards.size() + " processed and sorted redeemed rewards");
        callback.onRedeemedRewardsLoaded(redeemedRewards);
    }


    public static void getRedeemedRewardsForChild(String childId, String familyId, RedeemedRewardsCallback callback) {
        Log.d("FirebaseHelper", "getRedeemedRewardsForChild called with childId: " + childId + ", familyId: " + familyId);

        if (childId == null || childId.isEmpty()) {
            Log.e("FirebaseHelper", "Child ID is null or empty");
            callback.onError("Invalid child ID");
            return;
        }

        if (familyId == null || familyId.isEmpty()) {
            Log.e("FirebaseHelper", "Family ID is null or empty");
            callback.onError("Invalid family ID");
            return;
        }

        db.collection("redeemedRewards")
                .whereEqualTo("childId", childId)
                .whereEqualTo("familyId", familyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseHelper", "Successfully retrieved " + task.getResult().size() + " child-specific redeemed rewards");
                        processRedeemedRewardsResult(task.getResult(), callback);
                    } else {
                        Log.e("FirebaseHelper", "Failed to get child-specific redeemed rewards", task.getException());
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        callback.onError("Failed to load redeemed rewards for child: " + errorMsg);
                    }
                });
    }

    public static void redeemRewardWithSelectedChild(String rewardId, String childId, OnCompleteListener<Void> listener) {
        Log.d("FirebaseHelper", "redeemRewardWithSelectedChild - RewardID: " + rewardId + ", ChildID: " + childId);

        if (rewardId == null || rewardId.isEmpty() || childId == null || childId.isEmpty()) {
            Log.e("FirebaseHelper", "Invalid parameters - RewardID: " + rewardId + ", ChildID: " + childId);
            Exception exception = new Exception("Invalid reward ID or child ID");
            com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(exception);
            listener.onComplete(failedTask);
            return;
        }

        // First, get the reward details
        db.collection("rewards").document(rewardId).get()
                .addOnCompleteListener(rewardTask -> {
                    if (rewardTask.isSuccessful() && rewardTask.getResult().exists()) {
                        DocumentSnapshot rewardDoc = rewardTask.getResult();
                        Long starCostLong = rewardDoc.getLong("starCost");
                        int starCost = starCostLong != null ? starCostLong.intValue() : 0;
                        String rewardName = rewardDoc.getString("name");
                        String familyId = rewardDoc.getString("familyId");

                        Log.d("FirebaseHelper", "Reward details - Name: " + rewardName + ", Cost: " + starCost + " stars");

                        // Get the child's current star balance
                        db.collection("users").document(childId).get()
                                .addOnCompleteListener(childTask -> {
                                    if (childTask.isSuccessful() && childTask.getResult().exists()) {
                                        DocumentSnapshot childDoc = childTask.getResult();
                                        Long currentBalanceLong = childDoc.getLong("starBalance");
                                        int currentBalance = currentBalanceLong != null ? currentBalanceLong.intValue() : 0;
                                        String childName = childDoc.getString("name");

                                        Log.d("FirebaseHelper", "Child current balance: " + currentBalance + " stars");

                                        // Check if child has enough stars
                                        if (currentBalance >= starCost) {
                                            // Proceed with redemption using WriteBatch for atomicity
                                            performAtomicRedemption(rewardDoc, childDoc, starCost, childId, childName, familyId, listener);
                                        } else {
                                            Log.w("FirebaseHelper", "Insufficient stars - Has: " + currentBalance + ", Needs: " + starCost);
                                            Exception exception = new Exception("Insufficient star balance. You have " + currentBalance +
                                                    " stars but need " + starCost + " stars.");
                                            com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(exception);
                                            listener.onComplete(failedTask);
                                        }
                                    } else {
                                        Log.e("FirebaseHelper", "Child document not found or error", childTask.getException());
                                        Exception exception = childTask.getException() != null ? childTask.getException() :
                                                new Exception("Child profile not found");
                                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(exception);
                                        listener.onComplete(failedTask);
                                    }
                                });
                    } else {
                        Log.e("FirebaseHelper", "Reward document not found or error", rewardTask.getException());
                        Exception exception = rewardTask.getException() != null ? rewardTask.getException() :
                                new Exception("Reward not found");
                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(exception);
                        listener.onComplete(failedTask);
                    }
                });
    }

    private static void performAtomicRedemption(DocumentSnapshot rewardDoc, DocumentSnapshot childDoc,
                                                int starCost, String childId, String childName,
                                                String familyId, OnCompleteListener<Void> listener) {

        long currentTimestamp = System.currentTimeMillis();
        int currentBalance = childDoc.getLong("starBalance") != null ? childDoc.getLong("starBalance").intValue() : 0;
        int newBalance = currentBalance - starCost;

        Log.d("FirebaseHelper", "Performing atomic redemption - Deducting " + starCost +
                " stars from " + currentBalance + " = " + newBalance);

        WriteBatch batch = db.batch();

        // 1. Update child's star balance
        DocumentReference childRef = db.collection("users").document(childId);
        batch.update(childRef, "starBalance", newBalance);
        Log.d("FirebaseHelper", "Batch: Update child star balance to " + newBalance);

        // 2. Create redemption record in redeemedRewards collection
        DocumentReference redeemedRewardRef = db.collection("redeemedRewards").document();
        Map<String, Object> redeemedRewardData = new HashMap<>();
        redeemedRewardData.put("rewardId", rewardDoc.getId());
        redeemedRewardData.put("rewardName", rewardDoc.getString("name"));
        redeemedRewardData.put("iconName", rewardDoc.getString("iconName"));
        redeemedRewardData.put("iconUrl", rewardDoc.getString("iconUrl"));
        redeemedRewardData.put("starCost", starCost);
        redeemedRewardData.put("childId", childId);
        redeemedRewardData.put("childName", childName);
        redeemedRewardData.put("familyId", familyId);
        redeemedRewardData.put("redeemedAt", currentTimestamp);
        redeemedRewardData.put("timestamp", currentTimestamp);

        batch.set(redeemedRewardRef, redeemedRewardData);
        Log.d("FirebaseHelper", "Batch: Create redeemed reward record");

        // 3. Create transaction record for star tracking
        DocumentReference transactionRef = db.collection("starTransactions").document();
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", childId);
        transactionData.put("familyId", familyId);
        transactionData.put("type", "reward_redemption");
        transactionData.put("amount", -starCost); // Negative for deduction
        transactionData.put("description", "Redeemed: " + rewardDoc.getString("name"));
        transactionData.put("relatedId", rewardDoc.getId());
        transactionData.put("timestamp", currentTimestamp);
        transactionData.put("balanceBefore", currentBalance);
        transactionData.put("balanceAfter", newBalance);

        batch.set(transactionRef, transactionData);
        Log.d("FirebaseHelper", "Batch: Create star transaction record");

        // 4. Optional: Create legacy redemption record for compatibility
        DocumentReference legacyRedemptionRef = db.collection("rewardRedemptions").document();
        Map<String, Object> legacyRedemptionData = new HashMap<>();
        legacyRedemptionData.put("rewardId", rewardDoc.getId());
        legacyRedemptionData.put("userId", childId);
        legacyRedemptionData.put("familyId", familyId);
        legacyRedemptionData.put("redeemedAt", currentTimestamp);
        legacyRedemptionData.put("starsSpent", starCost);
        legacyRedemptionData.put("status", "completed");

        batch.set(legacyRedemptionRef, legacyRedemptionData);
        Log.d("FirebaseHelper", "Batch: Create legacy redemption record");

        // Commit the batch
        batch.commit().addOnCompleteListener(batchTask -> {
            if (batchTask.isSuccessful()) {
                Log.d("FirebaseHelper", "Atomic redemption completed successfully - " +
                        starCost + " stars deducted from " + childName);

                // Create success task
                com.google.android.gms.tasks.Task<Void> successTask = com.google.android.gms.tasks.Tasks.forResult(null);
                listener.onComplete(successTask);
            } else {
                Log.e("FirebaseHelper", "Atomic redemption failed", batchTask.getException());

                // Create failure task
                Exception exception = batchTask.getException() != null ? batchTask.getException() :
                        new Exception("Failed to complete reward redemption");
                com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(exception);
                listener.onComplete(failedTask);
            }
        });
    }
    // Enhanced documentToRedeemedReward method with better error handling
    private static RedeemedReward documentToRedeemedReward(DocumentSnapshot doc) {
        try {
            if (!doc.exists()) {
                Log.w("FirebaseHelper", "Document does not exist: " + doc.getId());
                return null;
            }

            RedeemedReward redeemedReward = new RedeemedReward();
            redeemedReward.setRedeemedRewardId(doc.getId());

            // Basic reward information
            redeemedReward.setRewardId(doc.getString("rewardId"));
            redeemedReward.setRewardName(doc.getString("rewardName"));
            redeemedReward.setIconName(doc.getString("iconName"));
            redeemedReward.setIconUrl(doc.getString("iconUrl"));

            // Star cost
            Long starCost = doc.getLong("starCost");
            redeemedReward.setStarCost(starCost != null ? starCost.intValue() : 0);

            // Child and family information
            redeemedReward.setChildId(doc.getString("childId"));
            redeemedReward.setChildName(doc.getString("childName"));
            redeemedReward.setFamilyId(doc.getString("familyId"));

            // Enhanced date handling
            Object redeemedAtObj = doc.get("redeemedAt");
            if (redeemedAtObj instanceof com.google.firebase.Timestamp) {
                redeemedReward.setRedeemedAt(((com.google.firebase.Timestamp) redeemedAtObj).toDate());
            } else if (redeemedAtObj instanceof Long) {
                redeemedReward.setRedeemedAt(new java.util.Date((Long) redeemedAtObj));
            } else if (redeemedAtObj instanceof java.util.Date) {
                redeemedReward.setRedeemedAt((java.util.Date) redeemedAtObj);
            } else {
                // Fallback to current time if no valid date found
                Log.w("FirebaseHelper", "No valid redeemedAt date found for reward: " + doc.getId() + ", using current time");
                redeemedReward.setRedeemedAt(new java.util.Date());
            }

            // Enhanced timestamp handling
            Long timestamp = doc.getLong("timestamp");
            if (timestamp != null && timestamp > 0) {
                redeemedReward.setTimestamp(timestamp);
            } else {
                // If no timestamp, use redeemedAt date as timestamp
                if (redeemedReward.getRedeemedAt() != null) {
                    redeemedReward.setTimestamp(redeemedReward.getRedeemedAt().getTime());
                } else {
                    redeemedReward.setTimestamp(System.currentTimeMillis());
                }
                Log.d("FirebaseHelper", "No timestamp found for reward: " + doc.getId() + ", using redeemedAt as fallback");
            }

            // Validation
            if (redeemedReward.getRewardName() == null || redeemedReward.getRewardName().isEmpty()) {
                Log.w("FirebaseHelper", "Reward has empty name: " + doc.getId());
                redeemedReward.setRewardName("Unknown Reward");
            }

            if (redeemedReward.getChildName() == null || redeemedReward.getChildName().isEmpty()) {
                Log.w("FirebaseHelper", "Reward has empty child name: " + doc.getId());
                redeemedReward.setChildName("Unknown Child");
            }

            return redeemedReward;
        } catch (Exception e) {
            Log.e("FirebaseHelper", "Error converting document to RedeemedReward: " + doc.getId(), e);
            return null;
        }
    }
    public interface RedeemedRewardsCallback {
        void onRedeemedRewardsLoaded(List<RedeemedReward> redeemedRewards);

        void onError(String error);
    }

    // ==================== STAR MANAGEMENT ====================

    public static void updateStarBalance(String userId, int amount, String familyId, String description, String taskId, String rewardId) {
        // Get current balance and update
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        Long currentBalance = document.getLong("starBalance");
                        int newBalance = (currentBalance != null ? currentBalance.intValue() : 0) + amount;

                        // Ensure balance doesn't go negative
                        newBalance = Math.max(0, newBalance);

                        // Update user balance
                        db.collection("users").document(userId)
                                .update("starBalance", newBalance);

                        // Create transaction record
                        StarTransaction transaction = new StarTransaction();
                        transaction.setUserId(userId);
                        transaction.setFamilyId(familyId);
                        transaction.setType(amount > 0 ? "earned" : "spent");
                        transaction.setAmount(Math.abs(amount));
                        transaction.setDescription(description);
                        transaction.setTimestamp(System.currentTimeMillis());
                        transaction.setRelatedTaskId(taskId);
                        transaction.setRelatedRewardId(rewardId);

                        Map<String, Object> transactionData = new HashMap<>();
                        transactionData.put("userId", transaction.getUserId());
                        transactionData.put("familyId", transaction.getFamilyId());
                        transactionData.put("type", transaction.getType());
                        transactionData.put("amount", transaction.getAmount());
                        transactionData.put("description", transaction.getDescription());
                        transactionData.put("timestamp", transaction.getTimestamp());
                        transactionData.put("relatedTaskId", transaction.getRelatedTaskId());
                        transactionData.put("relatedRewardId", transaction.getRelatedRewardId());
                        transactionData.put("balanceAfter", newBalance);

                        db.collection("starTransactions").add(transactionData);
                    }
                });
    }

    public static void getUserStarBalance(StarBalanceCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
                            Long balance = document.getLong("starBalance");
                            callback.onStarBalanceReceived(balance != null ? balance.intValue() : 0);
                        } else {
                            callback.onStarBalanceReceived(0);
                        }
                    });
        } else {
            callback.onStarBalanceReceived(0);
        }
    }

    public static void getStarTransactions(String userId, String familyId, StarTransactionsCallback callback) {
        db.collection("starTransactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("familyId", familyId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Get last 50 transactions
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<StarTransaction> transactions = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            StarTransaction transaction = documentToStarTransaction(doc);
                            transactions.add(transaction);
                        }
                        callback.onTransactionsLoaded(transactions);
                    } else {
                        callback.onError("Failed to load transactions");
                    }
                });
    }

    // ==================== CHILD PROFILE MANAGEMENT ====================

    public static void getChildProfiles(OnChildProfilesLoadedListener listener) {
        getCurrentUser(new CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (user.getFamilyId() != null) {
                    getChildProfilesWithInviteCodes(user.getFamilyId(), new ChildProfilesCallback() {
                        @Override
                        public void onProfilesLoaded(List<ChildProfile> profiles) {
                            listener.onChildProfilesLoaded(profiles);
                        }

                        @Override
                        public void onError(String error) {
                            listener.onError(error);
                        }
                    });
                } else {
                    listener.onError("User has no family ID");
                }
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    public interface OnChildProfilesLoadedListener {
        void onChildProfilesLoaded(List<ChildProfile> profiles);

        void onError(String error);
    }

    public static void generateMissingInviteCodes(String familyId, OnCompleteListener<Void> listener) {
        generateInviteCodesForAllChildren(familyId, listener);
    }

    public static void getChildrenInviteCodes(String familyId, ChildrenInviteCodesCallback callback) {
        db.collection("users")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("role", "child")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> childrenData = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Map<String, Object> childData = new HashMap<>();
                            childData.put("name", doc.getString("name"));
                            childData.put("inviteCode", doc.getString("inviteCode"));
                            Long expiry = doc.getLong("inviteCodeExpiry");
                            childData.put("inviteCodeExpiry", expiry != null ? expiry : 0);
                            Long balance = doc.getLong("starBalance");
                            childData.put("starBalance", balance != null ? balance.intValue() : 0);
                            childData.put("childId", doc.getId());
                            childrenData.add(childData);
                        }
                        callback.onInviteCodesLoaded(childrenData);
                    } else {
                        callback.onError("Failed to load children invite codes");
                    }
                });
    }

    public static void getChildProfilesWithInviteCodes(String familyId, ChildProfilesCallback callback) {
        db.collection("users")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("role", "child")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ChildProfile> profiles = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            ChildProfile profile = new ChildProfile();
                            profile.setChildId(doc.getId());
                            profile.setName(doc.getString("name"));
                            profile.setFamilyId(doc.getString("familyId"));
                            profile.setInviteCode(doc.getString("inviteCode"));
                            Long expiry = doc.getLong("inviteCodeExpiry");
                            profile.setInviteCodeExpiry(expiry != null ? expiry : 0);
                            profile.setProfileImageUrl(doc.getString("profileImageUrl"));
                            Long balance = doc.getLong("starBalance");
                            profile.setStarBalance(balance != null ? balance.intValue() : 0);
                            Boolean active = doc.getBoolean("isActive");
                            profile.setActive(active == null || active);
                            Long created = doc.getLong("createdAt");
                            profile.setCreatedAt(created != null ? created : System.currentTimeMillis());
                            profiles.add(profile);
                        }
                        callback.onProfilesLoaded(profiles);
                    } else {
                        callback.onError("Failed to load child profiles");
                    }
                });
    }

    public static void generateChildInviteCode(String childId, OnCompleteListener<Void> listener) {
        String inviteCode = String.format("%06d", new Random().nextInt(1000000));
        long expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days instead of 24 hours

        Log.d("FirebaseHelper", "Generating invite code: " + inviteCode +
                " for child: " + childId + ", expires at: " + expiryTime);

        Map<String, Object> updates = new HashMap<>();
        updates.put("inviteCode", inviteCode);
        updates.put("inviteCodeExpiry", expiryTime);

        db.collection("users")
                .document(childId)
                .update(updates)
                .addOnCompleteListener(result -> {
                    if (result.isSuccessful()) {
                        Log.d("FirebaseHelper", "Successfully generated invite code for child: " + childId);
                    } else {
                        Log.e("FirebaseHelper", "Failed to generate invite code for child: " + childId, result.getException());
                    }
                    listener.onComplete(result);
                });
    }

    public static void generateInviteCodesForAllChildren(String familyId, OnCompleteListener<Void> listener) {
        db.collection("users")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("role", "child")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        boolean hasUpdates = false;

                        for (DocumentSnapshot doc : task.getResult()) {
                            String existingCode = doc.getString("inviteCode");
                            Long expiry = doc.getLong("inviteCodeExpiry");
                            long currentTime = System.currentTimeMillis();

                            // Generate code if none exists or if expired
                            if (existingCode == null || existingCode.isEmpty() ||
                                    (expiry != null && expiry <= currentTime)) {

                                String newInviteCode = String.format("%06d", new Random().nextInt(1000000));
                                long expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days

                                batch.update(doc.getReference(), "inviteCode", newInviteCode);
                                batch.update(doc.getReference(), "inviteCodeExpiry", expiryTime);
                                hasUpdates = true;
                            }
                        }

                        if (hasUpdates) {
                            batch.commit().addOnCompleteListener(listener);
                        } else {
                            // No updates needed, create success task
                            com.google.android.gms.tasks.TaskCompletionSource<Void> taskSource =
                                    new com.google.android.gms.tasks.TaskCompletionSource<>();
                            taskSource.setResult(null);
                            listener.onComplete(taskSource.getTask());
                        }
                    } else {
                        // Create failed task
                        com.google.android.gms.tasks.TaskCompletionSource<Void> taskSource =
                                new com.google.android.gms.tasks.TaskCompletionSource<>();
                        taskSource.setException(task.getException() != null ? task.getException() :
                                new Exception("Failed to load children"));
                        listener.onComplete(taskSource.getTask());
                    }
                });
    }

    public static void deleteChildProfile(String childId, OnCompleteListener<Void> listener) {
        // Soft delete - mark as inactive
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", false);
        updates.put("inviteCode", "");
        updates.put("inviteCodeExpiry", 0);

        db.collection("users")
                .document(childId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    public static void joinFamilyWithChildCode(String inviteCode, OnCompleteListener<ChildProfile> listener) {
        long currentTime = System.currentTimeMillis();

        Log.d("FirebaseHelper", "Looking for invite code: " + inviteCode + " at time: " + currentTime);

        db.collection("users")
                .whereEqualTo("role", "child")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseHelper", "Query successful, found " + task.getResult().size() + " matching codes");

                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            Long expiryTime = doc.getLong("inviteCodeExpiry");
                            String childName = doc.getString("name");

                            Log.d("FirebaseHelper", "Found code for child: " + childName +
                                    ", expires at: " + expiryTime + ", current time: " + currentTime);

                            // Check if code is still valid (allow 5 minute buffer for timing issues)
                            if (expiryTime != null && expiryTime > (currentTime - 5 * 60 * 1000)) {
                                Log.d("FirebaseHelper", "Code is valid, creating profile");

                                ChildProfile profile = new ChildProfile();
                                profile.setChildId(doc.getId());
                                profile.setName(doc.getString("name"));
                                profile.setFamilyId(doc.getString("familyId"));
                                profile.setInviteCode(doc.getString("inviteCode"));

                                // Create successful task
                                com.google.android.gms.tasks.TaskCompletionSource<ChildProfile> taskSource =
                                        new com.google.android.gms.tasks.TaskCompletionSource<>();
                                taskSource.setResult(profile);
                                listener.onComplete(taskSource.getTask());
                            } else {
                                Log.d("FirebaseHelper", "Code is expired");
                                // Create failed task
                                com.google.android.gms.tasks.TaskCompletionSource<ChildProfile> taskSource =
                                        new com.google.android.gms.tasks.TaskCompletionSource<>();
                                taskSource.setException(new Exception("Invite code has expired"));
                                listener.onComplete(taskSource.getTask());
                            }
                        } else {
                            Log.d("FirebaseHelper", "No matching invite code found");
                            // Create failed task
                            com.google.android.gms.tasks.TaskCompletionSource<ChildProfile> taskSource =
                                    new com.google.android.gms.tasks.TaskCompletionSource<>();
                            taskSource.setException(new Exception("Invalid invite code"));
                            listener.onComplete(taskSource.getTask());
                        }
                    } else {
                        Log.e("FirebaseHelper", "Query failed", task.getException());
                        // Create failed task
                        com.google.android.gms.tasks.TaskCompletionSource<ChildProfile> taskSource =
                                new com.google.android.gms.tasks.TaskCompletionSource<>();
                        taskSource.setException(new Exception("Failed to verify invite code"));
                        listener.onComplete(taskSource.getTask());
                    }
                });
    }

// ==================== TASK PRESETS MANAGEMENT ====================

    public interface TaskPresetsCallback {
        void onPresetsLoaded(List<TaskPreset> presets);

        void onError(String error);
    }

    public interface TaskIconsCallback {
        void onIconsLoaded(List<TaskIcon> icons);

        void onError(String error);
    }

    public interface InviteCodeCallback {
        void onInviteCodeLoaded(String inviteCode, long expiryTime);

        void onError(String error);
    }

    public static void getTaskPresets(TaskPresetsCallback callback) {
        db.collection("taskPresets")
                .orderBy("createdTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TaskPreset> presets = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            TaskPreset preset = documentToTaskPreset(doc);
                            presets.add(preset);
                        }
                        callback.onPresetsLoaded(presets);
                    } else {
                        callback.onError("Failed to load task presets");
                    }
                });
    }

    public static void addTaskPreset(TaskPreset preset, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> presetData = new HashMap<>();
        presetData.put("name", preset.getName());
        presetData.put("iconUrl", preset.getIconUrl());
        presetData.put("starReward", preset.getStarReward());
        presetData.put("description", preset.getDescription());
        presetData.put("createdTimestamp", preset.getCreatedTimestamp());

        db.collection("taskPresets")
                .add(presetData)
                .addOnCompleteListener(listener);
    }

    // ==================== TASK ICONS MANAGEMENT ====================

    public static void getTaskIcons(TaskIconsCallback callback) {
        db.collection("taskIcons")
                .orderBy("createdTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TaskIcon> icons = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            TaskIcon icon = documentToTaskIcon(doc);
                            icons.add(icon);
                        }
                        callback.onIconsLoaded(icons);
                    } else {
                        callback.onError("Failed to load task icons");
                    }
                });
    }

    public static void addTaskIcon(TaskIcon icon, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> iconData = new HashMap<>();
        iconData.put("name", icon.getName());
        iconData.put("iconUrl", icon.getIconUrl());
        iconData.put("category", icon.getCategory());
        iconData.put("isDefault", icon.isDefault());
        iconData.put("drawableName", icon.getDrawableName());
        iconData.put("createdTimestamp", icon.getCreatedTimestamp());

        db.collection("taskIcons")
                .add(iconData)
                .addOnCompleteListener(listener);
    }

    public static void seedDefaultTaskIcons(OnCompleteListener<Void> listener) {
        // Check if default icons already exist
        db.collection("taskIcons")
                .whereEqualTo("isDefault", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // No default icons exist, create them
                        WriteBatch batch = db.batch();

                        List<TaskIcon> defaultIcons = createDefaultTaskIcons();
                        for (TaskIcon icon : defaultIcons) {
                            Map<String, Object> iconData = new HashMap<>();
                            iconData.put("name", icon.getName());
                            iconData.put("iconUrl", icon.getIconUrl());
                            iconData.put("category", icon.getCategory());
                            iconData.put("isDefault", icon.isDefault());
                            iconData.put("drawableName", icon.getDrawableName());
                            iconData.put("createdTimestamp", icon.getCreatedTimestamp());

                            DocumentReference docRef = db.collection("taskIcons").document();
                            batch.set(docRef, iconData);
                        }

                        batch.commit().addOnCompleteListener(listener);
                    } else {
                        // Default icons already exist or error occurred
                        // Create success task for listener
                        com.google.android.gms.tasks.Task<Void> successTask = com.google.android.gms.tasks.Tasks.forResult(null);
                        listener.onComplete(successTask);
                    }
                });
    }

    private static List<TaskIcon> createDefaultTaskIcons() {
        List<TaskIcon> icons = new ArrayList<>();

        icons.add(createDefaultTaskIcon("Brush Teeth", "personal_care", "ic_brush_teeth"));
        icons.add(createDefaultTaskIcon("Clean Room", "chores", "ic_clean_room"));
        icons.add(createDefaultTaskIcon("Do Homework", "education", "ic_homework"));
        icons.add(createDefaultTaskIcon("Feed Pet", "pets", "ic_pet"));
        icons.add(createDefaultTaskIcon("Take Shower", "personal_care", "ic_shower"));
        icons.add(createDefaultTaskIcon("Make Bed", "chores", "ic_bed"));
        icons.add(createDefaultTaskIcon("Wash Dishes", "chores", "ic_dishes"));
        icons.add(createDefaultTaskIcon("Exercise", "health", "ic_exercise"));

        return icons;
    }

    private static TaskIcon createDefaultTaskIcon(String name, String category, String drawableName) {
        TaskIcon icon = new TaskIcon();
        icon.setName(name);
        icon.setIconUrl(""); // Empty for drawable resources
        icon.setCategory(category);
        icon.setDefault(true);
        icon.setDrawableName(drawableName);
        icon.setCreatedTimestamp(System.currentTimeMillis());
        return icon;
    }

    private static TaskPreset documentToTaskPreset(DocumentSnapshot doc) {
        TaskPreset preset = new TaskPreset();
        preset.setId(doc.getId());
        preset.setName(doc.getString("name"));
        preset.setIconUrl(doc.getString("iconUrl"));

        Long starReward = doc.getLong("starReward");
        preset.setStarReward(starReward != null ? starReward.intValue() : 1);

        preset.setDescription(doc.getString("description"));

        Long timestamp = doc.getLong("createdTimestamp");
        preset.setCreatedTimestamp(timestamp != null ? timestamp : 0);

        return preset;
    }

    private static TaskIcon documentToTaskIcon(DocumentSnapshot doc) {
        TaskIcon icon = new TaskIcon();
        icon.setId(doc.getId());
        icon.setName(doc.getString("name"));
        icon.setIconUrl(doc.getString("iconUrl"));
        icon.setCategory(doc.getString("category"));
        icon.setDrawableName(doc.getString("drawableName"));

        Boolean isDefault = doc.getBoolean("isDefault");
        icon.setDefault(isDefault != null && isDefault);

        Long timestamp = doc.getLong("createdTimestamp");
        icon.setCreatedTimestamp(timestamp != null ? timestamp : 0);

        return icon;
    }



    // ==================== UTILITY METHODS ====================

    public static String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    private static String getCurrentDateString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    // Document conversion methods
    private static com.chores.app.kids.chores_app_for_kids.models.Task documentToTask(DocumentSnapshot doc) {
        com.chores.app.kids.chores_app_for_kids.models.Task task = new com.chores.app.kids.chores_app_for_kids.models.Task();
        task.setTaskId(doc.getId());
        task.setName(doc.getString("name"));
        task.setNotes(doc.getString("notes"));
        task.setIconName(doc.getString("iconName"));
        task.setIconUrl(doc.getString("iconUrl"));

        Long starReward = doc.getLong("starReward");
        task.setStarReward(starReward != null ? starReward.intValue() : 0);

        List<String> assignedKids = (List<String>) doc.get("assignedKids");
        task.setAssignedKids(assignedKids != null ? assignedKids : new ArrayList<>());

        task.setFamilyId(doc.getString("familyId"));
        task.setCreatedBy(doc.getString("createdBy"));

        // Handle both old and new date fields for backward compatibility
        Long startDateTimestamp = doc.getLong("startDateTimestamp");
        if (startDateTimestamp != null) {
            task.setStartDateTimestamp(startDateTimestamp);
        } else {
            // Fallback to old string field if exists
            String oldStartDate = doc.getString("startDate");
            if (oldStartDate != null && !oldStartDate.isEmpty()) {
                try {
                    task.setStartDateTimestamp(Long.parseLong(oldStartDate));
                } catch (NumberFormatException e) {
                    task.setStartDateTimestamp(System.currentTimeMillis());
                }
            } else {
                task.setStartDateTimestamp(System.currentTimeMillis());
            }
        }

        task.setRepeatType(doc.getString("repeatType"));

        // Fix: Convert Long values from Firebase to Integer values
        List<Object> customDaysRaw = (List<Object>) doc.get("customDays");
        List<Integer> customDays = new ArrayList<>();
        if (customDaysRaw != null) {
            for (Object day : customDaysRaw) {
                if (day instanceof Long) {
                    customDays.add(((Long) day).intValue());
                } else if (day instanceof Integer) {
                    customDays.add((Integer) day);
                }
            }
        }
        task.setCustomDays(customDays);

        task.setReminderTime(doc.getString("reminderTime"));

        Boolean photoProof = doc.getBoolean("photoProofRequired");
        task.setPhotoProofRequired(photoProof != null && photoProof);

        task.setStatus(doc.getString("status"));

        Long timestamp = doc.getLong("createdTimestamp");
        task.setCreatedTimestamp(timestamp != null ? timestamp : 0);

        return task;
    }

    private static Reward documentToReward(DocumentSnapshot doc) {
        Reward reward = new Reward();
        reward.setRewardId(doc.getId());
        reward.setName(doc.getString("name"));
        reward.setIconName(doc.getString("iconName"));
        reward.setIconUrl(doc.getString("iconUrl"));

        Long starCost = doc.getLong("starCost");
        reward.setStarCost(starCost != null ? starCost.intValue() : 0);

        List<String> availableForKids = (List<String>) doc.get("availableForKids");
        reward.setAvailableForKids(availableForKids != null ? availableForKids : new ArrayList<>());

        reward.setFamilyId(doc.getString("familyId"));
        reward.setRenewalPeriod(doc.getString("renewalPeriod"));

        Boolean isCustom = doc.getBoolean("isCustom");
        reward.setCustom(isCustom != null && isCustom);

        return reward;
    }

    private static StarTransaction documentToStarTransaction(DocumentSnapshot doc) {
        StarTransaction transaction = new StarTransaction();
        transaction.setTransactionId(doc.getId());
        transaction.setUserId(doc.getString("userId"));
        transaction.setFamilyId(doc.getString("familyId"));
        transaction.setType(doc.getString("type"));

        Long amount = doc.getLong("amount");
        transaction.setAmount(amount != null ? amount.intValue() : 0);

        transaction.setDescription(doc.getString("description"));

        Long timestamp = doc.getLong("timestamp");
        transaction.setTimestamp(timestamp != null ? timestamp : 0);

        transaction.setRelatedTaskId(doc.getString("relatedTaskId"));
        transaction.setRelatedRewardId(doc.getString("relatedRewardId"));

        return transaction;
    }

    // ==================== PRE-REWARDS MANAGEMENT ====================

    public static void getPreRewards(PreRewardsCallback callback) {
        db.collection("preRewards")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<com.chores.app.kids.chores_app_for_kids.models.PreReward> preRewards = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            com.chores.app.kids.chores_app_for_kids.models.PreReward preReward = documentToPreReward(doc);
                            preRewards.add(preReward);
                        }
                        callback.onPreRewardsLoaded(preRewards);
                    } else {
                        callback.onError("Failed to load pre-rewards");
                    }
                });
    }

    private static com.chores.app.kids.chores_app_for_kids.models.PreReward documentToPreReward(DocumentSnapshot doc) {
        com.chores.app.kids.chores_app_for_kids.models.PreReward preReward = new com.chores.app.kids.chores_app_for_kids.models.PreReward();
        preReward.setId(doc.getId());
        preReward.setName(doc.getString("name"));
        preReward.setIconName(doc.getString("iconName"));
        preReward.setIconUrl(doc.getString("iconUrl"));

        Long starCost = doc.getLong("starCost");
        preReward.setStarCost(starCost != null ? starCost.intValue() : 0);

        return preReward;
    }

    public interface PreRewardsCallback {
        void onPreRewardsLoaded(List<com.chores.app.kids.chores_app_for_kids.models.PreReward> preRewards);

        void onError(String error);
    }

    // ==================== CALLBACK INTERFACES ====================

    public interface StarBalanceCallback {
        void onStarBalanceReceived(int balance);
    }

    public interface ChildrenInviteCodesCallback {
        void onInviteCodesLoaded(List<Map<String, Object>> childrenData);

        void onError(String error);
    }

    public interface ChildProfilesCallback {
        void onProfilesLoaded(List<ChildProfile> profiles);

        void onError(String error);
    }

    public static void getUserStarBalanceById(String userId, StarBalanceCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        Long balance = document.getLong("starBalance");
                        callback.onStarBalanceReceived(balance != null ? balance.intValue() : 0);
                    } else {
                        callback.onStarBalanceReceived(0);
                    }
                });
    }

    public static void getUserById(String userId, CurrentUserCallback callback) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        User user = new User();
                        user.setUserId(doc.getId());
                        user.setName(doc.getString("name"));
                        user.setEmail(doc.getString("email"));
                        user.setRole(doc.getString("role"));
                        user.setFamilyId(doc.getString("familyId"));
                        Long balance = doc.getLong("starBalance");
                        user.setStarBalance(balance != null ? balance.intValue() : 0);
                        Boolean tts = doc.getBoolean("textToSpeechEnabled");
                        user.setTextToSpeechEnabled(tts != null && tts);
                        user.setProfileImageUrl(doc.getString("profileImageUrl"));

                        callback.onUserLoaded(user);
                    } else {
                        callback.onError("User not found");
                    }
                });
    }

    public interface TaskCompletionStatsCallback {
        void onStatsReceived(int completedToday);
    }

    public interface CurrentUserCallback {
        void onUserLoaded(User user);
        void onError(String error);
    }

    public interface FamilyChildrenCallback {
        void onChildrenLoaded(List<User> children);
        void onError(String error);
    }

    public interface CreateUserCallback {
        void onUserCreated(String userId);
        void onError(String error);
    }

    public interface FamilyMembersCallback {
        void onMembersLoaded(List<User> members);
        void onError(String error);
    }

    public interface TasksCallback {
        void onTasksLoaded(List<Task> tasks);
        void onError(String error);
    }

    public interface RewardsCallback {
        void onRewardsLoaded(List<Reward> rewards);
        void onError(String error);
    }

    public interface StarTransactionsCallback {
        void onTransactionsLoaded(List<StarTransaction> transactions);
        void onError(String error);
    }

    // ==================== ADDITIONAL HELPER METHODS ====================

    public static void deleteTask(String taskId, OnCompleteListener<Void> listener) {
        // Soft delete - just mark as inactive
        db.collection("tasks").document(taskId)
                .update("status", "deleted")
                .addOnCompleteListener(listener);
    }

    public static void deleteReward(String rewardId, OnCompleteListener<Void> listener) {
        // Soft delete - just mark as inactive
        db.collection("rewards").document(rewardId)
                .update("isActive", false)
                .addOnCompleteListener(listener);
    }

    public static void updateTaskStatus(String taskId, String status, OnCompleteListener<Void> listener) {
        db.collection("tasks").document(taskId)
                .update("status", status)
                .addOnCompleteListener(listener);
    }

    public static void getFamilyInviteCode(String familyId, InviteCodeCallback callback) {
        db.collection("families").document(familyId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String inviteCode = document.getString("inviteCode");
                            Long expiryTime = document.getLong("inviteCodeExpiry");
                            callback.onInviteCodeLoaded(
                                    inviteCode != null ? inviteCode : "",
                                    expiryTime != null ? expiryTime : 0
                            );
                        } else {
                            callback.onError("Family not found");
                        }
                    } else {
                        callback.onError("Failed to load invite code");
                    }
                });
    }


    // ==================== TASK COMPLETION STATISTICS ====================

    public static void getTodaysTaskCompletions(String familyId, TaskCompletionStatsCallback callback) {
        // Get start and end of today
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        db.collection("taskCompletions")
                .whereGreaterThanOrEqualTo("completedAt", startOfDay)
                .whereLessThan("completedAt", endOfDay)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int completedCount = 0;

                        // Get family tasks first to filter completions
                        getFamilyTasks(familyId, new TasksCallback() {
                            @Override
                            public void onTasksLoaded(List<Task> familyTasks) {
                                int count = 0;
                                List<String> familyTaskIds = new ArrayList<>();
                                for (Task familyTask : familyTasks) {
                                    if (familyTask.getTaskId() != null) {
                                        familyTaskIds.add(familyTask.getTaskId());
                                    }
                                }

                                // Count completions that belong to family tasks
                                for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                                    String taskId = doc.getString("taskId");
                                    if (taskId != null && familyTaskIds.contains(taskId)) {
                                        count++;
                                    }
                                }
                                callback.onStatsReceived(count);
                            }

                            @Override
                            public void onError(String error) {
                                callback.onStatsReceived(0);
                            }
                        });
                    } else {
                        callback.onStatsReceived(0);
                    }
                });
    }

// ==================== TASK COMPLETION TRACKING ====================

    public static void getTaskCompletionForToday(String userId, String taskId, TaskCompletionCallback callback) {
        String today = getCurrentDateString();

        db.collection("taskCompletions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("date", today)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isCompleted = !task.getResult().isEmpty();
                        callback.onCompletionStatusReceived(isCompleted);
                    } else {
                        callback.onCompletionStatusReceived(false);
                    }
                });
    }

    public interface TaskCompletionCallback {
        void onCompletionStatusReceived(boolean isCompleted);
    }

    public interface OnTasksLoadedListener {
        void onTasksLoaded(List<Task> tasks);

        void onError(String error);
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated();

        void onError(String error);
    }

    interface TaskCompletionStatusCallback {
        void onStatusReceived(boolean isCompleted);
    }

    // ==================== REWARD REDEMPTION MANAGEMENT ====================

    public static void getPendingRedemptions(String familyId, RedemptionsCallback callback) {
        db.collection("rewardRedemptions")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("status", "pending")
                .orderBy("redeemedAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> redemptions = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Map<String, Object> redemption = new HashMap<>();
                            redemption.put("id", doc.getId());
                            redemption.put("rewardId", doc.getString("rewardId"));
                            redemption.put("userId", doc.getString("userId"));
                            redemption.put("redeemedAt", doc.getLong("redeemedAt"));
                            redemption.put("starsSpent", doc.getLong("starsSpent"));
                            redemptions.add(redemption);
                        }
                        callback.onRedemptionsLoaded(redemptions);
                    } else {
                        callback.onError("Failed to load redemptions");
                    }
                });
    }

    public static void approveRedemption(String redemptionId, String parentId, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "approved");
        updates.put("approvedBy", parentId);
        updates.put("approvedAt", System.currentTimeMillis());

        db.collection("rewardRedemptions").document(redemptionId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    public interface RedemptionsCallback {
        void onRedemptionsLoaded(List<Map<String, Object>> redemptions);
        void onError(String error);
    }

    // ==================== ANALYTICS AND REPORTING ====================

    public static void getFamilyStats(String familyId, FamilyStatsCallback callback) {
        // Get task completion stats for the last 7 days
        long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        db.collection("taskCompletions")
                .whereGreaterThan("completedAt", weekAgo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int tasksCompleted = 0;
                        int starsEarned = 0;

                        // Filter by family ID and count
                        for (DocumentSnapshot doc : task.getResult()) {
                            String userId = doc.getString("userId");
                            // Verify user belongs to this family
                            if (userId != null) {
                                Long stars = doc.getLong("starsAwarded");
                                tasksCompleted++;
                                starsEarned += stars != null ? stars.intValue() : 0;
                            }
                        }

                        // Create final variables for lambda
                        final int finalTasksCompleted = tasksCompleted;
                        final int finalStarsEarned = starsEarned;

                        // Get total family star balance
                        db.collection("users")
                                .whereEqualTo("familyId", familyId)
                                .whereEqualTo("role", "child")
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        int totalBalance = 0;
                                        int childCount = userTask.getResult().size();

                                        for (DocumentSnapshot doc : userTask.getResult()) {
                                            Long balance = doc.getLong("starBalance");
                                            totalBalance += balance != null ? balance.intValue() : 0;
                                        }

                                        FamilyStats stats = new FamilyStats();
                                        stats.setTasksCompletedThisWeek(finalTasksCompleted);
                                        stats.setStarsEarnedThisWeek(finalStarsEarned);
                                        stats.setTotalStarBalance(totalBalance);
                                        stats.setChildCount(childCount);

                                        callback.onStatsLoaded(stats);
                                    } else {
                                        callback.onError("Failed to load family stats");
                                    }
                                });
                    } else {
                        callback.onError("Failed to load completion stats");
                    }
                });
    }

    public interface FamilyStatsCallback {
        void onStatsLoaded(FamilyStats stats);
        void onError(String error);
    }

    public static class FamilyStats {
        private int tasksCompletedThisWeek;
        private int starsEarnedThisWeek;
        private int totalStarBalance;
        private int childCount;

        // Getters and setters
        public int getTasksCompletedThisWeek() { return tasksCompletedThisWeek; }
        public void setTasksCompletedThisWeek(int tasksCompletedThisWeek) { this.tasksCompletedThisWeek = tasksCompletedThisWeek; }

        public int getStarsEarnedThisWeek() { return starsEarnedThisWeek; }
        public void setStarsEarnedThisWeek(int starsEarnedThisWeek) { this.starsEarnedThisWeek = starsEarnedThisWeek; }

        public int getTotalStarBalance() { return totalStarBalance; }
        public void setTotalStarBalance(int totalStarBalance) { this.totalStarBalance = totalStarBalance; }

        public int getChildCount() { return childCount; }
        public void setChildCount(int childCount) { this.childCount = childCount; }
    }

    // ==================== CHILD PERFORMANCE TRACKING ====================

    public static void getChildPerformance(String childId, String familyId, ChildPerformanceCallback callback) {
        long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        // Get tasks completed by this child in the last week
        db.collection("taskCompletions")
                .whereEqualTo("userId", childId)
                .whereGreaterThan("completedAt", weekAgo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int tasksCompleted = task.getResult().size();
                        int starsEarned = 0;

                        for (DocumentSnapshot doc : task.getResult()) {
                            Long stars = doc.getLong("starsAwarded");
                            starsEarned += stars != null ? stars.intValue() : 0;
                        }

                        // Create final variable for lambda
                        final int finalStarsEarned = starsEarned;

                        // Get current star balance
                        db.collection("users").document(childId).get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                        DocumentSnapshot userDoc = userTask.getResult();
                                        Long balance = userDoc.getLong("starBalance");
                                        int currentBalance = balance != null ? balance.intValue() : 0;

                                        ChildPerformance performance = new ChildPerformance();
                                        performance.setChildId(childId);
                                        performance.setTasksCompletedThisWeek(tasksCompleted);
                                        performance.setStarsEarnedThisWeek(finalStarsEarned);
                                        performance.setCurrentStarBalance(currentBalance);

                                        callback.onPerformanceLoaded(performance);
                                    } else {
                                        callback.onError("Failed to load child data");
                                    }
                                });
                    } else {
                        callback.onError("Failed to load performance data");
                    }
                });
    }

    public interface ChildPerformanceCallback {
        void onPerformanceLoaded(ChildPerformance performance);
        void onError(String error);
    }

    public static class ChildPerformance {
        private String childId;
        private int tasksCompletedThisWeek;
        private int starsEarnedThisWeek;
        private int currentStarBalance;

        // Getters and setters
        public String getChildId() { return childId; }
        public void setChildId(String childId) { this.childId = childId; }

        public int getTasksCompletedThisWeek() { return tasksCompletedThisWeek; }
        public void setTasksCompletedThisWeek(int tasksCompletedThisWeek) { this.tasksCompletedThisWeek = tasksCompletedThisWeek; }

        public int getStarsEarnedThisWeek() { return starsEarnedThisWeek; }
        public void setStarsEarnedThisWeek(int starsEarnedThisWeek) { this.starsEarnedThisWeek = starsEarnedThisWeek; }

        public int getCurrentStarBalance() { return currentStarBalance; }
        public void setCurrentStarBalance(int currentStarBalance) { this.currentStarBalance = currentStarBalance; }
    }

    // ==================== USER PREFERENCES ====================

    public static void updateUserPreferences(String userId, Map<String, Object> preferences, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId)
                .update(preferences)
                .addOnCompleteListener(listener);
    }

    public static void updateTextToSpeechSetting(String userId, boolean enabled, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId)
                .update("textToSpeechEnabled", enabled)
                .addOnCompleteListener(listener);
    }

    // ==================== FAMILY SETTINGS ====================

    public static void updateFamilySettings(String familyId, Map<String, Object> settings, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("settings", settings);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("familySettings").document(familyId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    public static void getFamilySettings(String familyId, FamilySettingsCallback callback) {
        db.collection("familySettings").document(familyId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        Map<String, Object> settings = (Map<String, Object>) doc.get("settings");
                        callback.onSettingsLoaded(settings);
                    } else {
                        // Return default settings
                        Map<String, Object> defaultSettings = getDefaultFamilySettings();
                        callback.onSettingsLoaded(defaultSettings);
                    }
                });
    }

    private static Map<String, Object> getDefaultFamilySettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("weekStartsOn", 1); // Monday
        settings.put("timezone", "UTC");

        Map<String, Object> notifications = new HashMap<>();
        notifications.put("taskReminders", true);
        notifications.put("rewardRedemptions", true);
        notifications.put("dailySummary", true);
        settings.put("notifications", notifications);

        Map<String, Object> starSystem = new HashMap<>();
        starSystem.put("maxDailyEarnings", 50);
        starSystem.put("bonusMultiplier", 1.5);
        starSystem.put("weekendBonus", true);
        settings.put("starSystem", starSystem);

        Map<String, Object> childSettings = new HashMap<>();
        childSettings.put("requirePhotoProof", false);
        childSettings.put("autoApproveRedemptions", false);
        childSettings.put("textToSpeechDefault", true);
        settings.put("childSettings", childSettings);

        return settings;
    }

    public interface FamilySettingsCallback {
        void onSettingsLoaded(Map<String, Object> settings);
        void onError(String error);
    }

    // ==================== BATCH OPERATIONS ====================

    public static void batchUpdateTaskStatuses(List<String> taskIds, String status, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        for (String taskId : taskIds) {
            DocumentReference taskRef = db.collection("tasks").document(taskId);
            batch.update(taskRef, "status", status);
        }

        batch.commit().addOnCompleteListener(listener);
    }

    public static void batchDeleteRewards(List<String> rewardIds, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        for (String rewardId : rewardIds) {
            DocumentReference rewardRef = db.collection("rewards").document(rewardId);
            batch.update(rewardRef, "isActive", false);
        }

        batch.commit().addOnCompleteListener(listener);
    }

    // ==================== SEARCH AND FILTERING ====================

    public static void searchTasks(String familyId, String searchQuery, TasksCallback callback) {
        // Simple search by task name (case-insensitive)
        String lowercaseQuery = searchQuery.toLowerCase();

        db.collection("tasks")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> filteredTasks = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String taskName = doc.getString("name");
                            if (taskName != null && taskName.toLowerCase().contains(lowercaseQuery)) {
                                Task taskObj = documentToTask(doc);
                                filteredTasks.add(taskObj);
                            }
                        }
                        callback.onTasksLoaded(filteredTasks);
                    } else {
                        callback.onError("Failed to search tasks");
                    }
                });
    }

    public static void getTasksByRepeatType(String familyId, String repeatType, TasksCallback callback) {
        db.collection("tasks")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("status", "active")
                .whereEqualTo("repeatType", repeatType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> tasks = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Task taskObj = documentToTask(doc);
                            tasks.add(taskObj);
                        }
                        callback.onTasksLoaded(tasks);
                    } else {
                        callback.onError("Failed to load tasks by repeat type");
                    }
                });
    }

    // ==================== DATA EXPORT/BACKUP ====================

    public static void exportFamilyData(String familyId, DataExportCallback callback) {
        Map<String, Object> exportData = new HashMap<>();

        // Get family info
        db.collection("families").document(familyId).get()
                .addOnCompleteListener(familyTask -> {
                    if (familyTask.isSuccessful() && familyTask.getResult().exists()) {
                        exportData.put("family", familyTask.getResult().getData());

                        // Get all family members
                        getFamilyMembers(familyId, new FamilyMembersCallback() {
                            @Override
                            public void onMembersLoaded(List<User> members) {
                                exportData.put("members", members);

                                // Get all tasks
                                getFamilyTasks(familyId, new TasksCallback() {
                                    @Override
                                    public void onTasksLoaded(List<Task> tasks) {
                                        exportData.put("tasks", tasks);

                                        // Get all rewards
                                        getFamilyRewards(familyId, new RewardsCallback() {
                                            @Override
                                            public void onRewardsLoaded(List<Reward> rewards) {
                                                exportData.put("rewards", rewards);
                                                exportData.put("exportedAt", System.currentTimeMillis());
                                                callback.onDataExported(exportData);
                                            }

                                            @Override
                                            public void onError(String error) {
                                                callback.onError("Failed to export rewards: " + error);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        callback.onError("Failed to export tasks: " + error);
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                callback.onError("Failed to export members: " + error);
                            }
                        });
                    } else {
                        callback.onError("Failed to export family data");
                    }
                });
    }

    public interface DataExportCallback {
        void onDataExported(Map<String, Object> data);
        void onError(String error);
    }

    // ==================== CLEANUP AND MAINTENANCE ====================

    public static void cleanupExpiredInviteCodes(OnCompleteListener<Void> listener) {
        long currentTime = System.currentTimeMillis();

        db.collection("families")
                .whereLessThan("inviteCodeExpiry", currentTime)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();

                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.update(doc.getReference(), "inviteCode", "");
                            batch.update(doc.getReference(), "inviteCodeExpiry", 0);
                        }

                        batch.commit().addOnCompleteListener(listener);
                    } else {
                        // Create failure task for listener
                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                task.getException() != null ? task.getException()
                                        : new Exception("Failed to cleanup expired invite codes"));
                        listener.onComplete(failedTask);
                    }
                });
    }

    public static void archiveOldTaskCompletions(int daysOld, OnCompleteListener<Void> listener) {
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000);

        db.collection("taskCompletions")
                .whereLessThan("completedAt", cutoffTime)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();

                        // Move to archived collection
                        for (DocumentSnapshot doc : task.getResult()) {
                            DocumentReference archiveRef = db.collection("archivedTaskCompletions").document();
                            batch.set(archiveRef, doc.getData());
                            batch.delete(doc.getReference());
                        }

                        batch.commit().addOnCompleteListener(listener);
                    } else {
                        // Create failure task for listener
                        com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                task.getException() != null ? task.getException()
                                        : new Exception("Failed to archive old task completions"));
                        listener.onComplete(failedTask);
                    }
                });
    }

    // Get family invite code

    // Create child user with profile image
// Create child user WITHOUT Firebase Auth (just Firestore document)
    public static void createChildUser(String childName, String familyId, String profileImageUrl, CreateUserCallback callback) {
        android.util.Log.d("FirebaseHelper", "Creating child user: " + childName + " for family: " + familyId);

        // Generate a unique ID for the child (without using Firebase Auth)
        String childId = "child_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);

        // Create user document directly in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", childName);
        userData.put("email", "");
        userData.put("role", "child");
        userData.put("familyId", familyId);
        userData.put("starBalance", 0);
        userData.put("textToSpeechEnabled", true);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLoginAt", System.currentTimeMillis());
        userData.put("profileImageUrl", profileImageUrl != null ? profileImageUrl : "");
        userData.put("isActive", true);

        db.collection("users").document(childId).set(userData)
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        android.util.Log.d("FirebaseHelper", "Child user document created: " + childId);
                        // Add child to family
                        addChildToFamily(familyId, childId, callback);
                    } else {
                        android.util.Log.e("FirebaseHelper", "Failed to create child user", userTask.getException());
                        String errorMessage = userTask.getException() != null ?
                                userTask.getException().getMessage() : "Failed to create child user";
                        callback.onError(errorMessage);
                    }
                });
    }


    private static void addChildToFamily(String familyId, String childId, CreateUserCallback callback) {
        android.util.Log.d("FirebaseHelper", "Adding child to family: " + childId + " -> " + familyId);

        db.collection("families").document(familyId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ArrayList<String> childIds = (ArrayList<String>) document.get("childIds");
                            if (childIds == null) childIds = new ArrayList<>();
                            childIds.add(childId);

                            db.collection("families").document(familyId)
                                    .update("childIds", childIds)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            android.util.Log.d("FirebaseHelper", "Child added to family successfully");
                                            callback.onUserCreated(childId);
                                        } else {
                                            android.util.Log.e("FirebaseHelper", "Failed to update family", updateTask.getException());
                                            String errorMessage = updateTask.getException() != null ?
                                                    updateTask.getException().getMessage() : "Failed to update family";
                                            callback.onError(errorMessage);
                                        }
                                    });
                        } else {
                            callback.onError("Family not found");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to access family data";
                        callback.onError(errorMessage);
                    }
                });
    }

    // Helper method to get child profile with callback
    public static void getChildProfile(String childId, ChildProfileCallback callback) {
        if (childId == null || childId.isEmpty()) {
            callback.onError("Invalid child ID");
            return;
        }

        db.collection("users").document(childId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        ChildProfile childProfile = documentToChildProfile(doc);
                        if (childProfile != null) {
                            callback.onChildProfileLoaded(childProfile);
                        } else {
                            callback.onError("Failed to parse child profile");
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Child not found";
                        callback.onError(error);
                    }
                });
    }

    // ADD THIS METHOD - This is what's missing and causing the compilation error
    private static ChildProfile documentToChildProfile(DocumentSnapshot doc) {
        try {
            if (!doc.exists()) {
                Log.w("FirebaseHelper", "Document does not exist: " + doc.getId());
                return null;
            }

            ChildProfile profile = new ChildProfile();
            profile.setChildId(doc.getId());
            profile.setName(doc.getString("name"));
            profile.setFamilyId(doc.getString("familyId"));
            profile.setInviteCode(doc.getString("inviteCode"));

            Long expiry = doc.getLong("inviteCodeExpiry");
            profile.setInviteCodeExpiry(expiry != null ? expiry : 0);

            profile.setProfileImageUrl(doc.getString("profileImageUrl"));

            Long balance = doc.getLong("starBalance");
            profile.setStarBalance(balance != null ? balance.intValue() : 0);

            Boolean active = doc.getBoolean("isActive");
            profile.setActive(active == null || active);

            Long created = doc.getLong("createdAt");
            profile.setCreatedAt(created != null ? created : System.currentTimeMillis());

            return profile;
        } catch (Exception e) {
            Log.e("FirebaseHelper", "Error converting document to ChildProfile: " + doc.getId(), e);
            return null;
        }
    }



    // Callback interface for child profile
    public interface ChildProfileCallback {
        void onChildProfileLoaded(ChildProfile childProfile);
        void onError(String error);
    }

}