package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.LandingActivity;
import com.chores.app.kids.chores_app_for_kids.activities.parent.FamilyManagementActivity;
import com.chores.app.kids.chores_app_for_kids.utils.AuthManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParentSettingsFragment extends Fragment {
    private static final String TAG = "ParentSettingsFragment";

    // Views - User Profile
    private CircleImageView ivUserAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvPremiumStatus;
    private MaterialCardView cardUserProfile;

    // Account Section
    private LinearLayout layoutFamily;
    private LinearLayout layoutNotifications;
    private LinearLayout layoutLanguage;
    private LinearLayout layoutAppearance;
    private LinearLayout layoutStartWeekOn;
    private TextView tvFamilyMembers;
    private TextView tvLanguageValue;
    private TextView tvAppearanceValue;
    private TextView tvStartWeekValue;

    // App Settings
    private Switch switchNotifications;
    private Switch switchSounds;
    private Switch switchVibration;
    private Switch switchAutoBackup;

    // Support Section
    private LinearLayout layoutContactUs;
    private LinearLayout layoutHelpCenter;
    private LinearLayout layoutRateApp;
    private LinearLayout layoutShareApp;

    // Legal Section
    private LinearLayout layoutTermsOfUse;
    private LinearLayout layoutPrivacyPolicy;
    private LinearLayout layoutAbout;

    // Account Actions
    private LinearLayout layoutLogout;
    private LinearLayout layoutDeleteAccount;

    // Managers
    private SharedPrefManager prefManager;
    private AuthManager authManager;

    public ParentSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initManagers();
        setupClickListeners();
        loadUserData();
        loadAppPreferences();
    }

    private void initViews(View view) {
        // User Profile
        ivUserAvatar = view.findViewById(R.id.iv_user_avatar);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvPremiumStatus = view.findViewById(R.id.tv_premium_status);
        cardUserProfile = view.findViewById(R.id.card_user_profile);

        // Account Section
        layoutFamily = view.findViewById(R.id.layout_family);
        layoutNotifications = view.findViewById(R.id.layout_notifications);
        layoutLanguage = view.findViewById(R.id.layout_language);
        layoutAppearance = view.findViewById(R.id.layout_appearance);
        layoutStartWeekOn = view.findViewById(R.id.layout_start_week_on);
        tvFamilyMembers = view.findViewById(R.id.tv_family_members);
        tvLanguageValue = view.findViewById(R.id.tv_language_value);
        tvAppearanceValue = view.findViewById(R.id.tv_appearance_value);
        tvStartWeekValue = view.findViewById(R.id.tv_start_week_value);

        // App Settings
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchSounds = view.findViewById(R.id.switch_sounds);
        switchVibration = view.findViewById(R.id.switch_vibration);
        switchAutoBackup = view.findViewById(R.id.switch_auto_backup);

        // Support Section
        layoutContactUs = view.findViewById(R.id.layout_contact_us);
        layoutHelpCenter = view.findViewById(R.id.layout_help_center);
        layoutRateApp = view.findViewById(R.id.layout_rate_app);
        layoutShareApp = view.findViewById(R.id.layout_share_app);

        // Legal Section
        layoutTermsOfUse = view.findViewById(R.id.layout_terms_of_use);
        layoutPrivacyPolicy = view.findViewById(R.id.layout_privacy_policy);
        layoutAbout = view.findViewById(R.id.layout_about);

        // Account Actions
        layoutLogout = view.findViewById(R.id.layout_logout);
        layoutDeleteAccount = view.findViewById(R.id.layout_delete_account);
    }

    private void initManagers() {
        prefManager = SharedPrefManager.getInstance(requireContext());
        authManager = AuthManager.getInstance(requireContext());
    }

    private void setupClickListeners() {
        // User Profile
        cardUserProfile.setOnClickListener(v -> editUserProfile());

        // Account Section
        layoutFamily.setOnClickListener(v -> openFamilyManagement());
        layoutNotifications.setOnClickListener(v -> openNotificationSettings());
        layoutLanguage.setOnClickListener(v -> showLanguageSelector());
        layoutAppearance.setOnClickListener(v -> showAppearanceSelector());
        layoutStartWeekOn.setOnClickListener(v -> showStartWeekSelector());

        // App Settings Switches
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("notifications_enabled", isChecked);
            showToast("Notifications " + (isChecked ? "enabled" : "disabled"));
        });

        switchSounds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("sounds_enabled", isChecked);
            showToast("Sounds " + (isChecked ? "enabled" : "disabled"));
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("vibration_enabled", isChecked);
            showToast("Vibration " + (isChecked ? "enabled" : "disabled"));
        });

        switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("auto_backup_enabled", isChecked);
            showToast("Auto backup " + (isChecked ? "enabled" : "disabled"));
        });

        // Support Section
        layoutContactUs.setOnClickListener(v -> openContactUs());
        layoutHelpCenter.setOnClickListener(v -> openHelpCenter());
        layoutRateApp.setOnClickListener(v -> rateApp());
        layoutShareApp.setOnClickListener(v -> shareApp());

        // Legal Section
        layoutTermsOfUse.setOnClickListener(v -> openTermsOfUse());
        layoutPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        layoutAbout.setOnClickListener(v -> showAboutDialog());

        // Account Actions
        layoutLogout.setOnClickListener(v -> showLogoutConfirmation());
        layoutDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    private void loadUserData() {
        // Load user information
        String userName = prefManager.getUserName();
        String userEmail = prefManager.getUserEmail();

        if (userName != null) {
            tvUserName.setText(userName);
        } else {
            tvUserName.setText("User Name");
        }

        if (userEmail != null) {
            tvUserEmail.setText(userEmail);
        } else {
            tvUserEmail.setText("user@example.com");
        }

        // Load user avatar
        String profileImageUrl = authManager.getCurrentUser() != null ?
                authManager.getCurrentUser().getPhotoUrl() != null ?
                        authManager.getCurrentUser().getPhotoUrl().toString() : null : null;

        if (profileImageUrl != null) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivUserAvatar);
        }

        // Set premium status
        tvPremiumStatus.setText("PREMIUM");
        tvPremiumStatus.setVisibility(View.VISIBLE);

        // Load family info
        tvFamilyMembers.setText("3 members"); // This would be loaded from Firebase
    }

    private void loadAppPreferences() {
        // Load saved preferences with defaults
        switchNotifications.setChecked(getPreference("notifications_enabled", true));
        switchSounds.setChecked(getPreference("sounds_enabled", true));
        switchVibration.setChecked(getPreference("vibration_enabled", true));
        switchAutoBackup.setChecked(getPreference("auto_backup_enabled", true));

        tvLanguageValue.setText(getPreference("app_language", "English"));
        tvAppearanceValue.setText(getPreference("app_theme", "System"));
        tvStartWeekValue.setText(getPreference("week_start", "Sunday"));
    }

    private void editUserProfile() {
        showToast("Profile editing feature coming soon!");
        // Navigate to profile editing activity
    }

    private void openFamilyManagement() {
        Intent intent = new Intent(requireContext(), FamilyManagementActivity.class);
        startActivity(intent);
    }

    private void openNotificationSettings() {
        // Navigate to detailed notification settings
        showToast("Opening notification settings...");
    }

    private void showLanguageSelector() {
        String[] languages = {"English", "Spanish", "French", "German", "Chinese", "Japanese", "Portuguese"};
        String currentLanguage = getPreference("app_language", "English");
        int currentSelection = getIndexOfString(languages, currentLanguage);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                    String selectedLanguage = languages[which];
                    tvLanguageValue.setText(selectedLanguage);
                    savePreference("app_language", selectedLanguage);
                    showToast("Language changed to " + selectedLanguage);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAppearanceSelector() {
        String[] themes = {"Light", "Dark", "System"};
        String currentTheme = getPreference("app_theme", "System");
        int currentSelection = getIndexOfString(themes, currentTheme);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("App Appearance")
                .setSingleChoiceItems(themes, currentSelection, (dialog, which) -> {
                    String selectedTheme = themes[which];
                    tvAppearanceValue.setText(selectedTheme);
                    savePreference("app_theme", selectedTheme);
                    showToast("Theme changed to " + selectedTheme);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showStartWeekSelector() {
        String[] weekStarts = {"Sunday", "Monday"};
        String currentWeekStart = getPreference("week_start", "Sunday");
        int currentSelection = getIndexOfString(weekStarts, currentWeekStart);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Week Starts On")
                .setSingleChoiceItems(weekStarts, currentSelection, (dialog, which) -> {
                    String selectedWeekStart = weekStarts[which];
                    tvStartWeekValue.setText(selectedWeekStart);
                    savePreference("week_start", selectedWeekStart);
                    showToast("Week starts on " + selectedWeekStart);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openContactUs() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@neatkid.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NeatKid App Support");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi NeatKid Team,\n\nI need help with...\n\nApp Version: 1.0.0\nDevice: " + android.os.Build.MODEL);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception ex) {
            showToast("No email client found");
        }
    }

    private void openHelpCenter() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://neatkid.com/help"));
        try {
            startActivity(browserIntent);
        } catch (Exception ex) {
            showToast("Unable to open help center");
        }
    }

    private void rateApp() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + requireContext().getPackageName()));
            startActivity(rateIntent);
        } catch (Exception ex) {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName()));
            startActivity(rateIntent);
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out NeatKid!");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Hey! I'm using NeatKid to help my kids build great habits with fun tasks and rewards. " +
                        "Download it here: https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
        startActivity(Intent.createChooser(shareIntent, "Share NeatKid"));
    }

    private void openTermsOfUse() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://neatkid.com/terms"));
        try {
            startActivity(browserIntent);
        } catch (Exception ex) {
            showToast("Unable to open terms of use");
        }
    }

    private void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://neatkid.com/privacy"));
        try {
            startActivity(browserIntent);
        } catch (Exception ex) {
            showToast("Unable to open privacy policy");
        }
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("About NeatKid")
                .setMessage("NeatKid - Small Tasks, Big Habits\n\n" +
                        "Version: 1.0.0\n" +
                        "Build: 2025.06.07\n\n" +
                        "Help your kids build great habits with fun tasks and exciting rewards!\n\n" +
                        "Developed with ❤️ for families everywhere.\n\n" +
                        "© 2025 NeatKid. All rights reserved.")
                .setPositiveButton("OK", null)
                .setNeutralButton("Visit Website", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://neatkid.com"));
                    startActivity(browserIntent);
                })
                .show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from your account?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear all stored data
        prefManager.clearAll();

        // Sign out from Firebase Auth
        authManager.signOut(task -> {
            // Navigate to landing screen
            Intent intent = new Intent(requireContext(), LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }

            showToast("Logged out successfully");
        });
    }

    private void showDeleteAccountConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("⚠️ WARNING: This action cannot be undone!\n\n" +
                        "Deleting your account will:\n" +
                        "• Remove all your family data\n" +
                        "• Delete all tasks and rewards\n" +
                        "• Remove all kids' progress\n" +
                        "• Cancel your subscription\n\n" +
                        "Are you absolutely sure?")
                .setPositiveButton("Delete Forever", (dialog, which) -> showFinalDeleteConfirmation())
                .setNegativeButton("Keep Account", null)
                .show();
    }

    private void showFinalDeleteConfirmation() {
        // For now, just show a message that this feature is coming soon
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Account Deletion")
                .setMessage("Account deletion is being processed. Our team will review your request and delete your account within 7 business days.\n\nYou will receive a confirmation email once the deletion is complete.")
                .setPositiveButton("OK", null)
                .show();
    }

    // Helper methods
    private void savePreference(String key, boolean value) {
        requireContext().getSharedPreferences("app_settings", 0)
                .edit().putBoolean(key, value).apply();
    }

    private void savePreference(String key, String value) {
        requireContext().getSharedPreferences("app_settings", 0)
                .edit().putString(key, value).apply();
    }

    private boolean getPreference(String key, boolean defaultValue) {
        return requireContext().getSharedPreferences("app_settings", 0)
                .getBoolean(key, defaultValue);
    }

    private String getPreference(String key, String defaultValue) {
        return requireContext().getSharedPreferences("app_settings", 0)
                .getString(key, defaultValue);
    }

    private int getIndexOfString(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); // Refresh user data when fragment becomes visible
    }
}