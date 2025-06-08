package com.chores.app.kids.chores_app_for_kids.utils;

public class Constants {
    // Firestore Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_FAMILIES = "families";
    public static final String COLLECTION_KIDS = "kids";
    public static final String COLLECTION_TASKS = "tasks";
    public static final String COLLECTION_REWARDS = "rewards";
    public static final String COLLECTION_TASK_COMPLETIONS = "task_completions";
    public static final String COLLECTION_REWARD_REDEMPTIONS = "reward_redemptions";
    public static final String COLLECTION_STAR_TRANSACTIONS = "star_transactions";

    // User Roles
    public static final String ROLE_PARENT = "parent";
    public static final String ROLE_KID = "kid";

    // Task Repeat Frequencies
    public static final String REPEAT_DAILY = "daily";
    public static final String REPEAT_WEEKLY = "weekly";
    public static final String REPEAT_MONTHLY = "monthly";

    // Reward Renewal Periods
    public static final String RENEWAL_NONE = "none";
    public static final String RENEWAL_DAILY = "daily";
    public static final String RENEWAL_WEEKLY = "weekly";
    public static final String RENEWAL_MONTHLY = "monthly";

    // Star Transaction Types
    public static final String TRANSACTION_EARNED = "earned";
    public static final String TRANSACTION_SPENT = "spent";
    public static final String TRANSACTION_ADJUSTMENT = "adjustment";

    // Redemption Status
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    // Shared Preferences Keys
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_FAMILY_ID = "family_id";
    public static final String PREF_KID_ID = "kid_id";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";

    // Intent Extra Keys
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_REWARD_ID = "reward_id";
    public static final String EXTRA_KID_ID = "kid_id";
    public static final String EXTRA_FAMILY_ID = "family_id";
    public static final String EXTRA_INVITE_CODE = "invite_code";

    // Task Icons
    public static final String ICON_BRUSH_TEETH = "brush_teeth";
    public static final String ICON_PUT_AWAY_TOYS = "put_away_toys";
    public static final String ICON_FOLD_CLOTHES = "fold_clothes";
    public static final String ICON_DO_LAUNDRY = "do_laundry";

    // Reward Icons
    public static final String ICON_ICE_CREAM = "ice_cream";
    public static final String ICON_BUY_TOY = "buy_toy";
    public static final String ICON_PIZZA = "pizza";
    public static final String ICON_MONEY = "money";
    public static final String ICON_WATCH_MOVIE = "watch_movie";
    public static final String ICON_PLAY_GAMES = "play_games";

    // Default Values
    public static final int DEFAULT_STARS_PER_TASK = 1;
    public static final int DEFAULT_REWARD_COST = 5;

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String TIME_FORMAT_DISPLAY = "hh:mm a";
    public static final String DATETIME_FORMAT_DISPLAY = "MMM dd, yyyy hh:mm a";

    // Notification Channel IDs
    public static final String CHANNEL_TASK_REMINDERS = "task_reminders";
    public static final String CHANNEL_TASK_COMPLETIONS = "task_completions";
    public static final String CHANNEL_REWARD_NOTIFICATIONS = "reward_notifications";

    // Request Codes
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1001;
    public static final int REQUEST_CODE_CAMERA = 1002;
    public static final int REQUEST_CODE_GALLERY = 1003;

    // Storage Paths
    public static final String STORAGE_PROFILE_IMAGES = "profile_images/";
    public static final String STORAGE_TASK_PROOFS = "task_proofs/";
}