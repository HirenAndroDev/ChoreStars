package com.chores.app.kids.chores_app_for_kids;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.chores.app.kids.chores_app_for_kids.data.firebase.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.PrefsManager;

/**
 * Main Application class for Chores App
 * Initializes Firebase, notifications, and other app-wide configurations
 */
public class ChoresApplication extends Application {

    private static final String TAG = "ChoresApplication";

    // Singleton instance
    private static ChoresApplication instance;

    // Managers
    private PrefsManager prefsManager;
    private FirebaseManager firebaseManager;

    // Firebase instances
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // Initialize Firebase
        initializeFirebase();

        // Initialize Preferences Manager
        initializePreferences();

        // Initialize Notification Channels
        createNotificationChannels();

        // Initialize Firebase Cloud Messaging
        initializeFirebaseMessaging();

        // Set up crash reporting (optional - you can use Firebase Crashlytics)
        setupCrashReporting();

        // Enable Firebase offline persistence
        enableOfflinePersistence();

        Log.d(TAG, "Application initialized successfully");
    }

    /**
     * Initialize Firebase and its components
     */
    private void initializeFirebase() {
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(this);

            // Get Firebase instances
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();

            // Initialize Firebase Manager
            firebaseManager = FirebaseManager.getInstance();
            firebaseManager.initialize(this);

            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
        }
    }

    /**
     * Initialize SharedPreferences manager
     */
    private void initializePreferences() {
        prefsManager = new PrefsManager(this);
        Log.d(TAG, "Preferences manager initialized");
    }

    /**
     * Create notification channels for Android O and above
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Task Reminder Channel
            NotificationChannel taskReminderChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_TASK_REMINDER,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            taskReminderChannel.setDescription("Notifications for task reminders");
            taskReminderChannel.enableVibration(true);
            taskReminderChannel.setShowBadge(true);

            // Task Completion Channel
            NotificationChannel taskCompletionChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_TASK_COMPLETION,
                    "Task Completions",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            taskCompletionChannel.setDescription("Notifications when kids complete tasks");
            taskCompletionChannel.setShowBadge(true);

            // Reward Redemption Channel
            NotificationChannel rewardChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_REWARD,
                    "Reward Redemptions",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            rewardChannel.setDescription("Notifications for reward redemptions");
            rewardChannel.setShowBadge(true);

            // General Channel
            NotificationChannel generalChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            generalChannel.setDescription("General app notifications");

            // Create channels
            notificationManager.createNotificationChannel(taskReminderChannel);
            notificationManager.createNotificationChannel(taskCompletionChannel);
            notificationManager.createNotificationChannel(rewardChannel);
            notificationManager.createNotificationChannel(generalChannel);

            Log.d(TAG, "Notification channels created");
        }
    }

    /**
     * Initialize Firebase Cloud Messaging for push notifications
     */
    private void initializeFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);

                    // Save token to preferences
                    prefsManager.setFCMToken(token);

                    // Update token in Firebase if user is logged in
                    if (firebaseAuth.getCurrentUser() != null) {
                        firebaseManager.updateFCMToken(firebaseAuth.getCurrentUser().getUid(), token);
                    }
                });

        // Subscribe to topics based on user role
        subscribeToTopics();
    }

    /**
     * Subscribe to FCM topics based on user role
     */
    private void subscribeToTopics() {
        String userRole = prefsManager.getUserRole();

        if (userRole != null) {
            if (userRole.equals(Constants.ROLE_PARENT)) {
                FirebaseMessaging.getInstance().subscribeToTopic("parents");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("kids");
            } else if (userRole.equals(Constants.ROLE_KID)) {
                FirebaseMessaging.getInstance().subscribeToTopic("kids");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("parents");
            }
        }
    }

    /**
     * Enable Firebase offline persistence
     */
    private void enableOfflinePersistence() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase offline persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling offline persistence: " + e.getMessage());
        }
    }

    /**
     * Set up crash reporting (optional)
     */
    private void setupCrashReporting() {
        // You can integrate Firebase Crashlytics here
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Set up custom uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                Log.e(TAG, "Uncaught exception: " + throwable.getMessage());
                // Log to Firebase Crashlytics if integrated
                // FirebaseCrashlytics.getInstance().recordException(throwable);

                // Let the system handle the crash
                System.exit(1);
            }
        });
    }

    /**
     * Get application instance
     */
    public static ChoresApplication getInstance() {
        return instance;
    }

    /**
     * Get application context
     */
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    /**
     * Get Preferences Manager
     */
    public PrefsManager getPrefsManager() {
        return prefsManager;
    }

    /**
     * Get Firebase Manager
     */
    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    /**
     * Get Firebase Auth instance
     */
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    /**
     * Get Firebase Database instance
     */
    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Clear all app data on logout
     */
    public void clearAppData() {
        // Clear preferences
        prefsManager.clearAll();

        // Unsubscribe from all topics
        FirebaseMessaging.getInstance().unsubscribeFromTopic("parents");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("kids");

        // Clear any cached data
        firebaseManager.clearCache();

        Log.d(TAG, "App data cleared");
    }

    /**
     * Update user online status
     */
    public void updateUserOnlineStatus(boolean isOnline) {
        String userId = getCurrentUserId();
        if (userId != null) {
            firebaseManager.updateUserOnlineStatus(userId, isOnline);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Update user offline status
        updateUserOnlineStatus(false);
    }
}
