package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

// Displays the daily medication list and adherence streak.
public class HomeFragment extends Fragment {
    private MedicationViewModel viewModel;
    private LinearLayout medicationContainer;

    // Inflates the home screen layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    // Wires the streak display, medication list, and navigation buttons.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);
        medicationContainer = view.findViewById(R.id.medicationListContainer);
        TextView streakText = view.findViewById(R.id.streakCountText);
        View missedDoseBtn = view.findViewById(R.id.viewMissedDosesBtn);

        View addMedicationBtn = view.findViewById(R.id.addMedicationBtn);

        bindStreakCount(streakText);
        renderMedicationList();
        missedDoseBtn.setOnClickListener(v -> navigateToMissedDoses());
        addMedicationBtn.setOnClickListener(v -> navigateToAddMedication());
    }

    // Updates the streak counter text from the ViewModel.
    private void bindStreakCount(TextView streakText) {
        String formatted = getString(R.string.home_streak_template, viewModel.getStreakCount());
        streakText.setText(formatted);
    }

    // Clears and rebuilds the medication list UI based on current state.
    private void renderMedicationList() {
        medicationContainer.removeAllViews();
        for (Medication med : viewModel.getMedicationList()) {
            View medCard = createMedicationCard(med);
            medicationContainer.addView(medCard);
        }
    }

    // Creates a clickable card view for a single medication.
    private View createMedicationCard(Medication med) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_medication, medicationContainer, false);
        TextView nameText = card.findViewById(R.id.medNameText);
        TextView statusText = card.findViewById(R.id.medStatusText);
        ImageView statusIcon = card.findViewById(R.id.statusIcon);

        nameText.setText(med.getName());
        updateStatusDisplay(statusText, statusIcon, med.getName());

        // 🔗 CRITICAL: Pass medication name when navigating to confirmation
        card.setOnClickListener(v -> openDoseConfirmation(med.getName()));
        card.setClickable(true);
        card.setFocusable(true);

        return card;
    }

    // Sets the status icon, label, and color for a medication.
    private void updateStatusDisplay(TextView statusText, ImageView statusIcon, String medName) {
        DoseStatus status = viewModel.getDoseStatusMap().getOrDefault(medName, DoseStatus.UPCOMING);
        statusText.setText(status.name().toLowerCase());
        int iconRes = status == DoseStatus.TAKEN
                ? android.R.drawable.checkbox_on_background
                : android.R.drawable.ic_menu_close_clear_cancel;
        int colorRes = status == DoseStatus.TAKEN ? R.color.success : R.color.accent;
        statusIcon.setImageResource(iconRes);
        statusIcon.setColorFilter(ContextCompat.getColor(requireContext(), colorRes));
        statusText.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    // Opens the confirmation prompt WITH the medication name argument
    private void openDoseConfirmation(String medName) {
        Bundle args = new Bundle();
        args.putString("medicationName", medName);  // ← This is what was missing
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_dose_confirmation, args);
    }

    // Navigates to the missed dose tracking screen.
    private void navigateToMissedDoses() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_missedDose);
    }

    // Navigates to the add medication flow.
    private void navigateToAddMedication() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_addMedication);
    }
}