package com.example.myapplication;

import android.content.Context;

public final class ReminderMessageFormatter {

    private ReminderMessageFormatter() {
    }

    // Returns the preview message for the currently selected tone.
    public static String getPreviewMessage(Context context, String tone) {
        if (ReminderScheduler.TONE_DIRECT.equals(tone)) {
            return context.getString(R.string.reminder_message_direct_preview);
        }

        return context.getString(R.string.reminder_message_caring_preview);
    }

    // Returns the notification message for the currently selected tone.
    public static String getNotificationMessage(Context context, String tone) {
        if (ReminderScheduler.TONE_DIRECT.equals(tone)) {
            return context.getString(R.string.reminder_message_direct_notification);
        }

        return context.getString(R.string.reminder_message_caring_notification);
    }

    // Returns a readable label for the selected tone.
    public static String getToneLabel(Context context, String tone) {
        if (ReminderScheduler.TONE_DIRECT.equals(tone)) {
            return context.getString(R.string.reminder_tone_direct_label);
        }

        return context.getString(R.string.reminder_tone_caring_label);
    }
}