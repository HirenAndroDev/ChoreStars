package com.chores.app.kids.chores_app_for_kids.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.KidProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthHelper {

    private static final String TAG = "AuthHelper";
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
        Log.d(TAG, "Starting Firebase authentication with Google");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful");
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "User ID: " + firebaseUser.getUid());
                            Log.d(TAG, "User email: " + firebaseUser.getEmail());
                            createOrUpdateParentUser(firebaseUser, context, listener);
                        } else {
                            Log.e(TAG, "Firebase user is null after successful authentication");
                            AuthResult result = new AuthResult(false, "Failed to get user information", null);
                            listener.onComplete(createTaskFromResult(result));
                        }
                    } else {
                        Log.e(TAG, "Firebase authentication failed", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Authentication failed";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createOrUpdateParentUser(FirebaseUser firebaseUser, Context context, OnCompleteListener<AuthResult> listener) {
        String userId = firebaseUser.getUid();
        Log.d(TAG, "Checking if user exists: " + userId);

        // Check if user already exists
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "User document exists, checking family status");
                            // User exists, update last login and check family
                            updateUserLastLogin(userId);

                            String familyId = document.getString("familyId");
                            String role = document.getString("role");
                            String name = document.getString("name");

                            if (familyId != null && !familyId.isEmpty()) {
                                // User has a family, proceed to dashboard
                                Log.d(TAG, "User has family: " + familyId);
                                saveUserSession(context, name != null ? name : firebaseUser.getDisplayName(),
                                        role != null ? role : "parent", familyId);
                                AuthResult result = new AuthResult(true, "User authenticated successfully", familyId);
                                listener.onComplete(createTaskFromResult(result));
                            } else {
                                // User exists but no family, create one
                                Log.d(TAG, "User exists but no family, creating new family");
                                createNewFamily(firebaseUser, context, listener);
                            }
                        } else {
                            // New user, create user and family
                            Log.d(TAG, "New user, creating user document and family");
                            createNewUserAndFamily(firebaseUser, context, listener);
                        }
                    } else {
                        Log.e(TAG, "Error checking user document", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Database error";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createNewUserAndFamily(FirebaseUser firebaseUser, Context context, OnCompleteListener<AuthResult> listener) {
        String userId = firebaseUser.getUid();
        String familyId = "family_" + System.currentTimeMillis();

        Log.d(TAG, "Creating new user and family - UserID: " + userId + ", FamilyID: " + familyId);

        // Create user document first
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Parent");
        userData.put("email", firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
        userData.put("role", "parent");
        userData.put("familyId", familyId);
        userData.put("starBalance", 0);
        userData.put("textToSpeechEnabled", false);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLoginAt", System.currentTimeMillis());
        userData.put("profileImageUrl", firebaseUser.getPhotoUrl() != null ?
                firebaseUser.getPhotoUrl().toString() : "");

        Log.d(TAG, "Creating user document with data: " + userData.toString());

        db.collection("users").document(userId).set(userData)
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        Log.d(TAG, "User document created successfully");
                        // Now create family document
                        createFamilyDocument(firebaseUser, familyId, context, listener);
                    } else {
                        Log.e(TAG, "Failed to create user document", userTask.getException());
                        String errorMessage = userTask.getException() != null ?
                                userTask.getException().getMessage() : "Failed to create user";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createFamilyDocument(FirebaseUser firebaseUser, String familyId, Context context, OnCompleteListener<AuthResult> listener) {
        String userId = firebaseUser.getUid();

        Log.d(TAG, "Creating family document: " + familyId);

        // Create family document
        Map<String, Object> familyData = new HashMap<>();
        familyData.put("ownerId", userId);

        ArrayList<String> parentIds = new ArrayList<>();
        parentIds.add(userId);
        familyData.put("parentIds", parentIds);

        familyData.put("childIds", new ArrayList<String>());
        familyData.put("inviteCode", generateInviteCode());
        familyData.put("inviteCodeExpiry", System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        familyData.put("createdAt", System.currentTimeMillis());
        familyData.put("familyName", (firebaseUser.getDisplayName() != null ?
                firebaseUser.getDisplayName() : "Parent") + "'s Family");

        Log.d(TAG, "Creating family document with data: " + familyData.toString());

        db.collection("families").document(familyId).set(familyData)
                .addOnCompleteListener(familyTask -> {
                    if (familyTask.isSuccessful()) {
                        Log.d(TAG, "Family document created successfully");
                        saveUserSession(context, firebaseUser.getDisplayName() != null ?
                                firebaseUser.getDisplayName() : "Parent", "parent", familyId);
                        AuthResult result = new AuthResult(true, "Family created successfully", familyId);
                        listener.onComplete(createTaskFromResult(result));
                    } else {
                        Log.e(TAG, "Failed to create family document", familyTask.getException());
                        String errorMessage = familyTask.getException() != null ?
                                familyTask.getException().getMessage() : "Failed to create family";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createNewFamily(FirebaseUser firebaseUser, Context context, OnCompleteListener<AuthResult> listener) {
        String userId = firebaseUser.getUid();
        String familyId = "family_" + System.currentTimeMillis();

        Log.d(TAG, "Creating new family for existing user: " + familyId);

        // Create family document
        createFamilyDocument(firebaseUser, familyId, context, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Update user document with family ID
                    db.collection("users").document(userId)
                            .update("familyId", familyId, "lastLoginAt", System.currentTimeMillis())
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d(TAG, "User updated with family ID");
                                    listener.onComplete(task);
                                } else {
                                    Log.e(TAG, "Failed to update user with family ID", updateTask.getException());
                                    String errorMessage = updateTask.getException() != null ?
                                            updateTask.getException().getMessage() : "Failed to update user";
                                    AuthResult result = new AuthResult(false, errorMessage, null);
                                    listener.onComplete(createTaskFromResult(result));
                                }
                            });
                } else {
                    listener.onComplete(task);
                }
            }
        });
    }

    private static void updateUserLastLogin(String userId) {
        Log.d(TAG, "Updating last login for user: " + userId);
        db.collection("users").document(userId)
                .update("lastLoginAt", System.currentTimeMillis())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Last login updated successfully");
                    } else {
                        Log.e(TAG, "Failed to update last login", task.getException());
                    }
                });
    }

    // Join family with invite code (for kids)
    public static void joinFamilyWithCode(String inviteCode, String childName, Context context,
                                          OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "Attempting to join family with code: " + inviteCode);

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

                            Log.d(TAG, "Found family: " + familyId);

                            // Check if code is still valid
                            if (expiry != null && expiry > System.currentTimeMillis()) {
                                createChildUser(childName, familyId, context, listener);
                            } else {
                                Log.d(TAG, "Invite code expired");
                                AuthResult result = new AuthResult(false, "Invite code has expired", null);
                                listener.onComplete(createTaskFromResult(result));
                            }
                        } else {
                            Log.d(TAG, "Invalid invite code");
                            AuthResult result = new AuthResult(false, "Invalid invite code", null);
                            listener.onComplete(createTaskFromResult(result));
                        }
                    } else {
                        Log.e(TAG, "Error verifying invite code", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to verify invite code";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void createChildUser(String childName, String familyId, Context context,
                                        OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "Creating child user: " + childName + " for family: " + familyId);

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
        userData.put("profileImageUrl", "");
        userData.put("isActive", true);

        db.collection("users").document(childId).set(userData)
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        Log.d(TAG, "Child user document created: " + childId);
                        // Add child to family and save session
                        updateFamilyWithNewChild(familyId, childId, childName, context, listener);
                    } else {
                        Log.e(TAG, "Failed to create child user", userTask.getException());
                        String errorMessage = userTask.getException() != null ?
                                userTask.getException().getMessage() : "Failed to create child user";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    private static void updateFamilyWithNewChild(String familyId, String childId, String childName, Context context,
                                                 OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "Adding child to family: " + childId + " -> " + familyId);

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
                                            Log.d(TAG, "Child added to family successfully");
                                            saveChildSession(context, childId, childName, familyId);
                                            AuthResult result = new AuthResult(true, "Successfully joined family", familyId);
                                            listener.onComplete(createTaskFromResult(result));
                                        } else {
                                            Log.e(TAG, "Failed to update family", updateTask.getException());
                                            String errorMessage = updateTask.getException() != null ?
                                                    updateTask.getException().getMessage() : "Failed to update family";
                                            AuthResult result = new AuthResult(false, errorMessage, null);
                                            listener.onComplete(createTaskFromResult(result));
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Family document not found");
                            AuthResult result = new AuthResult(false, "Family not found", null);
                            listener.onComplete(createTaskFromResult(result));
                        }
                    } else {
                        Log.e(TAG, "Error accessing family data", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to access family data";
                        AuthResult result = new AuthResult(false, errorMessage, null);
                        listener.onComplete(createTaskFromResult(result));
                    }
                });
    }

    // Join family with child-specific invite code
    public static void joinFamilyWithChildCode(String inviteCode, Context context,
                                               OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "Attempting to join family with child code: " + inviteCode);

        FirebaseHelper.joinFamilyWithChildCode(inviteCode, task -> {
            if (task.isSuccessful()) {
                ChildProfile childProfile = task.getResult();
                Log.d(TAG, "Found child profile: " + childProfile.getName());

                // Save child session
                saveChildSession(context, childProfile.getChildId(), childProfile.getName(), childProfile.getFamilyId());

                AuthResult result = new AuthResult(true, "Successfully joined as " + childProfile.getName(), childProfile.getFamilyId());
                listener.onComplete(createTaskFromResult(result));
            } else {
                Log.d(TAG, "Failed to join with child invite code");
                Exception exception = task.getException();
                String errorMessage = "Invalid invite code";

                if (exception != null) {
                    String exceptionMessage = exception.getMessage();
                    Log.d(TAG, "Exception message: " + exceptionMessage);

                    if (exceptionMessage != null) {
                        if (exceptionMessage.contains("expired")) {
                            errorMessage = "This invite code has expired. Ask your parent for a new one!";
                        } else if (exceptionMessage.contains("Invalid invite code")) {
                            errorMessage = "Invite code not found. Double-check the numbers!";
                        } else {
                            errorMessage = exceptionMessage;
                        }
                    }
                }

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
        Log.d(TAG, "Saving user session - Name: " + userName + ", Role: " + role + ", Family: " + familyId);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_FAMILY_ID, familyId);
        editor.apply();
    }

    // Enhanced session management for children
    private static void saveChildSession(Context context, String childId, String userName, String familyId) {
        Log.d(TAG, "Saving child session - ID: " + childId + ", Name: " + userName + ", Family: " + familyId);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("child_id", childId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_ROLE, "child");
        editor.putString(KEY_FAMILY_ID, familyId);
        editor.putBoolean("is_child_account", true);
        editor.putLong("child_login_time", System.currentTimeMillis());
        editor.apply();

        // Also save this kid profile to the KidProfileManager for multi-profile support
        saveKidProfile(context, childId, userName, familyId);
    }

    // Save kid profile for multi-profile support
    private static void saveKidProfile(Context context, String childId, String userName, String familyId) {
        KidProfileManager kidProfileManager = new KidProfileManager(context);

        // Create a KidProfile object
        KidProfile kidProfile = new KidProfile();
        kidProfile.setKidId(childId);
        kidProfile.setName(userName);
        kidProfile.setFamilyId(familyId);
        kidProfile.setStarBalance(0); // Will be updated when loaded
        kidProfile.setSelected(false); // Will be set as selected below

        // Add to saved profiles
        kidProfileManager.addKidProfile(kidProfile);
        kidProfileManager.setSelectedKid(childId);
    }

    // Get all saved kid profiles
    public static List<KidProfile> getSavedKidProfiles(Context context) {
        KidProfileManager kidProfileManager = new KidProfileManager(context);
        return kidProfileManager.getKidProfiles();
    }

    // Switch to a different kid profile
    public static void switchToKidProfile(Context context, String kidId) {
        KidProfileManager kidProfileManager = new KidProfileManager(context);
        List<KidProfile> profiles = kidProfileManager.getKidProfiles();

        for (KidProfile profile : profiles) {
            if (profile.getKidId().equals(kidId)) {
                // Update current session
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("child_id", profile.getKidId());
                editor.putString(KEY_USER_NAME, profile.getName());
                editor.putString(KEY_USER_ROLE, "child");
                editor.putString(KEY_FAMILY_ID, profile.getFamilyId());
                editor.putBoolean("is_child_account", true);
                editor.putLong("child_login_time", System.currentTimeMillis());
                editor.apply();

                // Set as selected
                kidProfileManager.setSelectedKid(kidId);
                break;
            }
        }
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

    public static String getChildId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("child_id", "");
    }

    public static boolean isChildAccount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_child_account", false);
    }

    // Check if a kid is already logged in
    public static boolean isKidLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isChildAccount = prefs.getBoolean("is_child_account", false);
        String childId = prefs.getString("child_id", "");
        String familyId = prefs.getString(KEY_FAMILY_ID, "");

        return isChildAccount && !childId.isEmpty() && !familyId.isEmpty();
    }

    // Clear only kid session data
    public static void clearKidSession(Context context) {
        Log.d(TAG, "Clearing kid session");
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("child_id");
        editor.remove("is_child_account");
        editor.remove("child_login_time");
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_FAMILY_ID);
        editor.apply();

        // Clear selected kid but keep profiles
        KidProfileManager kidProfileManager = new KidProfileManager(context);
        kidProfileManager.clearSelectedKid();
    }

    // Clear all kid profiles (for complete logout)
    public static void clearAllKidProfiles(Context context) {
        Log.d(TAG, "Clearing all kid profiles");
        clearKidSession(context);

        // Clear all saved profiles
        KidProfileManager kidProfileManager = new KidProfileManager(context);
        kidProfileManager.clearAllKidProfiles();
    }

    // Sign out
    public static void signOut(Context context) {
        Log.d(TAG, "Signing out user");
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

    // Enhanced getCurrentUserId that works for both parents and children  
    public static String getCurrentUserId(Context context) {
        if (isChildAccount(context)) {
            return getChildId(context);
        } else {
            FirebaseUser user = auth.getCurrentUser();
            return user != null ? user.getUid() : null;
        }
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
