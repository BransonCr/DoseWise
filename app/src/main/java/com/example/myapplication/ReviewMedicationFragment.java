package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

// Shows a summary of the pending medication for the user to confirm before saving.
public class ReviewMedicationFragment extends Fragment {
    private MedicationViewModel viewModel;

    // Inflates the review medication layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_medication, container, false);
    }

    // Populates the summary fields and wires the confirm and edit buttons.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        TextView nameText = view.findViewById(R.id.reviewNameText);
        TextView dosageText = view.findViewById(R.id.reviewDosageText);
        TextView timesText = view.findViewById(R.id.reviewTimesText);

        bindPendingMedication(nameText, dosageText, timesText);

        view.findViewById(R.id.confirmBtn).setOnClickListener(v -> onConfirmClicked());
        view.findViewById(R.id.editBtn).setOnClickListener(v -> navigateBackToAdd());
    }

    // Fills the review fields from the pending medication in the ViewModel.
    private void bindPendingMedication(TextView nameText, TextView dosageText, TextView timesText) {
        Medication pending = viewModel.getPendingMedication();
        if (pending == null) {
            return;
        }
        nameText.setText(pending.getName());
        dosageText.setText(pending.getDosage());
        timesText.setText(String.join(", ", pending.getScheduledTimes()));
    }

    // Saves the pending medication to the list, sets its status, and navigates to confirmation.
    private void onConfirmClicked() {
        Medication pending = viewModel.getPendingMedication();
        if (pending == null) {
            return;
        }
        viewModel.addMedication(pending);
        viewModel.updateDoseStatus(pending.getName(), DoseStatus.UPCOMING);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_reviewMedication_to_medicationConfirmation);
    }

    // Navigates back to the add form so the user can edit their entry.
    private void navigateBackToAdd() {
        NavHostFragment.findNavController(this).navigateUp();
    }
}
