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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

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

        MaterialToolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.homeFragment, R.id.caregiverDashboardFragment).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavView);
        NavigationUI.setupWithNavController(bottomNav, navController);

        MedicationViewModel viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);
        if (viewModel.getMedicationList().isEmpty()) {
            // Test Medication 1
            Medication lisinopril = new Medication("Lisinopril", "10mg", List.of("09:00"));
            viewModel.addMedication(lisinopril);
            viewModel.updateDoseStatus("Lisinopril", DoseStatus.UPCOMING);

            // Test Medication 2
            Medication aspirin = new Medication("Aspirin", "80mg", List.of("07:00"));
            viewModel.addMedication(aspirin);
            viewModel.updateDoseStatus("Aspirin", DoseStatus.TAKEN);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Requests notification permission on Android 13 and above.
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            // Show a warm-up dialog before triggering the system prompt
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Notifications Needed")
                    .setMessage("DoseWise needs to send you notifications so we can remind you when it's time to take your medication.")
                    .setPositiveButton("Continue", (dialog, which) -> {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    })
                    .setNegativeButton("Not Now", null)
                    .show();
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