package com.example.myapplication;

import android.content.Context;

import androidx.lifecycle.ViewModel;

public class ReminderViewModel extends ViewModel {

    private int reminderHour = -1;
    private int reminderMinute = -1;
    private int secondaryReminderHour = -1;
    private int secondaryReminderMinute = -1;
    private String frequency;
    private String tone;
    private boolean reminderScheduled;

    // Stores the selected reminder time.
    public void setReminderTime(int hour, int minute) {
        this.reminderHour = hour;
        this.reminderMinute = minute;
    }

    // Returns the selected reminder hour.
    public int getReminderHour() {
        return reminderHour;
    }

    // Returns the selected reminder minute.
    public int getReminderMinute() {
        return reminderMinute;
    }

    // Returns true when a reminder time exists.
    public boolean hasReminderTime() {
        return reminderHour >= 0 && reminderMinute >= 0;
    }

    // Stores the selected secondary reminder time.
    public void setSecondaryReminderTime(int hour, int minute) {
        this.secondaryReminderHour = hour;
        this.secondaryReminderMinute = minute;
    }

    // Clears the selected secondary reminder time.
    public void clearSecondaryReminderTime() {
        this.secondaryReminderHour = -1;
        this.secondaryReminderMinute = -1;
    }

    // Returns the selected secondary reminder hour.
    public int getSecondaryReminderHour() {
        return secondaryReminderHour;
    }

    // Returns the selected secondary reminder minute.
    public int getSecondaryReminderMinute() {
        return secondaryReminderMinute;
    }

    // Returns true when a secondary reminder time exists.
    public boolean hasSecondaryReminderTime() {
        return secondaryReminderHour >= 0 && secondaryReminderMinute >= 0;
    }

    // Stores the selected reminder frequency.
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    // Returns the selected reminder frequency.
    public String getFrequency() {
        return frequency;
    }

    // Stores the selected reminder tone.
    public void setTone(String tone) {
        this.tone = tone;
    }

    // Returns the selected reminder tone.
    public String getTone() {
        return tone;
    }

    // Stores whether the reminder has already been scheduled.
    public void setReminderScheduled(boolean reminderScheduled) {
        this.reminderScheduled = reminderScheduled;
    }

    // Returns true when the reminder has been scheduled.
    public boolean isReminderScheduled() {
        return reminderScheduled;
    }

    // Clears the current reminder configuration.
    public void clear() {
        reminderHour = -1;
        reminderMinute = -1;
        secondaryReminderHour = -1;
        secondaryReminderMinute = -1;
        frequency = null;
        tone = null;
        reminderScheduled = false;
    }

    // Builds a formatted summary of the selected reminder time.
    public String getFormattedReminderTime(Context context) {
        if (!hasReminderTime()) {
            return context.getString(R.string.reminder_time_not_set);
        }

        return ReminderScheduler.formatTime(context, reminderHour, reminderMinute);
    }

    // Builds a formatted summary of the selected secondary reminder time.
    public String getFormattedSecondaryReminderTime(Context context) {
        if (!hasSecondaryReminderTime()) {
            return context.getString(R.string.reminder_time_not_set);
        }

        return ReminderScheduler.formatTime(context, secondaryReminderHour, secondaryReminderMinute);
    }
}
