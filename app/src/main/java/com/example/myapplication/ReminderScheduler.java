package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import java.util.Calendar;

public final class ReminderScheduler {

    public static final String NOTIFICATION_CHANNEL_ID = "dosewise_reminders";
    public static final int NOTIFICATION_ID = 1001;
    public static final String TONE_CARING = "caring";
    public static final String TONE_DIRECT = "direct";
    public static final String EXTRA_TONE = "extra_tone";
    private static final int FIRST_REMINDER_REQUEST_CODE = 2001;
    private static final int SECOND_REMINDER_REQUEST_CODE = 2002;

    private ReminderScheduler() {
    }

    // Schedules daily reminders for one or two selected times.
    public static boolean scheduleReminder(
            Context context,
            int hour,
            int minute,
            Integer secondHour,
            Integer secondMinute,
            String frequency,
            String tone
    ) {
        // Always schedule the primary daily reminder.
        boolean firstScheduled = scheduleReminderAt(context, FIRST_REMINDER_REQUEST_CODE, hour, minute, tone);
        if (!firstScheduled) {
            return false;
        }

        if (context.getString(R.string.frequency_twice_daily).equals(frequency)) {
            // Twice Daily requires a second independent alarm slot.
            if (secondHour == null || secondMinute == null) {
                return false;
            }

            return scheduleReminderAt(context, SECOND_REMINDER_REQUEST_CODE, secondHour, secondMinute, tone);
        }

        cancelReminder(context, SECOND_REMINDER_REQUEST_CODE);
        return true;
    }

    // Formats the selected time for display in the UI.
    public static String formatTime(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return DateFormat.getTimeFormat(context).format(calendar.getTime());
    }

    // Builds the next trigger time for the selected hour and minute.
    public static long buildNextTriggerMillis(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }

    // Cancels all scheduled reminder alarms.
    public static void cancelAllReminders(Context context) {
        cancelReminder(context, FIRST_REMINDER_REQUEST_CODE);
        cancelReminder(context, SECOND_REMINDER_REQUEST_CODE);
    }

    // Schedules a repeating alarm for one reminder slot.
    private static boolean scheduleReminderAt(Context context, int requestCode, int hour, int minute, String tone) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return false;
        }

        long triggerAtMillis = buildNextTriggerMillis(hour, minute);

        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
        reminderIntent.putExtra(EXTRA_TONE, tone);

        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Clear any stale alarm for this slot before scheduling a fresh one.
        cancelReminder(context, requestCode);

        try {
            // Inexact repeating avoids exact-alarm permission requirements.
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_DAY,
                    reminderPendingIntent
            );
            return true;
        } catch (SecurityException ignored) {
            // Gracefully fail so the UI can show a user-facing error.
            return false;
        }
    }

    // Cancels a scheduled reminder alarm.
    private static void cancelReminder(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(reminderPendingIntent);
    }
}