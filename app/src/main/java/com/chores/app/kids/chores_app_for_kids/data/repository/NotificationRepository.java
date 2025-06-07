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
import com.chores.app.kids.chores_app_for_kids.data.models.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for notification operations
 */
public class NotificationRepository {

    private DatabaseReference databaseReference;
    private DatabaseReference notificationsRef;

    public NotificationRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        notificationsRef = databaseReference.child("notifications");
    }

    /**
     * Create notification
     */
    public void createNotification(Notification notification, final CreateNotificationCallback callback) {
        String notificationId = notificationsRef.push().getKey();
        notification.setNotificationId(notificationId);

        notificationsRef.child(notificationId).setValue(notification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(notification);
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
     * Get notifications for user
     */
    public void getNotificationsForUser(String userId, final GetNotificationsCallback callback) {
        Query query = notificationsRef.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notification> notifications = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notifications.add(notification);
                    }
                }

                // Sort by created date (newest first)
                notifications.sort((n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));

                callback.onSuccess(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get unread notifications count
     */
    public void getUnreadNotificationsCount(String userId, final GetCountCallback callback) {
        Query query = notificationsRef.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unreadCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null && !notification.isRead()) {
                        unreadCount++;
                    }
                }
                callback.onSuccess(unreadCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId, final UpdateCallback callback) {
        notificationsRef.child(notificationId).child("isRead").setValue(true)
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
     * Mark all notifications as read
     */
    public void markAllAsRead(String userId, final UpdateCallback callback) {
        Query query = notificationsRef.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().child("isRead").setValue(true);
                }
                callback.onSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId, final UpdateCallback callback) {
        notificationsRef.child(notificationId).removeValue()
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
     * Delete old notifications (older than 30 days)
     */
    public void deleteOldNotifications(String userId, final UpdateCallback callback) {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        Query query = notificationsRef.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null && notification.getCreatedAt() < thirtyDaysAgo) {
                        snapshot.getRef().removeValue();
                    }
                }
                callback.onSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    // Callback interfaces
    public interface CreateNotificationCallback {
        void onSuccess(Notification notification);
        void onError(Exception e);
    }

    public interface GetNotificationsCallback {
        void onSuccess(List<Notification> notifications);
        void onError(Exception e);
    }

    public interface GetCountCallback {
        void onSuccess(int count);
        void onError(Exception e);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }
}) {
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