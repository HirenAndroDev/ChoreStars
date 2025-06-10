package com.chores.app.kids.chores_app_for_kids.utils;

import android.text.TextUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.regex.Pattern;

public class ValidationHelper {

    private static final int MIN_TASK_NAME_LENGTH = 2;
    private static final int MAX_TASK_NAME_LENGTH = 50;
    private static final int MIN_REWARD_NAME_LENGTH = 2;
    private static final int MAX_REWARD_NAME_LENGTH = 50;
    private static final int MIN_CHILD_NAME_LENGTH = 2;
    private static final int MAX_CHILD_NAME_LENGTH = 30;
    private static final int INVITE_CODE_LENGTH = 6;

    private static final Pattern TASK_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-'.,!]+$");
    private static final Pattern REWARD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-'.,!]+$");
    private static final Pattern CHILD_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-']+$");
    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^[0-9]{6}$");

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Validates task name
     * @param name Task name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTaskName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        String trimmedName = name.trim();

        // Check length
        if (trimmedName.length() < MIN_TASK_NAME_LENGTH || trimmedName.length() > MAX_TASK_NAME_LENGTH) {
            return false;
        }

        // Check pattern (letters, numbers, spaces, basic punctuation)
        return TASK_NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validates reward name
     * @param name Reward name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRewardName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        String trimmedName = name.trim();

        // Check length
        if (trimmedName.length() < MIN_REWARD_NAME_LENGTH || trimmedName.length() > MAX_REWARD_NAME_LENGTH) {
            return false;
        }

        // Check pattern
        return REWARD_NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validates child name
     * @param name Child name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidChildName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        String trimmedName = name.trim();

        // Check length
        if (trimmedName.length() < MIN_CHILD_NAME_LENGTH || trimmedName.length() > MAX_CHILD_NAME_LENGTH) {
            return false;
        }

        // Check pattern (only letters, spaces, hyphens, apostrophes)
        return CHILD_NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validates invite code format
     * @param code Invite code to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidInviteCode(String code) {
        if (TextUtils.isEmpty(code)) {
            return false;
        }

        String trimmedCode = code.trim();

        // Check if it's exactly 6 digits
        return INVITE_CODE_PATTERN.matcher(trimmedCode).matches();
    }

