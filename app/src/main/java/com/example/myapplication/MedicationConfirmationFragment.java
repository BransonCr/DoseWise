package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

// Confirms the medication was saved and offers options to add another or return home.
public class MedicationConfirmationFragment extends Fragment {
    private MedicationViewModel viewModel;

    // Inflates the confirmation screen layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medication_confirmation, container, false);
    }

    // Populates confirmation text and wires the action buttons.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        TextView subtitleText = view.findViewById(R.id.confirmSubtitleText);
        TextView summaryText = view.findViewById(R.id.confirmSummaryText);

        bindSavedMedication(subtitleText, summaryText);

        view.findViewById(R.id.addAnotherBtn).setOnClickListener(v -> onAddAnotherClicked());
        view.findViewById(R.id.doneBtn).setOnClickListener(v -> onDoneClicked());
    }

    // Fills the subtitle and summary from the most recently confirmed medication.
    private void bindSavedMedication(TextView subtitleText, TextView summaryText) {
        Medication pending = viewModel.getPendingMedication();
        if (pending == null) {
            return;
        }
        subtitleText.setText(getString(R.string.med_confirm_subtitle_template, pending.getName()));
        String times = String.join(", ", pending.getScheduledTimes());
        summaryText.setText(getString(R.string.med_confirm_summary_template,
                pending.getName(), pending.getDosage(), times));
    }

    // Clears pending state and navigates back to the add form for another entry.
    private void onAddAnotherClicked() {
        viewModel.clearPendingMedication();
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.addMedicationFragment, true)
                .build();
        NavHostFragment.findNavController(this)
                .navigate(R.id.addMedicationFragment, null, navOptions);
    }

    // Clears pending state and navigates home, removing the add flow from the back stack.
    private void onDoneClicked() {
        viewModel.clearPendingMedication();
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .build();
        NavHostFragment.findNavController(this)
                .navigate(R.id.homeFragment, null, navOptions);
    }
}
