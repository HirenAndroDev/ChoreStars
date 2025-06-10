package com.chores.app.kids.chores_app_for_kids.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.chores.app.kids.chores_app_for_kids.R;

import java.util.HashMap;
import java.util.Map;

public class SoundHelper {

    private static SoundPool soundPool;
    private static Map<String, Integer> soundMap;
    private static boolean isInitialized = false;
    private static Context appContext;

    // Sound effect constants
    public static final String SOUND_CLICK = "click";
    public static final String SOUND_SUCCESS = "success";
    public static final String SOUND_ERROR = "error";
    public static final String SOUND_CELEBRATION = "celebration";
    public static final String SOUND_STAR_EARNED = "star_earned";
    public static final String SOUND_REWARD = "reward";
    public static final String SOUND_TASK_COMPLETE = "task_complete";
    public static final String SOUND_NOTIFICATION = "notification";

    public static void initialize(Context context) {
        if (isInitialized) return;

        appContext = context.getApplicationContext();
        soundMap = new HashMap<>();

        // Create SoundPool based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        loadSounds();
        isInitialized = true;
    }

    private static void loadSounds() {
        try {
            // Load sound effects from raw resources
            soundMap.put(SOUND_CLICK, soundPool.load(appContext, R.raw.sound_click, 1));
            soundMap.put(SOUND_SUCCESS, soundPool.load(appContext, R.raw.sound_success, 1));
            soundMap.put(SOUND_ERROR, soundPool.load(appContext, R.raw.sound_error, 1));
            soundMap.put(SOUND_CELEBRATION, soundPool.load(appContext, R.raw.sound_celebration, 1));
            soundMap.put(SOUND_STAR_EARNED, soundPool.load(appContext, R.raw.sound_success, 1));
            soundMap.put(SOUND_REWARD, soundPool.load(appContext, R.raw.sound_reward, 1));
            soundMap.put(SOUND_TASK_COMPLETE, soundPool.load(appContext, R.raw.sound_success, 1));
            soundMap.put(SOUND_NOTIFICATION, soundPool.load(appContext, R.raw.sound_success, 1));
        } catch (Exception e) {
            // If sound files don't exist, create placeholder entries
            for (String soundKey : new String[]{SOUND_CLICK, SOUND_SUCCESS, SOUND_ERROR,
                    SOUND_CELEBRATION, SOUND_STAR_EARNED, SOUND_REWARD,
                    SOUND_TASK_COMPLETE, SOUND_NOTIFICATION}) {
                soundMap.put(soundKey, -1); // Placeholder
            }
        }
    }

    private static void playSound(Context context, String soundKey, float volume) {
        if (!isInitialized) {
            initialize(context);
        }

        // Check if sound is enabled in settings
        if (!isSoundEnabled(context)) {
            return;
        }

        Integer soundId = soundMap.get(soundKey);
        if (soundId != null && soundId != -1) {
            try {
                soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
            } catch (Exception e) {
                // Silently handle sound playback errors
            }
        }
    }

    private static void playVibration(Context context, long duration) {
        if (!isVibrationEnabled(context)) {
            return;
        }

        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(duration);
                }
            }
        } catch (Exception e) {
            // Silently handle vibration errors
        }
    }

    // Public methods for different sound effects

    public static void playClickSound(Context context) {
        playSound(context, SOUND_CLICK, 0.5f);
        playVibration(context, 50); // Short vibration
    }

    public static void playSuccessSound(Context context) {
        playSound(context, SOUND_SUCCESS, 0.7f);
        playVibration(context, 100);
    }

    public static void playErrorSound(Context context) {
        playSound(context, SOUND_ERROR, 0.6f);
        playVibration(context, 200); // Longer vibration for errors
    }

    public static void playCelebrationSound(Context context) {
        playSound(context, SOUND_CELEBRATION, 0.8f);
        // Custom vibration pattern for celebration
        playCustomVibration(context, new long[]{0, 100, 100, 100, 100, 200});
    }

    public static void playStarEarnedSound(Context context) {
        playSound(context, SOUND_STAR_EARNED, 0.7f);
        playVibration(context, 150);
    }

    public static void playRewardSound(Context context) {
        playSound(context, SOUND_REWARD, 0.8f);
        playCustomVibration(context, new long[]{0, 50, 50, 100, 50, 150});
    }

    public static void playTaskCompleteSound(Context context) {
        playSound(context, SOUND_TASK_COMPLETE, 0.7f);
        playVibration(context, 120);
    }

    public static void playNotificationSound(Context context) {
        playSound(context, SOUND_NOTIFICATION, 0.6f);
        playVibration(context, 80);
    }

    private static void playCustomVibration(Context context, long[] pattern) {
        if (!isVibrationEnabled(context)) {
            return;
        }

        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vibrator.vibrate(pattern, -1);
                }
            }
        } catch (Exception e) {
            // Silently handle vibration errors
        }
    }

    // Settings helpers

    private static boolean isSoundEnabled(Context context) {
        // Check app-specific sound settings
        // For now, return true. You can implement SharedPreferences check here
        return true;
    }

    private static boolean isVibrationEnabled(Context context) {
        // Check app-specific vibration settings
        // For now, return true. You can implement SharedPreferences check here
        return true;
    }

    public static void setSoundEnabled(Context context, boolean enabled) {
        // Save sound preference to SharedPreferences
        context.getSharedPreferences("NeatKidPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("sound_enabled", enabled)
                .apply();
    }

    public static void setVibrationEnabled(Context context, boolean enabled) {
        // Save vibration preference to SharedPreferences
        context.getSharedPreferences("NeatKidPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("vibration_enabled", enabled)
                .apply();
    }

    public static boolean getSoundEnabled(Context context) {
        return context.getSharedPreferences("NeatKidPrefs", Context.MODE_PRIVATE)
                .getBoolean("sound_enabled", true);
    }

    public static boolean getVibrationEnabled(Context context) {
        return context.getSharedPreferences("NeatKidPrefs", Context.MODE_PRIVATE)
                .getBoolean("vibration_enabled", true);
    }

    // Volume control

    public static void setVolume(Context context, float volume) {
        // Clamp volume between 0.0 and 1.0
        volume = Math.max(0.0f, Math.min(1.0f, volume));
        context.getSharedPreferences("NeatKidPrefs", Context.MODE_PRIVATE)
                .edit()
                .putFloat("sound_volume", volume)
                .apply();
    }

    public static float getVolume(Context context) {
        return context.getSharedPreferences("NeatKidPrefs", Context.MODE_PRIVATE)
                .getFloat("sound_volume", 0.7f);
    }

    // Cleanup

    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (soundMap != null) {
            soundMap.clear();
        }
        isInitialized = false;
    }

    // Kid-friendly sound combinations

    public static void playHappyFeedback(Context context) {
        playSuccessSound(context);
        // Additional happy feedback can be added here
    }

    public static void playSadFeedback(Context context) {
        playErrorSound(context);
        // Additional sad feedback can be added here
    }

    public static void playExcitedFeedback(Context context) {
        playCelebrationSound(context);
        // Additional excited feedback can be added here
    }
}
