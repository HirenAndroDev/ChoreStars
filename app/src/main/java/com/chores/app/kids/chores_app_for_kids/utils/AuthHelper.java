package com.chores.app.kids.chores_app_for_kids.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.models.Family;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuthHelper {

    private static final String PREFS_NAME = "NeatKidPrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_FAMILY_ID = "family_id";
    private static final String KEY_USER_NAME = "user_name";

    private static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Google Sign-in for Parents
    public static void signInWithGoogle(Activity activity, OnCompleteListener<Task<GoogleSignInAccount>> listener) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, 9001);
    }

    public static void firebaseAuthWithGoogle(String idToken, Context context, OnCompleteListener<AuthResult> listener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        createOrUpdateParentUser(firebaseUser, context, listener);
                    } else {
                        // Create failure AuthResult
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Authentication failed";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createOrUpdateParentUser(FirebaseUser firebaseUser, Context context, OnCompleteListener<AuthResult> listener) {
        String userId = firebaseUser.getUid();

        // Check if user already exists
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // User exists, check if they have a family
                            String familyId = document.getString("familyId");
                            if (familyId != null && !familyId.isEmpty()) {
                                // User has a family, proceed to dashboard
                                saveUserSession(context, firebaseUser.getDisplayName(), "parent", familyId);
                                AuthResult result = new AuthResult(true, "User authenticated successfully", familyId);
                                listener.onComplete(createTaskFromResult(result));
                            } else {
                                // User exists but no family, create one
                                createNewFamily(firebaseUser, context, listener);
                            }
                        } else {
                            // New user, create user and family
                            createNewFamily(firebaseUser, context, listener);
                        }
                    } else {
                        // Database error
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Database error";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createNewFamily(FirebaseUser firebaseUser, Context context, OnCompleteListener<AuthResult> listener) {
        String userId = firebaseUser.getUid();
        String familyId = "family_" + System.currentTimeMillis();

        // Create family document
        Family family = new Family(familyId, userId);
        family.getParentIds().add(userId);

        Map<String, Object> familyData = new HashMap<>();
        familyData.put("ownerId", family.getOwnerId());
        familyData.put("parentIds", family.getParentIds());
        familyData.put("childIds", family.getChildIds());
        familyData.put("inviteCode", generateInviteCode());
        familyData.put("inviteCodeExpiry", System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        familyData.put("createdAt", System.currentTimeMillis());
        familyData.put("familyName", firebaseUser.getDisplayName() + "'s Family");

        db.collection("families").document(familyId).set(familyData)
                .addOnCompleteListener(familyTask -> {
                    if (familyTask.isSuccessful()) {
                        // Create user document
                        User user = new User(userId, firebaseUser.getDisplayName(),
                                firebaseUser.getEmail(), "parent", familyId);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", user.getName());
                        userData.put("email", user.getEmail());
                        userData.put("role", user.getRole());
                        userData.put("familyId", user.getFamilyId());
                        userData.put("starBalance", 0);
                        userData.put("textToSpeechEnabled", false);
                        userData.put("createdAt", System.currentTimeMillis());
                        userData.put("profileImageUrl", firebaseUser.getPhotoUrl() != null ?
                                firebaseUser.getPhotoUrl().toString() : "");

                        db.collection("users").document(userId).set(userData)
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        saveUserSession(context, user.getName(), "parent", familyId);
                                        AuthResult result = new AuthResult(true, "Family created successfully", familyId);
                                        listener.onComplete(createTaskFromResult(result));
                                    } else {
                                        String errorMessage = userTask.getException() != null ?
                                                userTask.getException().getMessage() : "Failed to create user";
                                        AuthResult result = new AuthResult(false, errorMessage, null);
                                        listener.onComplete(createTaskFromResult(result));
                                    }
                                });
                    } else {
                        String errorMessage = familyTask.getException() != null ?
                                familyTask.getException().getMessage() : "Failed to create family";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    // Join family with invite code (for kids)
    public static void joinFamilyWithCode(String inviteCode, String childName, Context context,
                                          OnCompleteListener<AuthResult> listener) {
        // First, find the family with this invite code
        db.collection("families")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot familyDoc = querySnapshot.getDocuments().get(0);
                            String familyId = familyDoc.getId();
                            Long expiry = familyDoc.getLong("inviteCodeExpiry");

                            // Check if code is still valid
                            if (expiry != null && expiry > System.currentTimeMillis()) {
                                createChildUser(childName, familyId, context, listener);
                            } else {
                                // Code expired
                                AuthResult result = new AuthResult(false, "Invite code has expired", null);
                                listener.onComplete(createTaskFromResult(result));
                            }
                        } else {
                            // Invalid code
                            AuthResult result = new AuthResult(false, "Invalid invite code", null);
                            listener.onComplete(createTaskFromResult(result));
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to verify invite code";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createChildUser(String childName, String familyId, Context context,
                                        OnCompleteListener<AuthResult> listener) {
        // Create anonymous user for child
        auth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        // Create user document
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", childName);
                        userData.put("email", "");
                        userData.put("role", "child");
                        userData.put("familyId", familyId);
                        userData.put("starBalance", 0);
                        userData.put("textToSpeechEnabled", true);
                        userData.put("createdAt", System.currentTimeMillis());
                        userData.put("profileImageUrl", "");

                        db.collection("users").document(userId).set(userData)
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        // Add child to family
                                        updateFamilyWithNewChild(familyId, userId, childName, context, listener);
                                    } else {
                                        String errorMessage = userTask.getException() != null ?
                                                userTask.getException().getMessage() : "Failed to create child user";
                                        AuthResult result = new AuthResult(false, errorMessage, null);
                                        listener.onComplete(createTaskFromResult(result));
                                    }
                                });
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to create anonymous user";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void updateFamilyWithNewChild(String familyId, String childId, String childName, Context context,
                                                 OnCompleteListener<AuthResult> listener) {
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
                                            saveUserSession(context, childName, "child", familyId);
                                            AuthResult result = new AuthResult(true, "Successfully joined family", familyId);
                                            listener.onComplete(createTaskFromResult(result));
                                        } else {
                                            String errorMessage = updateTask.getException() != null ?
                                                    updateTask.getException().getMessage() : "Failed to update family";
                                            AuthResult result = new AuthResult(false, errorMessage, null);
                                            listener.onComplete(createTaskFromResult(result));
                                        }
                                    });
                        } else {
                            AuthResult result = new AuthResult(false, "Family not found", null);
                            listener.onComplete(createTaskFromResult(result));
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to access family data";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    // Helper method to create a Task from AuthResult
    private static Task<AuthResult> createTaskFromResult(AuthResult result) {
        com.google.android.gms.tasks.TaskCompletionSource<AuthResult> taskSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        if (result.isSuccessful()) {
            taskSource.setResult(result);
        } else {
            taskSource.setException(new Exception(result.getMessage()));
        }
        return taskSource.getTask();
    }

    // Session management
    private static void saveUserSession(Context context, String userName, String role, String familyId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_FAMILY_ID, familyId);
        editor.apply();
    }

    public static String getUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, "");
    }

    public static String getFamilyId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FAMILY_ID, "");
    }

    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "");
    }

    // Sign out
    public static void signOut(Context context) {
        auth.signOut();

        // Clear Google Sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
        googleSignInClient.signOut();

        // Clear session
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    // Utility methods
    private static String generateInviteCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    public static boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public static String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Custom result class
    public static class AuthResult {
        private boolean success;
        private String message;
        private String familyId;

        public AuthResult(boolean success, String message, String familyId) {
            this.success = success;
            this.message = message;
            this.familyId = familyId;
        }

        public boolean isSuccessful() { return success; }
        public String getMessage() { return message; }
        public String getFamilyId() { return familyId; }
    }
}