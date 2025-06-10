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

import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.models.StarTransaction;
import java.util.ArrayList;
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
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours

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

    public static void getFamilyChildren(String familyId, FamilyChildrenCallback callback) {
        db.collection("users")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("role", "child")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> children = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            User child = new User();
                            child.setUserId(doc.getId());
                            child.setName(doc.getString("name"));
                            child.setRole(doc.getString("role"));
                            child.setFamilyId(doc.getString("familyId"));
                            Long balance = doc.getLong("starBalance");
                            child.setStarBalance(balance != null ? balance.intValue() : 0);
                            children.add(child);
                        }
                        callback.onChildrenLoaded(children);
                    } else {
                        callback.onError("Failed to load children");
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
                            Long balance = doc.getLong("starBalance");
                            member.setStarBalance(balance != null ? balance.intValue() : 0);
                            members.add(member);
                        }
                        callback.onMembersLoaded(members);
                    } else {
                        callback.onError("Failed to load family members");
                    }
                });
    }

    // ==================== TASK MANAGEMENT ====================

    public static void addTask(com.chores.app.kids.chores_app_for_kids.models.Task task, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("name", task.getName());
        taskData.put("iconName", task.getIconName());
        taskData.put("starReward", task.getStarReward());
        taskData.put("assignedKids", task.getAssignedKids());
        taskData.put("familyId", task.getFamilyId());
        taskData.put("createdBy", task.getCreatedBy());
        taskData.put("startDate", task.getStartDate());
        taskData.put("repeatType", task.getRepeatType());
        taskData.put("reminderTime", task.getReminderTime());
        taskData.put("photoProofRequired", task.isPhotoProofRequired());
        taskData.put("status", task.getStatus());
        taskData.put("createdTimestamp", task.getCreatedTimestamp());

        db.collection("tasks")
                .add(taskData)
                .addOnCompleteListener(listener);
    }

    public static void getFamilyTasks(String familyId, TasksCallback callback) {
        db.collection("tasks")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("status", "active")
                .orderBy("createdTimestamp", Query.Direction.DESCENDING)
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

    // ==================== REWARD MANAGEMENT ====================

    public static void addReward(Reward reward, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> rewardData = new HashMap<>();
        rewardData.put("name", reward.getName());
        rewardData.put("iconName", reward.getIconName());
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

                                        if (currentBalance >= cost) {
                                            // Create redemption record
                                            Map<String, Object> redemptionData = new HashMap<>();
                                            redemptionData.put("rewardId", rewardId);
                                            redemptionData.put("userId", userId);
                                            redemptionData.put("familyId", familyId);
                                            redemptionData.put("redeemedAt", System.currentTimeMillis());
                                            redemptionData.put("starsSpent", cost);
                                            redemptionData.put("status", "pending");

                                            db.collection("rewardRedemptions").add(redemptionData)
                                                    .addOnCompleteListener(redemptionResult -> {
                                                        if (redemptionResult.isSuccessful()) {
                                                            // Deduct stars from user balance
                                                            updateStarBalance(userId, -cost, familyId, "Redeemed: " + rewardName, null, rewardId);
                                                            // Create success task for listener
                                                            com.google.android.gms.tasks.Task<Void> successTask = com.google.android.gms.tasks.Tasks.forResult(null);
                                                            listener.onComplete(successTask);
                                                        } else {
                                                            // Create failure task for listener
                                                            com.google.android.gms.tasks.Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                                                                    redemptionResult.getException() != null ? redemptionResult.getException()
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
        task.setIconName(doc.getString("iconName"));

        Long starReward = doc.getLong("starReward");
        task.setStarReward(starReward != null ? starReward.intValue() : 0);

        List<String> assignedKids = (List<String>) doc.get("assignedKids");
        task.setAssignedKids(assignedKids != null ? assignedKids : new ArrayList<>());

        task.setFamilyId(doc.getString("familyId"));
        task.setCreatedBy(doc.getString("createdBy"));
        task.setStartDate(doc.getString("startDate"));
        task.setRepeatType(doc.getString("repeatType"));
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

    // ==================== CALLBACK INTERFACES ====================

    public interface StarBalanceCallback {
        void onStarBalanceReceived(int balance);
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
    // Callback interfaces
    public interface InviteCodeCallback {
        void onInviteCodeLoaded(String inviteCode, long expiryTime);
        void onError(String error);
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



}
