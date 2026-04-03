package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    // Fires when the alarm triggers and shows the reminder notification.
    @Override
    public void onReceive(Context context, Intent intent) {
        ensureNotificationChannel(context);

        String tone = intent.getStringExtra(ReminderScheduler.EXTRA_TONE);
        String message = ReminderMessageFormatter.getNotificationMessage(context, tone);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, ReminderScheduler.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.reminder_notification_title))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(ReminderScheduler.NOTIFICATION_ID, builder.build());
    }

    // Makes sure the reminder channel exists before showing the notification.
    private void ensureNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                ReminderScheduler.NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
