package com.chores.app.kids.chores_app_for_kids.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chores.app.kids.chores_app_for_kids.data.models.Family;
import com.chores.app.kids.chores_app_for_kids.data.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for family operations
 */
public class FamilyRepository {

    private DatabaseReference databaseReference;
    private DatabaseReference familiesRef;
    private DatabaseReference usersRef;

    public FamilyRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        familiesRef = databaseReference.child("families");
        usersRef = databaseReference.child("users");
    }

    /**
     * Get family details
     */
    public void getFamily(String familyId, final GetFamilyCallback callback) {
        familiesRef.child(familyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Family family = snapshot.getValue(Family.class);
                    callback.onSuccess(family);
                } else {
                    callback.onError(new Exception("Family not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get family members
     */
    public void getFamilyMembers(String familyId, final GetMembersCallback callback) {
        // First get family to get member IDs
        familiesRef.child(familyId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<String> memberIds = new ArrayList<>();
                    for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                        memberIds.add(memberSnapshot.getKey());
                    }

                    // Now get user details for each member
                    getMemberDetails(memberIds, callback);
                } else {
                    callback.onSuccess(new ArrayList<>(), new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get member details
     */
    private void getMemberDetails(List<String> memberIds, final GetMembersCallback callback) {
        final List<User> adults = new ArrayList<>();
        final List<User> kids = new ArrayList<>();
        final int[] completedRequests = {0};

        if (memberIds.isEmpty()) {
            callback.onSuccess(adults, kids);
            return;
        }

        for (String memberId : memberIds) {
            usersRef.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.isParent()) {
                                adults.add(user);
                            } else {
                                kids.add(user);
                            }
                        }
                    }

                    completedRequests[0]++;
                    if (completedRequests[0] == memberIds.size()) {
                        callback.onSuccess(adults, kids);
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
     * Add kid to family
     */
    public void addKidToFamily(String familyId, String kidName, final AddKidCallback callback) {
        // Generate kid ID and invite code
        String kidId = usersRef.push().getKey();

        // Update family with new kid invite code
        familiesRef.child(familyId).child("kidInviteCodes").child(kidId).setValue(generateInviteCode())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(kidId);
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
     * Update family name
     */
    public void updateFamilyName(String familyId, String newName, final UpdateCallback callback) {
        familiesRef.child(familyId).child("familyName").setValue(newName)
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
     * Remove member from family
     */
    public void removeMemberFromFamily(String familyId, String memberId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/families/" + familyId + "/members/" + memberId, null);
        updates.put("/families/" + familyId + "/kidInviteCodes/" + memberId, null);
        updates.put("/users/" + memberId, null);

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

    /**
     * Get kid invite code
     */
    public void getKidInviteCode(String familyId, String kidId, final GetInviteCodeCallback callback) {
        familiesRef.child(familyId).child("kidInviteCodes").child(kidId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String inviteCode = snapshot.getValue(String.class);
                            callback.onSuccess(inviteCode);
                        } else {
                            callback.onError(new Exception("Invite code not found"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(new Exception(error.getMessage()));
                    }
                });
    }

    /**
     * Regenerate kid invite code
     */
    public void regenerateKidInviteCode(String familyId, String kidId, final GetInviteCodeCallback callback) {
        String newCode = generateInviteCode();

        familiesRef.child(familyId).child("kidInviteCodes").child(kidId).setValue(newCode)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(newCode);
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
     * Generate random 6-digit invite code
     */
    private String generateInviteCode() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    // Callback interfaces
    public interface GetFamilyCallback {
        void onSuccess(Family family);
        void onError(Exception e);
    }

    public interface GetMembersCallback {
        void onSuccess(List<User> adults, List<User> kids);
        void onError(Exception e);
    }

    public interface AddKidCallback {
        void onSuccess(String kidId);
        void onError(Exception e);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface GetInviteCodeCallback {
        void onSuccess(String inviteCode);
        void onError(Exception e);
    }
}