package com.chores.app.kids.chores_app_for_kids.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.chores.app.kids.chores_app_for_kids.data.models.Reward;
import com.chores.app.kids.chores_app_for_kids.data.models.RewardRedemption;
import com.chores.app.kids.chores_app_for_kids.data.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for reward operations
 */
public class RewardRepository {

    private DatabaseReference databaseReference;
    private DatabaseReference rewardsRef;
    private DatabaseReference rewardRedemptionsRef;
    private DatabaseReference usersRef;

    public RewardRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        rewardsRef = databaseReference.child("rewards");
        rewardRedemptionsRef = databaseReference.child("rewardRedemptions");
        usersRef = databaseReference.child("users");
    }

    /**
     * Create new reward
     */
    public void createReward(Reward reward, final CreateRewardCallback callback) {
        String rewardId = rewardsRef.push().getKey();
        reward.setRewardId(rewardId);

        rewardsRef.child(rewardId).setValue(reward)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(reward);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Get rewards for family
     */
    public void getRewardsForFamily(String familyId, final GetRewardsCallback callback) {
        Query query = rewardsRef.orderByChild("familyId").equalTo(familyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Reward> rewards = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reward reward = snapshot.getValue(Reward.class);
                    if (reward != null && reward.isActive()) {
                        rewards.add(reward);
                    }
                }
                callback.onSuccess(rewards);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get rewards for kid
     */
    public void getRewardsForKid(String kidId, String familyId, final GetRewardsCallback callback) {
        Query query = rewardsRef.orderByChild("familyId").equalTo(familyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Reward> rewards = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reward reward = snapshot.getValue(Reward.class);
                    if (reward != null && reward.isActive() &&
                            reward.getAssignedTo() != null &&
                            reward.getAssignedTo().contains(kidId)) {
                        rewards.add(reward);
                    }
                }
                callback.onSuccess(rewards);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Redeem reward
     */
    public void redeemReward(String rewardId, String kidId, final RedeemRewardCallback callback) {
        // First check if kid has enough stars
        rewardsRef.child(rewardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Reward reward = snapshot.getValue(Reward.class);
                    if (reward != null) {
                        checkStarsAndRedeem(reward, kidId, callback);
                    } else {
                        callback.onError(new Exception("Reward not found"));
                    }
                } else {
                    callback.onError(new Exception("Reward not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Check stars and redeem
     */
    private void checkStarsAndRedeem(Reward reward, String kidId, final RedeemRewardCallback callback) {
        usersRef.child(kidId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User kid = snapshot.getValue(User.class);
                    if (kid != null) {
                        if (kid.getStarWallet() >= reward.getStarsRequired()) {
                            // Sufficient stars, proceed with redemption
                            createRedemption(reward, kid, callback);
                        } else {
                            callback.onError(new Exception("Insufficient stars"));
                        }
                    } else {
                        callback.onError(new Exception("User not found"));
                    }
                } else {
                    callback.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Create redemption record
     */
    private void createRedemption(Reward reward, User kid, final RedeemRewardCallback callback) {
        // Create redemption
        RewardRedemption redemption = new RewardRedemption(
                reward.getRewardId(),
                kid.getUserId(),
                reward.getStarsRequired(),
                reward.getRewardName(),
                kid.getName()
        );

        String redemptionId = rewardRedemptionsRef.push().getKey();
        redemption.setRedemptionId(redemptionId);

        // Update kid's star wallet
        int newStarBalance = kid.getStarWallet() - reward.getStarsRequired();

        Map<String, Object> updates = new HashMap<>();
        updates.put("/rewardRedemptions/" + redemptionId, redemption.toMap());
        updates.put("/users/" + kid.getUserId() + "/starWallet", newStarBalance);

        databaseReference.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(redemption, newStarBalance);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Get pending redemptions for family
     */
    public void getPendingRedemptions(String familyId, final GetRedemptionsCallback callback) {
        // First get all family kids
        Query query = usersRef.orderByChild("familyId").equalTo(familyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> kidIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.isKid()) {
                        kidIds.add(user.getUserId());
                    }
                }

                // Now get redemptions for these kids
                getRedemptionsForKids(kidIds, "pending", callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get redemptions for kids
     */
    private void getRedemptionsForKids(List<String> kidIds, String status,
                                       final GetRedemptionsCallback callback) {
        final List<RewardRedemption> redemptions = new ArrayList<>();
        final int[] completedRequests = {0};

        if (kidIds.isEmpty()) {
            callback.onSuccess(redemptions);
            return;
        }

        for (String kidId : kidIds) {
            Query query = rewardRedemptionsRef.orderByChild("kidId").equalTo(kidId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RewardRedemption redemption = snapshot.getValue(RewardRedemption.class);
                        if (redemption != null && redemption.getStatus().equals(status)) {
                            redemptions.add(redemption);
                        }
                    }

                    completedRequests[0]++;
                    if (completedRequests[0] == kidIds.size()) {
                        callback.onSuccess(redemptions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(new Exception(error.getMessage()));
                }
            });
        }
    }

    /**
     * Approve redemption
     */
    public void approveRedemption(String redemptionId, String parentId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "approved");
        updates.put("approvedBy", parentId);

        rewardRedemptionsRef.child(redemptionId).updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Reject redemption and refund stars
     */
    public void rejectRedemption(String redemptionId, String parentId, final UpdateCallback callback) {
        // First get redemption details
        rewardRedemptionsRef.child(redemptionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RewardRedemption redemption = snapshot.getValue(RewardRedemption.class);
                    if (redemption != null) {
                        refundStarsAndReject(redemption, parentId, callback);
                    } else {
                        callback.onError(new Exception("Redemption not found"));
                    }
                } else {
                    callback.onError(new Exception("Redemption not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Refund stars and reject
     */
    private void refundStarsAndReject(RewardRedemption redemption, String parentId,
                                      final UpdateCallback callback) {
        // Get current star balance
        usersRef.child(redemption.getKidId()).child("starWallet")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int currentStars = 0;
                        if (snapshot.exists()) {
                            currentStars = snapshot.getValue(Integer.class);
                        }

                        int newStars = currentStars + redemption.getStarsUsed();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("/rewardRedemptions/" + redemption.getRedemptionId() + "/status", "rejected");
                        updates.put("/rewardRedemptions/" + redemption.getRedemptionId() + "/approvedBy", parentId);
                        updates.put("/users/" + redemption.getKidId() + "/starWallet", newStars);

                        databaseReference.updateChildren(updates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        callback.onSuccess();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        callback.onError(e);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(new Exception(error.getMessage()));
                    }
                });
    }

    /**
     * Update reward
     */
    public void updateReward(Reward reward, final UpdateCallback callback) {
        rewardsRef.child(reward.getRewardId()).setValue(reward)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Delete reward (soft delete)
     */
    public void deleteReward(String rewardId, final UpdateCallback callback) {
        rewardsRef.child(rewardId).child("isActive").setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    // Callback interfaces
    public interface CreateRewardCallback {
        void onSuccess(Reward reward);
        void onError(Exception e);
    }

    public interface GetRewardsCallback {
        void onSuccess(List<Reward> rewards);
        void onError(Exception e);
    }

    public interface RedeemRewardCallback {
        void onSuccess(RewardRedemption redemption, int newStarBalance);
        void onError(Exception e);
    }

    public interface GetRedemptionsCallback {
        void onSuccess(List<RewardRedemption> redemptions);
        void onError(Exception e);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
