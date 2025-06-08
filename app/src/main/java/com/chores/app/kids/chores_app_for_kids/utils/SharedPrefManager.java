package com.chores.app.kids.chores_app_for_kids.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "chores_app_prefs";
    private static SharedPrefManager instance;
    private SharedPreferences prefs;

    private SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    // User ID
    public void setUserId(String userId) {
        prefs.edit().putString(Constants.PREF_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.PREF_USER_ID, null);
    }

    // User Role
    public void setUserRole(String role) {
        prefs.edit().putString(Constants.PREF_USER_ROLE, role).apply();
    }

    public String getUserRole() {
        return prefs.getString(Constants.PREF_USER_ROLE, null);
    }

    // Family ID
    public void setFamilyId(String familyId) {
        prefs.edit().putString(Constants.PREF_FAMILY_ID, familyId).apply();
    }

    public String getFamilyId() {
        return prefs.getString(Constants.PREF_FAMILY_ID, null);
    }

    // Kid ID
    public void setKidId(String kidId) {
        prefs.edit().putString(Constants.PREF_KID_ID, kidId).apply();
    }

    public String getKidId() {
        return prefs.getString(Constants.PREF_KID_ID, null);
    }

    // Login Status
    public void setLoggedIn(boolean isLoggedIn) {
        prefs.edit().putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    // User Name
    public void setUserName(String name) {
        prefs.edit().putString(Constants.PREF_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, null);
    }

    // User Email
    public void setUserEmail(String email) {
        prefs.edit().putString(Constants.PREF_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, null);
    }

    // Clear all data (logout)
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // Check if user is parent
    public boolean isParent() {
        return Constants.ROLE_PARENT.equals(getUserRole());
    }

    // Check if user is kid
    public boolean isKid() {
        return Constants.ROLE_KID.equals(getUserRole());
    }
}