package com.chores.app.kids.chores_app_for_kids.models;


import android.os.Build;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class WeekDay {
    private String dayName;
    private int dayNumber;
    private LocalDate date;
    private boolean isSelected;
    private boolean isToday;

    public WeekDay(LocalDate date, boolean isSelected) {
        this.date = date;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.dayNumber = date.getDayOfMonth();
        }
        this.isSelected = isSelected;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public String getDayName() {
        return dayName;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFormattedDate() {
        return date.toString(); // YYYY-MM-DD format
    }
}
