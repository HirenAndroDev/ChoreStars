package com.chores.app.kids.chores_app_for_kids.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isYesterday(long timestamp) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);

        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static String getRelativeTimeString(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            int minutes = (int) (diff / 60000);
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (diff < 86400000) { // Less than 1 day
            int hours = (int) (diff / 3600000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (isYesterday(timestamp)) {
            return "Yesterday";
        } else {
            return formatDate(timestamp);
        }
    }
}