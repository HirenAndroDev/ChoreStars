package com.chores.app.kids.chores_app_for_kids.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chores.app.kids.chores_app_for_kids.data.models.Family;
import com.chores.app.kids.chores_app_for_kids.data.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class for authentication operations
 */
public class AuthRepository {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private DatabaseReference familiesRef;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        usersRef = databaseReference.child("users");
        familiesRef = databaseReference.child("families");
    }

    /**
     * Sign in with Google account
     */
    public void signInWithGoogle(GoogleSignInAccount account, final AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            // Check if user exists in database
                            checkUserExists(firebaseUser, account, callback);
                        }
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
     * Check if user exists in database
     */
    private void checkUserExists(final FirebaseUser firebaseUser, final GoogleSignInAccount account,
                                 final AuthCallback callback) {
        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User exists, retrieve user data
                    User user = snapshot.getValue(User.class);
                    callback.onSuccess(user, false);
                } else {
                    // New user, create user data
                    createNewParentUser(firebaseUser, account, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Create new parent user
     */
    private void createNewParentUser(FirebaseUser firebaseUser, GoogleSignInAccount account,
                                     final AuthCallback callback) {
        // Create family first
        String familyId = familiesRef.push().getKey();
        String userId = firebaseUser.getUid();

        // Create family object
        Family family = new Family(familyId, account.getDisplayName() + "'s Family", userId);

        // Create user object
        User user = new User(userId, account.getEmail(), account.getDisplayName(), familyId);
        user.setGoogleId(account.getId());
        if (account.getPhotoUrl() != null) {
            user.setProfileImage(account.getPhotoUrl().toString());
        }

        // Save family and user to database
        Map<String, Object> updates = new HashMap<>();
        updates.put("/families/" + familyId, family.toMap());
        updates.put("/users/" + userId, user.toMap());

        databaseReference.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(user, true);
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
     * Join family with invite code
     */
    public void joinFamilyWithCode(final String inviteCode, final String kidName,
                                   final JoinFamilyCallback callback) {
        // Search for family with matching kid invite code
        familiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean codeFound = false;

                for (DataSnapshot familySnapshot : dataSnapshot.getChildren()) {
                    Family family = familySnapshot.getValue(Family.class);

                    if (family != null && family.getKidInviteCodes() != null) {
                        // Check kid invite codes
                        for (Map.Entry<String, String> entry : family.getKidInviteCodes().entrySet()) {
                            if (entry.getValue().equals(inviteCode)) {
                                // Code found, create kid account
                                codeFound = true;
                                String kidId = entry.getKey();
                                createKidAccount(kidId, kidName, family.getFamilyId(),
                                        family.getOwnerId(), callback);
                                return;
                            }
                        }
                    }
                }

                if (!codeFound) {
                    callback.onError(new Exception("Invalid invite code"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Create kid account
     */
    private void createKidAccount(String kidId, String kidName, String familyId,
                                  String parentId, final JoinFamilyCallback callback) {
        // Create kid user
        User kidUser = new User(kidId, kidName, familyId, parentId);

        // Update family members
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + kidId, kidUser.toMap());
        updates.put("/families/" + familyId + "/members/" + kidId, true);

        databaseReference.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(kidUser, familyId);
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
     * Get current user
     */
    public void getCurrentUser(String userId, final GetUserCallback callback) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    callback.onSuccess(user);
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
     * Update FCM token
     */
    public void updateFCMToken(String userId, String token) {
        usersRef.child(userId).child("fcmToken").setValue(token);
    }

    /**
     * Update user online status
     */
    public void updateUserOnlineStatus(String userId, boolean isOnline) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isOnline", isOnline);
        updates.put("lastSeenAt", System.currentTimeMillis());

        usersRef.child(userId).updateChildren(updates);
    }

    /**
     * Sign out user
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess(User user, boolean isNewUser);
        void onError(Exception e);
    }

    public interface JoinFamilyCallback {
        void onSuccess(User kidUser, String familyId);
        void onError(Exception e);
    }

    public interface GetUserCallback {
        void onSuccess(User user);
        void onError(Exception e);
    }
}
