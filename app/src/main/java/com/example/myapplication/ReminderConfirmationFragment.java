package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderConfirmationFragment extends Fragment {

    private ReminderViewModel reminderViewModel;
    private MedicationViewModel medicationViewModel;

    // Inflates the confirmation layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_confirmation, container, false);
    }

    // Shows the saved reminder summary and schedules the notification.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        reminderViewModel = new ViewModelProvider(requireActivity()).get(ReminderViewModel.class);
        medicationViewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        TextView summary = view.findViewById(R.id.summaryText);
        TextView messagePreview = view.findViewById(R.id.messagePreviewText);
        Button actionBtn = view.findViewById(R.id.restartBtn);

        showReminderSummary(summary, messagePreview);
        scheduleNotificationOnce();

        // If there's a pending medication, change button text and behavior.
        if (medicationViewModel.getPendingMedication() != null) {
            actionBtn.setText(R.string.reminder_add_to_med_button);
            actionBtn.setOnClickListener(v -> addTimesToMedicationAndReturn());
        } else {
            actionBtn.setText(R.string.reminder_restart_button);
            actionBtn.setOnClickListener(v -> restartFlow());
        }
    }

    // Formats the confirmation details for the user.
    private void showReminderSummary(TextView summary, TextView messagePreview) {
        String time = buildTimeSummary();
        String frequency = reminderViewModel.getFrequency() == null
                ? getString(R.string.reminder_frequency_missing)
                : reminderViewModel.getFrequency();
        String tone = ReminderMessageFormatter.getToneLabel(requireContext(), reminderViewModel.getTone());

        summary.setText(getString(R.string.reminder_confirmation_summary_template, time, frequency, tone));
        messagePreview.setText(ReminderMessageFormatter.getNotificationMessage(requireContext(), reminderViewModel.getTone()));
    }

    // Schedules the reminder once when the confirmation screen is shown.
    private void scheduleNotificationOnce() {
        if (reminderViewModel.isReminderScheduled() || !reminderViewModel.hasReminderTime()) {
            return;
        }

        Integer secondaryHour = reminderViewModel.hasSecondaryReminderTime()
            ? reminderViewModel.getSecondaryReminderHour()
            : null;
        Integer secondaryMinute = reminderViewModel.hasSecondaryReminderTime()
            ? reminderViewModel.getSecondaryReminderMinute()
            : null;

        boolean scheduled = ReminderScheduler.scheduleReminder(
                requireContext(),
                reminderViewModel.getReminderHour(),
                reminderViewModel.getReminderMinute(),
                secondaryHour,
                secondaryMinute,
                reminderViewModel.getFrequency(),
                reminderViewModel.getTone()
        );

        if (scheduled) {
            reminderViewModel.setReminderScheduled(true);
        } else {
            Toast.makeText(requireContext(), R.string.reminder_schedule_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // Adds the newly created reminder times to the pending medication and navigates back.
    private void addTimesToMedicationAndReturn() {
        Medication pending = medicationViewModel.getPendingMedication();
        if (pending != null) {
            List<String> times = new ArrayList<>(pending.getScheduledTimes());
            
            // Format and add the primary time
            String primaryTime = String.format(Locale.getDefault(), "%02d:%02d", 
                reminderViewModel.getReminderHour(), reminderViewModel.getReminderMinute());
            if (!times.contains(primaryTime)) {
                times.add(primaryTime);
            }

            // Format and add the secondary time if it exists
            if (reminderViewModel.hasSecondaryReminderTime()) {
                String secondaryTime = String.format(Locale.getDefault(), "%02d:%02d", 
                    reminderViewModel.getSecondaryReminderHour(), reminderViewModel.getSecondaryReminderMinute());
                if (!times.contains(secondaryTime)) {
                    times.add(secondaryTime);
                }
            }

            // Update the pending medication with the new times
            medicationViewModel.setPendingMedication(new Medication(pending.getName(), pending.getDosage(), times));
        }

        // Clear reminder state and return to Add Medication screen
        reminderViewModel.clear();
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_reminderConfirmation_to_addMedication);
    }

    // Resets the flow so the user can create another reminder (standard behavior).
    private void restartFlow() {
        ReminderScheduler.cancelAllReminders(requireContext());
        reminderViewModel.clear();
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_confirmation_to_home);
    }

    // Builds the reminder time summary shown on the confirmation card.
    private String buildTimeSummary() {
        String primaryTime = reminderViewModel.getFormattedReminderTime(requireContext());
        if (!reminderViewModel.hasSecondaryReminderTime()) {
            return primaryTime;
        }

        String secondaryTime = reminderViewModel.getFormattedSecondaryReminderTime(requireContext());
        return getString(R.string.reminder_two_times_summary_template, primaryTime, secondaryTime);
    }
}
