package com.chores.app.kids.chores_app_for_kids.utils;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Queue;
import java.util.LinkedList;

public class TextToSpeechHelper {

    private TextToSpeech textToSpeech;
    private Context context;
    private boolean isInitialized = false;
    private Queue<SpeechItem> speechQueue;
    private boolean isSpeaking = false;

    // Speech settings
    private float speechRate = 0.8f; // Slightly slower for kids
    private float pitch = 1.1f; // Slightly higher pitch for friendliness

    public TextToSpeechHelper(Context context, TextToSpeech.OnInitListener initListener) {
        this.context = context;
        this.speechQueue = new LinkedList<>();

        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setupTextToSpeech();
                isInitialized = true;
            }
            if (initListener != null) {
                initListener.onInit(status);
            }
        });
    }

    private void setupTextToSpeech() {
        if (textToSpeech == null) return;

        // Set language (try to use device locale, fallback to English)
        int result = textToSpeech.setLanguage(Locale.getDefault());
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech.setLanguage(Locale.US);
        }

        // Configure speech parameters for kids
        textToSpeech.setSpeechRate(speechRate);
        textToSpeech.setPitch(pitch);

        // Set up utterance progress listener to handle queue
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isSpeaking = true;
            }

            @Override
            public void onDone(String utteranceId) {
                isSpeaking = false;
                processNextInQueue();
            }

            @Override
            public void onError(String utteranceId) {
                isSpeaking = false;
                processNextInQueue();
            }
        });
    }

    public void speak(String text) {
        speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    public void speak(String text, int queueMode) {
        speak(text, queueMode, null);
    }

    public void speak(String text, int queueMode, HashMap<String, String> params) {
        if (!isInitialized || textToSpeech == null || text == null || text.trim().isEmpty()) {
            return;
        }

        // Clean up text for better speech
        String cleanText = prepareTextForSpeech(text);

        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            // Clear queue and speak immediately
            speechQueue.clear();
            speakNow(cleanText, params);
        } else {
            // Add to queue
            speechQueue.offer(new SpeechItem(cleanText, params));
            if (!isSpeaking) {
                processNextInQueue();
            }
        }
    }

    private void processNextInQueue() {
        if (!speechQueue.isEmpty() && !isSpeaking) {
            SpeechItem item = speechQueue.poll();
            speakNow(item.text, item.params);
        }
    }

    private void speakNow(String text, HashMap<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        // Generate unique utterance ID
        String utteranceId = "utterance_" + System.currentTimeMillis();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    private String prepareTextForSpeech(String text) {
        // Clean up text for better speech experience
        return text
                .replaceAll("⭐", " star ")
                .replaceAll("\\+", " plus ")
                .replaceAll("&", " and ")
                .replaceAll("\\d+", " $0 ") // Add spaces around numbers
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    // Kid-friendly announcement methods

    public void announceTaskCompletion(String taskName, int stars) {
        String message = String.format("Awesome job! You completed %s and earned %d %s!",
                taskName, stars, stars == 1 ? "star" : "stars");
        speak(message, TextToSpeech.QUEUE_ADD);
    }

    public void announceRewardRedemption(String rewardName, int stars) {
        String message = String.format("Yay! You got %s! That cost %d %s.",
                rewardName, stars, stars == 1 ? "star" : "stars");
        speak(message, TextToSpeech.QUEUE_ADD);
    }

    public void announceStarBalance(int stars) {
        String message = String.format("You have %d %s!", stars, stars == 1 ? "star" : "stars");
        speak(message, TextToSpeech.QUEUE_ADD);
    }

    public void announceInsufficientStars(int needed) {
        String message = String.format("You need %d more %s for this reward. Keep up the great work!",
                needed, needed == 1 ? "star" : "stars");
        speak(message, TextToSpeech.QUEUE_ADD);
    }

    public void announceWelcome(String childName) {
        String message = String.format("Hi %s! Welcome back! Let's see what fun tasks you have today!", childName);
        speak(message, TextToSpeech.QUEUE_FLUSH); // Welcome message should be immediate
    }

    public void announceTaskList(int taskCount) {
        if (taskCount == 0) {
            speak("You have no tasks right now. Great job!", TextToSpeech.QUEUE_ADD);
        } else {
            String message = String.format("You have %d %s to complete today!",
                    taskCount, taskCount == 1 ? "task" : "tasks");
            speak(message, TextToSpeech.QUEUE_ADD);
        }
    }

    public void announceRewardList(int rewardCount) {
        if (rewardCount == 0) {
            speak("No rewards are available right now.", TextToSpeech.QUEUE_ADD);
        } else {
            String message = String.format("There %s %d %s you can get!",
                    rewardCount == 1 ? "is" : "are",
                    rewardCount,
                    rewardCount == 1 ? "reward" : "rewards");
            speak(message, TextToSpeech.QUEUE_ADD);
        }
    }

    public void announcePageChange(String pageName) {
        speak(String.format("Now showing %s", pageName), TextToSpeech.QUEUE_FLUSH);
    }

    public void announceError(String errorMessage) {
        String friendlyMessage = makeFriendlyErrorMessage(errorMessage);
        speak(friendlyMessage, TextToSpeech.QUEUE_ADD);
    }

    private String makeFriendlyErrorMessage(String errorMessage) {
        // Convert technical errors to kid-friendly messages
        if (errorMessage.toLowerCase().contains("network") ||
                errorMessage.toLowerCase().contains("connection")) {
            return "Oops! I'm having trouble connecting. Let's try again in a moment.";
        } else if (errorMessage.toLowerCase().contains("not found")) {
            return "Hmm, I can't find that. Let's double-check!";
        } else if (errorMessage.toLowerCase().contains("invalid")) {
            return "That doesn't look right. Can you try again?";
        } else {
            return "Something went wrong, but don't worry! Let's try again.";
        }
    }

    // Settings and controls

    public void setSpeechRate(float rate) {
        this.speechRate = Math.max(0.1f, Math.min(3.0f, rate)); // Clamp between 0.1 and 3.0
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(this.speechRate);
        }
    }

    public void setPitch(float pitch) {
        this.pitch = Math.max(0.1f, Math.min(2.0f, pitch)); // Clamp between 0.1 and 2.0
        if (textToSpeech != null) {
            textToSpeech.setPitch(this.pitch);
        }
    }

    public void setLanguage(Locale locale) {
        if (textToSpeech != null) {
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if requested language is not available
                textToSpeech.setLanguage(Locale.US);
            }
        }
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        speechQueue.clear();
        isSpeaking = false;
    }

    public void pause() {
        if (textToSpeech != null && isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
        }
    }

    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    public boolean isAvailable() {
        return isInitialized && textToSpeech != null;
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        speechQueue.clear();
        isInitialized = false;
        isSpeaking = false;
    }

    // Helper class for speech queue
    private static class SpeechItem {
        String text;
        HashMap<String, String> params;

        SpeechItem(String text, HashMap<String, String> params) {
            this.text = text;
            this.params = params;
        }
    }

    // Convenience methods for common announcements

    public void sayHello() {
        speak("Hello!", TextToSpeech.QUEUE_ADD);
    }

    public void sayGoodbye() {
        speak("Goodbye! See you later!", TextToSpeech.QUEUE_ADD);
    }

    public void sayWellDone() {
        String[] praises = {
                "Well done!",
                "Great job!",
                "Awesome!",
                "You're amazing!",
                "Fantastic work!",
                "You did it!"
        };
        String praise = praises[(int) (Math.random() * praises.length)];
        speak(praise, TextToSpeech.QUEUE_ADD);
    }

    public void sayKeepTrying() {
        String[] encouragements = {
                "Keep trying! You can do it!",
                "Don't give up! You're doing great!",
                "Almost there! Keep going!",
                "You're getting better every time!"
        };
        String encouragement = encouragements[(int) (Math.random() * encouragements.length)];
        speak(encouragement, TextToSpeech.QUEUE_ADD);
    }
}
