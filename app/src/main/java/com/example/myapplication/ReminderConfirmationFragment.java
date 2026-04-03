package com.example.myapplication;

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

public class ReminderConfirmationFragment extends Fragment {

    private ReminderViewModel viewModel;

    // Inflates the confirmation layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_confirmation, container, false);
    }

    // Shows the saved reminder summary and schedules the notification.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ReminderViewModel.class);

        TextView summary = view.findViewById(R.id.summaryText);
        TextView messagePreview = view.findViewById(R.id.messagePreviewText);
        Button restartBtn = view.findViewById(R.id.restartBtn);

        showReminderSummary(summary, messagePreview);
        scheduleNotificationOnce();

        restartBtn.setOnClickListener(v -> restartFlow());
    }

    // Formats the confirmation details for the user.
    private void showReminderSummary(TextView summary, TextView messagePreview) {
        String time = buildTimeSummary();
        String frequency = viewModel.getFrequency() == null
                ? getString(R.string.reminder_frequency_missing)
                : viewModel.getFrequency();
        String tone = ReminderMessageFormatter.getToneLabel(requireContext(), viewModel.getTone());

        summary.setText(getString(R.string.reminder_confirmation_summary_template, time, frequency, tone));
        messagePreview.setText(ReminderMessageFormatter.getNotificationMessage(requireContext(), viewModel.getTone()));
    }

    // Schedules the reminder once when the confirmation screen is shown.
    private void scheduleNotificationOnce() {
        // Avoid scheduling duplicates if the user revisits this screen.
        if (viewModel.isReminderScheduled() || !viewModel.hasReminderTime()) {
            return;
        }

        Integer secondaryHour = viewModel.hasSecondaryReminderTime()
            ? viewModel.getSecondaryReminderHour()
            : null;
        Integer secondaryMinute = viewModel.hasSecondaryReminderTime()
            ? viewModel.getSecondaryReminderMinute()
            : null;

        boolean scheduled = ReminderScheduler.scheduleReminder(
                requireContext(),
                viewModel.getReminderHour(),
                viewModel.getReminderMinute(),
            secondaryHour,
            secondaryMinute,
                viewModel.getFrequency(),
                viewModel.getTone()
        );

        if (scheduled) {
            viewModel.setReminderScheduled(true);
        } else {
            Toast.makeText(requireContext(), R.string.reminder_schedule_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // Resets the flow so the user can create another reminder.
    private void restartFlow() {
        ReminderScheduler.cancelAllReminders(requireContext());
        viewModel.clear();

        NavHostFragment.findNavController(this)
                .popBackStack(R.id.reminderTimingFragment, false);
    }

    // Builds the reminder time summary shown on the confirmation card.
    private String buildTimeSummary() {
        String primaryTime = viewModel.getFormattedReminderTime(requireContext());
        if (!viewModel.hasSecondaryReminderTime()) {
            return primaryTime;
        }

        // For Twice Daily, present both selected slots in the confirmation summary.
        String secondaryTime = viewModel.getFormattedSecondaryReminderTime(requireContext());
        return getString(R.string.reminder_two_times_summary_template, primaryTime, secondaryTime);
    }
}
