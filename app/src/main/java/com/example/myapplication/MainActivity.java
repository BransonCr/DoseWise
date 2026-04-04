package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    // Creates the reminder channel and opens the reminder flow.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestNotificationPermissionIfNeeded();
        createNotificationChannel();

        MedicationViewModel viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);
        if (viewModel.getMedicationList().isEmpty()) {
            // Test Medication 1
            Medication lisinopril = new Medication("Lisinopril", "10mg", Arrays.asList("09:00"));
            viewModel.addMedication(lisinopril);
            viewModel.updateDoseStatus("Lisinopril", DoseStatus.UPCOMING);

            // Test Medication 2
            Medication aspirin = new Medication("Aspirin", "80mg", Arrays.asList("07:00"));
            viewModel.addMedication(aspirin);
            viewModel.updateDoseStatus("Aspirin", DoseStatus.TAKEN);
        }
    }

    // Requests notification permission on Android 13 and above.
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // Creates the notification channel used by reminder alarms.
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    ReminderScheduler.NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}