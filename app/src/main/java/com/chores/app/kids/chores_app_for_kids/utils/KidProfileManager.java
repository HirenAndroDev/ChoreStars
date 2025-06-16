package com.chores.app.kids.chores_app_for_kids.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.chores.app.kids.chores_app_for_kids.models.KidProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class KidProfileManager {
    private static final String PREF_NAME = "kid_profiles";
    private static final String KEY_KID_PROFILES = "saved_kid_profiles";
    private static final String KEY_SELECTED_KID_ID = "selected_kid_id";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public KidProfileManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Save kid profiles to local storage
    public void saveKidProfiles(List<KidProfile> kidProfiles) {
        String json = gson.toJson(kidProfiles);
        sharedPreferences.edit()
                .putString(KEY_KID_PROFILES, json)
                .apply();
    }

    // Load kid profiles from local storage
    public List<KidProfile> getKidProfiles() {
        String json = sharedPreferences.getString(KEY_KID_PROFILES, null);
        if (json != null) {
            Type type = new TypeToken<List<KidProfile>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    // Add a new kid profile
    public void addKidProfile(KidProfile kidProfile) {
        List<KidProfile> profiles = getKidProfiles();

        // Check if profile already exists
        boolean exists = false;
        for (KidProfile existingProfile : profiles) {
            if (existingProfile.getKidId().equals(kidProfile.getKidId())) {
                exists = true;
                break;
            }
        }

        // Only add if it doesn't exist
        if (!exists) {
            profiles.add(kidProfile);
            saveKidProfiles(profiles);
        }
    }

    // Update an existing kid profile
    public void updateKidProfile(KidProfile updatedProfile) {
        List<KidProfile> profiles = getKidProfiles();
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getKidId().equals(updatedProfile.getKidId())) {
                profiles.set(i, updatedProfile);
                break;
            }
        }
        saveKidProfiles(profiles);
    }

    // Remove a kid profile
    public void removeKidProfile(String kidId) {
        List<KidProfile> profiles = getKidProfiles();
        profiles.removeIf(profile -> profile.getKidId().equals(kidId));
        saveKidProfiles(profiles);
    }

    // Set selected kid
    public void setSelectedKid(String kidId) {
        sharedPreferences.edit()
                .putString(KEY_SELECTED_KID_ID, kidId)
                .apply();
    }

    // Get selected kid ID
    public String getSelectedKidId() {
        return sharedPreferences.getString(KEY_SELECTED_KID_ID, null);
    }

    // Get selected kid profile
    public KidProfile getSelectedKidProfile() {
        String selectedKidId = getSelectedKidId();
        if (selectedKidId != null) {
            List<KidProfile> profiles = getKidProfiles();
            for (KidProfile profile : profiles) {
                if (profile.getKidId().equals(selectedKidId)) {
                    return profile;
                }
            }
        }
        return null;
    }

    // Check if any kid profiles exist
    public boolean hasKidProfiles() {
        return !getKidProfiles().isEmpty();
    }

    // Get kid profile by ID
    public KidProfile getKidProfileById(String kidId) {
        for (KidProfile profile : getKidProfiles()) {
            if (profile.getKidId().equals(kidId)) {
                return profile;
            }
        }
        return null;
    }

    // Clear all kid profiles (for logout)
    public void clearAllKidProfiles() {
        sharedPreferences.edit()
                .remove(KEY_KID_PROFILES)
                .remove(KEY_SELECTED_KID_ID)
                .apply();
    }

    // Clear selected kid only (keep profiles)
    public void clearSelectedKid() {
        sharedPreferences.edit()
                .remove(KEY_SELECTED_KID_ID)
                .apply();
    }
}
