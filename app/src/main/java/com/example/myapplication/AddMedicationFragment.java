package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import java.util.ArrayList;
import java.util.List;

// Lets the user enter medication name, dosage, and one or more scheduled times.
public class AddMedicationFragment extends Fragment {
    private MedicationViewModel viewModel;
    private EditText nameInput;
    private EditText dosageInput;
    private LinearLayout timesContainer;
    private TextView noTimesText;
    private final List<String> scheduledTimes = new ArrayList<>();

    // Inflates the add medication form layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_medication, container, false);
    }

    // Binds form fields and button listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        nameInput = view.findViewById(R.id.nameInput);
        dosageInput = view.findViewById(R.id.dosageInput);
        timesContainer = view.findViewById(R.id.timesContainer);
        noTimesText = view.findViewById(R.id.noTimesText);

        // Navigate to Reminder Builder when Add Timer is clicked
        view.findViewById(R.id.addTimeBtn).setOnClickListener(v -> {
            saveCurrentStateToViewModel();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_addMedication_to_reminderTiming);
        });

        view.findViewById(R.id.continueBtn).setOnClickListener(v -> onContinueClicked());

        restoreFormStateIfReturningFromReview();
    }

    // Saves the current text input to the ViewModel so it's preserved during navigation.
    private void saveCurrentStateToViewModel() {
        String name = nameInput.getText().toString().trim();
        String dosage = dosageInput.getText().toString().trim();
        viewModel.setPendingMedication(new Medication(name, dosage, new ArrayList<>(scheduledTimes)));
    }

    // Pre-fills the form with pending medication data when the user returns to this screen.
    private void restoreFormStateIfReturningFromReview() {
        Medication pending = viewModel.getPendingMedication();
        if (pending != null) {
            nameInput.setText(pending.getName());
            dosageInput.setText(pending.getDosage());
            scheduledTimes.clear();
            scheduledTimes.addAll(pending.getScheduledTimes());
            rebuildTimesUI();
        }
    }

    // Adds a time row with a remove button to the times container.
    private void addTimeRow(String time) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_time_entry, timesContainer, false);
        TextView timeLabel = row.findViewById(R.id.timeEntryLabel);
        View removeBtn = row.findViewById(R.id.removeTimeBtn);

        timeLabel.setText(time);
        removeBtn.setOnClickListener(v -> removeTimeRow(time, row));
        timesContainer.addView(row);
    }

    // Removes a specific time row from the UI and the times list.
    private void removeTimeRow(String time, View row) {
        scheduledTimes.remove(time);
        timesContainer.removeView(row);
        if (scheduledTimes.isEmpty()) {
            noTimesText.setVisibility(View.VISIBLE);
        }
    }

    // Clears and rebuilds all time rows from the current scheduledTimes list.
    private void rebuildTimesUI() {
        timesContainer.removeAllViews();
        noTimesText.setVisibility(scheduledTimes.isEmpty() ? View.VISIBLE : View.GONE);
        for (String time : scheduledTimes) {
            addTimeRow(time);
        }
    }

    // Validates the form and navigates to the review screen if valid.
    private void onContinueClicked() {
        String name = nameInput.getText().toString().trim();
        String dosage = dosageInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.add_med_name_required));
            return;
        }
        if (dosage.isEmpty()) {
            dosageInput.setError(getString(R.string.add_med_dosage_required));
            return;
        }
        if (scheduledTimes.isEmpty()) {
            Toast.makeText(requireContext(), R.string.add_med_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.setPendingMedication(new Medication(name, dosage, new ArrayList<>(scheduledTimes)));
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_addMedication_to_reviewMedication);
    }
}
