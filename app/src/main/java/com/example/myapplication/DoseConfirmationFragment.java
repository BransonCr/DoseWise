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

// Prompts the user to confirm taking or missing a medication dose.
public class DoseConfirmationFragment extends Fragment {
    private MedicationViewModel viewModel;
    private String currentMedName;

    // Inflates the confirmation dialog layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dose_confirmation, container, false);
    }

    // Retrieves the medication argument and wires the action buttons.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);
        currentMedName = getArguments() != null ? getArguments().getString("medicationName") : "";

        TextView titleText = view.findViewById(R.id.confirmationTitle);
        Button takenBtn = view.findViewById(R.id.markTakenBtn);
        Button missedBtn = view.findViewById(R.id.markMissedBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);

        titleText.setText(getString(R.string.dose_confirm_title_template, currentMedName));
        takenBtn.setOnClickListener(v -> logDoseAndReturn(DoseStatus.TAKEN));
        missedBtn.setOnClickListener(v -> logDoseAndReturn(DoseStatus.MISSED));
        cancelBtn.setOnClickListener(v -> navigateBack());
    }

    // Updates the ViewModel and returns to the home screen.
    private void logDoseAndReturn(DoseStatus status) {
        if (currentMedName.isEmpty()) return;
        viewModel.updateDoseStatus(currentMedName, status);
        if (status == DoseStatus.TAKEN) {
            viewModel.incrementStreak();
            Toast.makeText(requireContext(), R.string.dose_logged_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), R.string.dose_logged_missed, Toast.LENGTH_SHORT).show();
        }
        navigateBack();
    }

    // Pops the current fragment off the navigation stack.
    private void navigateBack() {
        NavHostFragment.findNavController(this).popBackStack();
    }
}