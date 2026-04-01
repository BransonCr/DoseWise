package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class ReminderTimingFragment extends Fragment {

    private ReminderViewModel viewModel;

    // Inflates the reminder timing layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_timing, container, false);
    }

    // Wires the time and frequency controls.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ReminderViewModel.class);

        TimePicker primaryTimePicker = view.findViewById(R.id.timePicker);
        TimePicker secondaryTimePicker = view.findViewById(R.id.secondTimePicker);
        LinearLayout secondaryTimeContainer = view.findViewById(R.id.secondTimeContainer);
        Spinner frequencySpinner = view.findViewById(R.id.frequencySpinner);
        TextView summaryText = view.findViewById(R.id.summaryText);
        Button nextBtn = view.findViewById(R.id.nextBtn);

        bindDefaults(primaryTimePicker, secondaryTimePicker, secondaryTimeContainer, frequencySpinner, summaryText);
        attachListeners(primaryTimePicker, secondaryTimePicker, secondaryTimeContainer, frequencySpinner, summaryText);
        nextBtn.setOnClickListener(v -> saveAndContinue(primaryTimePicker, secondaryTimePicker, frequencySpinner));
    }

    // Restores saved selections or applies defaults.
    private void bindDefaults(
            TimePicker primaryTimePicker,
            TimePicker secondaryTimePicker,
            LinearLayout secondaryTimeContainer,
            Spinner frequencySpinner,
            TextView summaryText
    ) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.frequency_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);

        if (viewModel.hasReminderTime()) {
            primaryTimePicker.setHour(viewModel.getReminderHour());
            primaryTimePicker.setMinute(viewModel.getReminderMinute());
        }

        if (viewModel.hasSecondaryReminderTime()) {
            secondaryTimePicker.setHour(viewModel.getSecondaryReminderHour());
            secondaryTimePicker.setMinute(viewModel.getSecondaryReminderMinute());
        }

        if (viewModel.getFrequency() != null) {
            int selectedIndex = findFrequencyIndex(adapter, viewModel.getFrequency());
            if (selectedIndex >= 0) {
                frequencySpinner.setSelection(selectedIndex);
            }
        }

        updateSecondaryVisibility(secondaryTimeContainer, frequencySpinner);
        updateSummary(summaryText, primaryTimePicker, secondaryTimePicker, frequencySpinner);
    }

    // Attaches listeners that keep the summary current.
    private void attachListeners(
            TimePicker primaryTimePicker,
            TimePicker secondaryTimePicker,
            LinearLayout secondaryTimeContainer,
            Spinner frequencySpinner,
            TextView summaryText
    ) {
        primaryTimePicker.setOnTimeChangedListener((picker, hourOfDay, minute) ->
                updateSummary(summaryText, picker, secondaryTimePicker, frequencySpinner));

        secondaryTimePicker.setOnTimeChangedListener((picker, hourOfDay, minute) ->
                updateSummary(summaryText, primaryTimePicker, picker, frequencySpinner));

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                updateSecondaryVisibility(secondaryTimeContainer, frequencySpinner);
                updateSummary(summaryText, primaryTimePicker, secondaryTimePicker, frequencySpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateSecondaryVisibility(secondaryTimeContainer, frequencySpinner);
                updateSummary(summaryText, primaryTimePicker, secondaryTimePicker, frequencySpinner);
            }
        });
    }

    // Shows or hides the second time control when Twice Daily is selected.
    private void updateSecondaryVisibility(LinearLayout secondaryTimeContainer, Spinner frequencySpinner) {
        String frequency = frequencySpinner.getSelectedItem() == null ? null : frequencySpinner.getSelectedItem().toString();
        boolean isTwiceDaily = isTwiceDaily(frequency);
        // Keep the second picker hidden unless the selected cadence requires it.
        secondaryTimeContainer.setVisibility(isTwiceDaily ? View.VISIBLE : View.GONE);
    }

    // Updates the live reminder summary.
    private void updateSummary(
            TextView summaryText,
            TimePicker primaryTimePicker,
            TimePicker secondaryTimePicker,
            Spinner frequencySpinner
    ) {
        String primaryTime = ReminderScheduler.formatTime(requireContext(), primaryTimePicker.getHour(), primaryTimePicker.getMinute());
        String frequency = frequencySpinner.getSelectedItem() == null
                ? getString(R.string.reminder_time_missing_frequency)
                : frequencySpinner.getSelectedItem().toString();

        if (isTwiceDaily(frequency)) {
            String secondaryTime = ReminderScheduler.formatTime(requireContext(), secondaryTimePicker.getHour(), secondaryTimePicker.getMinute());
            summaryText.setText(getString(R.string.reminder_timing_summary_twice_template, primaryTime, secondaryTime));
            return;
        }

        summaryText.setText(getString(R.string.reminder_timing_summary_template, frequency, primaryTime));
    }

    // Saves the timing selections and advances to the tone screen.
    private void saveAndContinue(TimePicker primaryTimePicker, TimePicker secondaryTimePicker, Spinner frequencySpinner) {
        String frequency = frequencySpinner.getSelectedItem() == null ? null : frequencySpinner.getSelectedItem().toString();
        if (frequency == null) {
            Toast.makeText(requireContext(), R.string.reminder_frequency_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.setReminderTime(primaryTimePicker.getHour(), primaryTimePicker.getMinute());

        if (isTwiceDaily(frequency)) {
            // Prevent duplicate alarms when both daily slots are set to the same time.
            boolean sameTime = primaryTimePicker.getHour() == secondaryTimePicker.getHour()
                    && primaryTimePicker.getMinute() == secondaryTimePicker.getMinute();
            if (sameTime) {
                Toast.makeText(requireContext(), R.string.reminder_second_time_unique_error, Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.setSecondaryReminderTime(secondaryTimePicker.getHour(), secondaryTimePicker.getMinute());
        } else {
            // Clear stale second-time data when user switches back to Daily.
            viewModel.clearSecondaryReminderTime();
        }

        viewModel.setFrequency(frequency);
        viewModel.setReminderScheduled(false);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_timing_to_tone);
    }

    // Finds the spinner index for the saved frequency.
    private int findFrequencyIndex(ArrayAdapter<CharSequence> adapter, String frequency) {
        for (int index = 0; index < adapter.getCount(); index++) {
            if (frequency.equals(adapter.getItem(index))) {
                return index;
            }
        }

        return -1;
    }

    // Returns true when the selected frequency is Twice Daily.
    private boolean isTwiceDaily(String frequency) {
        return getString(R.string.frequency_twice_daily).equals(frequency);
    }
}