    /**
     * Validates star reward amount
     * @param stars Number of stars to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStarReward(int stars) {
        return stars >= 1 && stars <= 50; // Reasonable range for star rewards
    }

    /**
     * Validates star cost for rewards
     * @param cost Star cost to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStarCost(int cost) {
        return cost >= 1 && cost <= 100; // Reasonable range for star costs
    }

    /**
     * Checks if user has sufficient star balance for a reward
     * @param userId User ID to check
     * @param requiredStars Required stars for the reward
     * @param callback Callback with result
     */
    public static void hasValidStarBalance(String userId, int requiredStars, StarBalanceValidationCallback callback) {
        if (TextUtils.isEmpty(userId) || requiredStars <= 0) {
            callback.onValidationResult(false, "Invalid parameters");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long balance = document.getLong("starBalance");
                            int currentBalance = balance != null ? balance.intValue() : 0;

                            if (currentBalance >= requiredStars) {
                                callback.onValidationResult(true, "Sufficient balance");
                            } else {
                                int needed = requiredStars - currentBalance;
                                callback.onValidationResult(false, "Need " + needed + " more stars");
                            }
                        } else {
                            callback.onValidationResult(false, "User not found");
                        }
                    } else {
                        callback.onValidationResult(false, "Error checking balance");
                    }
                });
    }

    /**
     * Validates email format (for parent accounts)
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates reminder time format (HH:MM)
     * @param time Time string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidReminderTime(String time) {
        if (TextUtils.isEmpty(time)) {
            return true; // Optional field
        }

        String trimmedTime = time.trim();
        Pattern timePattern = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
        return timePattern.matcher(trimmedTime).matches();
    }

    /**
     * Validates that at least one child is selected for a task/reward
     * @param selectedKids List of selected child IDs
     * @return true if valid, false otherwise
     */
    public static boolean hasValidChildSelection(java.util.List<String> selectedKids) {
        return selectedKids != null && !selectedKids.isEmpty();
    }

    /**
     * Validates repeat type for tasks
     * @param repeatType Repeat type to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRepeatType(String repeatType) {
        if (TextUtils.isEmpty(repeatType)) {
            return false;
        }

        return repeatType.equals("daily") ||
                repeatType.equals("weekly") ||
                repeatType.equals("weekdays") ||
                repeatType.equals("weekends") ||
                repeatType.equals("custom");
    }

    /**
     * Validates renewal period for rewards
     * @param renewalPeriod Renewal period to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRenewalPeriod(String renewalPeriod) {
        if (TextUtils.isEmpty(renewalPeriod)) {
            return false;
        }

        return renewalPeriod.equals("daily") ||
                renewalPeriod.equals("weekly") ||
                renewalPeriod.equals("monthly") ||
                renewalPeriod.equals("once");
    }

    /**
     * Validates icon name
     * @param iconName Icon name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIconName(String iconName) {
        if (TextUtils.isEmpty(iconName)) {
            return false;
        }

        // Check if icon name follows expected pattern
        Pattern iconPattern = Pattern.compile("^ic_[a-z_]+$");
        return iconPattern.matcher(iconName).matches();
    }

    /**
     * Validates date string format (YYYY-MM-DD)
     * @param date Date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDateFormat(String date) {
        if (TextUtils.isEmpty(date)) {
            return true; // Optional field
        }

        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        if (!datePattern.matcher(date).matches()) {
            return false;
        }

        // Additional validation could be added here to check if it's a real date
        return true;
    }

    /**
     * Validates weekday selection for custom repeat tasks
     * @param selectedDays List of selected weekdays (1=Monday, 7=Sunday)
     * @return true if valid, false otherwise
     */
    public static boolean isValidWeekdaySelection(java.util.List<Integer> selectedDays) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            return false;
        }

        for (Integer day : selectedDays) {
            if (day == null || day < 1 || day > 7) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get error message for task name validation
     * @param name Task name to check
     * @return Error message or null if valid
     */
    public static String getTaskNameError(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Task name is required";
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < MIN_TASK_NAME_LENGTH) {
            return "Task name must be at least " + MIN_TASK_NAME_LENGTH + " characters";
        }

        if (trimmedName.length() > MAX_TASK_NAME_LENGTH) {
            return "Task name must be less than " + MAX_TASK_NAME_LENGTH + " characters";
        }

        if (!TASK_NAME_PATTERN.matcher(trimmedName).matches()) {
            return "Task name contains invalid characters";
        }

        return null; // Valid
    }

    /**
     * Get error message for child name validation
     * @param name Child name to check
     * @return Error message or null if valid
     */
    public static String getChildNameError(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Child name is required";
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < MIN_CHILD_NAME_LENGTH) {
            return "Name must be at least " + MIN_CHILD_NAME_LENGTH + " characters";
        }

        if (trimmedName.length() > MAX_CHILD_NAME_LENGTH) {
            return "Name must be less than " + MAX_CHILD_NAME_LENGTH + " characters";
        }

        if (!CHILD_NAME_PATTERN.matcher(trimmedName).matches()) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        return null; // Valid
    }

    /**
     * Get error message for invite code validation
     * @param code Invite code to check
     * @return Error message or null if valid
     */
    public static String getInviteCodeError(String code) {
        if (TextUtils.isEmpty(code)) {
            return "Invite code is required";
        }

        String trimmedCode = code.trim();

        if (trimmedCode.length() != INVITE_CODE_LENGTH) {
            return "Invite code must be exactly " + INVITE_CODE_LENGTH + " digits";
        }

        if (!INVITE_CODE_PATTERN.matcher(trimmedCode).matches()) {
            return "Invite code can only contain numbers";
        }

        return null; // Valid
    }

    // Callback interface for star balance validation
    public interface StarBalanceValidationCallback {
        void onValidationResult(boolean isValid, String message);
    }
}
