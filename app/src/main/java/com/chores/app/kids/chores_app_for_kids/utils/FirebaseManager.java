package com.chores.app.kids.chores_app_for_kids.utils;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.chores.app.kids.chores_app_for_kids.models.*;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // User Management
    public void createUser(User user, OnCompleteListener<Void> listener) {
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user.toMap())
                .addOnCompleteListener(listener);
    }

    public void getUser(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateUser(String userId, Map<String, Object> updates, OnCompleteListener<Void> listener) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Family Management
    public void createFamily(Family family, OnCompleteListener<Void> listener) {
        // Generate invite code
        String inviteCode = generateInviteCode();
        family.setInviteCode(inviteCode);

        db.collection(Constants.COLLECTION_FAMILIES)
                .document(family.getFamilyId())
                .set(family.toMap())
                .addOnCompleteListener(listener);
    }

    public void getFamily(String familyId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.COLLECTION_FAMILIES)
                .document(familyId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getFamilyByInviteCode(String inviteCode, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_FAMILIES)
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .addOnCompleteListener(listener);
    }

    public void joinFamily(String familyId, String userId, OnCompleteListener<Void> listener) {
        DocumentReference familyRef = db.collection(Constants.COLLECTION_FAMILIES).document(familyId);

        db.runTransaction(transaction -> {
            DocumentSnapshot familySnapshot = transaction.get(familyRef);
            Family family = familySnapshot.toObject(Family.class);

            if (family != null && !family.getMembers().contains(userId)) {
                family.getMembers().add(userId);
                family.setUpdatedAt(System.currentTimeMillis());
                transaction.set(familyRef, family.toMap());
            }
            return null;
        }).addOnCompleteListener(listener);
    }

    public String generateInviteCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    public void regenerateInviteCode(String familyId, OnCompleteListener<String> listener) {
        String newCode = generateInviteCode();
        Map<String, Object> updates = new HashMap<>();
        updates.put("inviteCode", newCode);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(Constants.COLLECTION_FAMILIES)
                .document(familyId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onComplete(null); // You'd need to modify this interface to return the code
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onComplete(null);
                    }
                });
    }

    // Kid Management
    public void createKid(Kid kid, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        // Add kid to kids collection
        DocumentReference kidRef = db.collection(Constants.COLLECTION_KIDS).document(kid.getKidId());
        batch.set(kidRef, kid.toMap());

        // Add kid to family's kids list
        DocumentReference familyRef = db.collection(Constants.COLLECTION_FAMILIES).document(kid.getFamilyId());
        batch.update(familyRef, "kids", com.google.firebase.firestore.FieldValue.arrayUnion(kid.getKidId()));
        batch.update(familyRef, "updatedAt", System.currentTimeMillis());

        batch.commit().addOnCompleteListener(listener);
    }

    public void getKid(String kidId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(Constants.COLLECTION_KIDS)
                .document(kidId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getFamilyKids(String familyId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_KIDS)
                .whereEqualTo("familyId", familyId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateKidStarBalance(String kidId, int newBalance, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("starBalance", newBalance);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(Constants.COLLECTION_KIDS)
                .document(kidId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Task Management
    public void createTask(Task task, OnCompleteListener<Void> listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .document(task.getTaskId())
                .set(task.toMap())
                .addOnCompleteListener(listener);
    }

    public void getFamilyTasks(String familyId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getKidTasks(String kidId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .whereArrayContains("assignedKids", kidId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    // Task Completion
    public void completeTask(TaskCompletion completion, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        // Add task completion record
        DocumentReference completionRef = db.collection(Constants.COLLECTION_TASK_COMPLETIONS)
                .document(completion.getCompletionId());
        batch.set(completionRef, completion.toMap());

        // Create star transaction
        StarTransaction transaction = new StarTransaction(
                db.collection(Constants.COLLECTION_STAR_TRANSACTIONS).document().getId(),
                completion.getKidId(),
                completion.getFamilyId(),
                Constants.TRANSACTION_EARNED,
                completion.getStarsEarned(),
                "Task completed: " + completion.getTaskId()
        );
        transaction.setRelatedId(completion.getTaskId());

        DocumentReference transactionRef = db.collection(Constants.COLLECTION_STAR_TRANSACTIONS)
                .document(transaction.getTransactionId());
        batch.set(transactionRef, transaction.toMap());

        batch.commit().addOnCompleteListener(listener);
    }

    // Reward Management
    public void createReward(Reward reward, OnCompleteListener<Void> listener) {
        db.collection(Constants.COLLECTION_REWARDS)
                .document(reward.getRewardId())
                .set(reward.toMap())
                .addOnCompleteListener(listener);
    }

    public void getFamilyRewards(String familyId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_REWARDS)
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getKidRewards(String kidId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_REWARDS)
                .whereArrayContains("availableForKids", kidId)
                .whereEqualTo("isActive", true)
                .orderBy("starCost", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    // Reward Redemption
    public void redeemReward(RewardRedemption redemption, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        // Add redemption record
        DocumentReference redemptionRef = db.collection(Constants.COLLECTION_REWARD_REDEMPTIONS)
                .document(redemption.getRedemptionId());
        batch.set(redemptionRef, redemption.toMap());

        // Create star transaction (deduct stars)
        StarTransaction transaction = new StarTransaction(
                db.collection(Constants.COLLECTION_STAR_TRANSACTIONS).document().getId(),
                redemption.getKidId(),
                redemption.getFamilyId(),
                Constants.TRANSACTION_SPENT,
                -redemption.getStarsSpent(), // Negative amount for spending
                "Reward redeemed: " + redemption.getRewardId()
        );
        transaction.setRelatedId(redemption.getRewardId());

        DocumentReference transactionRef = db.collection(Constants.COLLECTION_STAR_TRANSACTIONS)
                .document(transaction.getTransactionId());
        batch.set(transactionRef, transaction.toMap());

        batch.commit().addOnCompleteListener(listener);
    }

    // Star Transaction Management
    public void getStarHistory(String kidId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(Constants.COLLECTION_STAR_TRANSACTIONS)
                .whereEqualTo("kidId", kidId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(listener);
    }

    public void adjustStarBalance(String kidId, String familyId, int amount, String reason,
                                  String createdBy, OnCompleteListener<Void> listener) {
        StarTransaction transaction = new StarTransaction(
                db.collection(Constants.COLLECTION_STAR_TRANSACTIONS).document().getId(),
                kidId,
                familyId,
                Constants.TRANSACTION_ADJUSTMENT,
                amount,
                reason
        );
        transaction.setCreatedBy(createdBy);

        db.collection(Constants.COLLECTION_STAR_TRANSACTIONS)
                .document(transaction.getTransactionId())
                .set(transaction.toMap())
                .addOnCompleteListener(listener);
    }

    // Storage Management
    public StorageReference getProfileImageRef(String userId) {
        return storage.getReference().child(Constants.STORAGE_PROFILE_IMAGES + userId + ".jpg");
    }

    public StorageReference getTaskProofRef(String taskCompletionId) {
        return storage.getReference().child(Constants.STORAGE_TASK_PROOFS + taskCompletionId + ".jpg");
    }
}
