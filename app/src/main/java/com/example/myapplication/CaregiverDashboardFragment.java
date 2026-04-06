package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import java.util.Map;

// Displays caregiver monitoring dashboard with alerts and weekly summary tabs.
public class CaregiverDashboardFragment extends Fragment {
    private MedicationViewModel viewModel;
    private View alertsContent;
    private View weeklyContent;
    private LinearLayout missedListContainer;
    private MaterialButton alertsTab;
    private MaterialButton weeklyTab;

    // Inflates the caregiver dashboard layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_caregiver_dashboard, container, false);
    }

    // Wires dependent info, adherence stats, tab toggle, and missed dose list.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        alertsContent = view.findViewById(R.id.alertsContent);
        weeklyContent = view.findViewById(R.id.weeklyContent);
        missedListContainer = view.findViewById(R.id.missedListContainer);

        alertsTab = view.findViewById(R.id.alertsTab);
        weeklyTab = view.findViewById(R.id.weeklyTab);

        bindDependentInfo(view.findViewById(R.id.dependentInitials), view.findViewById(R.id.dependentNameText));
        bindAdherenceStats(view.findViewById(R.id.adherencePercentageText),
                view.findViewById(R.id.takenCountText), view.findViewById(R.id.missedCountText));
        renderMissedDoses();
        bindWeeklySummaryStats(view);

        alertsTab.setOnClickListener(v -> showAlertsTab());
        weeklyTab.setOnClickListener(v -> showWeeklyTab());

        view.findViewById(R.id.button).setOnClickListener(v -> navigateToHome());
    }

    // Navigates back to the home screen.
    private void navigateToHome() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_caregiverDashboard_to_home);
    }

    // Sets the dependent's initials and full name from the ViewModel.
    private void bindDependentInfo(TextView initialsText, TextView nameText) {
        String name = viewModel.getCaregiverDependentName();
        if (name == null || name.isEmpty()) {
            initialsText.setText("?");
            nameText.setText(R.string.caregiver_no_dependent);
            return;
        }
        String[] parts = name.trim().split("\\s+");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)
                : String.valueOf(parts[0].charAt(0));
        initialsText.setText(initials.toUpperCase());
        nameText.setText(name);
    }

    // Calculates taken count, missed count, and adherence percentage from the ViewModel.
    private void bindAdherenceStats(TextView percentageText, TextView takenText, TextView missedText) {
        int total = viewModel.getMedicationList().size();
        long taken = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.TAKEN).count();
        long missed = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.MISSED).count();
        int percentage = total > 0 ? (int) (taken * 100 / total) : 0;
        percentageText.setText(getString(R.string.caregiver_adherence_percentage, percentage));
        takenText.setText(getString(R.string.caregiver_taken_label, taken));
        missedText.setText(getString(R.string.caregiver_missed_label, missed));
    }

    // Populates the missed doses list using current dose status data.
    private void renderMissedDoses() {
        missedListContainer.removeAllViews();
        boolean hasMissed = false;
        for (Map.Entry<String, DoseStatus> entry : viewModel.getDoseStatusMap().entrySet()) {
            if (entry.getValue() == DoseStatus.MISSED) {
                addMissedCard(entry.getKey());
                hasMissed = true;
            }
        }
        if (!hasMissed) showEmptyState();
    }

    // Inflates and adds a missed dose card for the given medication name.
    private void addMissedCard(String medName) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_missed_dose, missedListContainer, false);
        ((TextView) card.findViewById(R.id.missedMedName)).setText(medName);
        ((TextView) card.findViewById(R.id.missedActionHint)).setText(R.string.missed_action_hint);
        missedListContainer.addView(card);
    }

    // Shows a message when there are no missed doses to display.
    private void showEmptyState() {
        TextView empty = new TextView(requireContext());
        empty.setText(R.string.no_missed_doses);
        empty.setTextSize(16);
        empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        missedListContainer.addView(empty);
    }

    // Binds total taken, missed, and adherence rate to the weekly summary stats section.
    private void bindWeeklySummaryStats(View view) {
        long taken = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.TAKEN).count();
        long missed = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.MISSED).count();
        long total = taken + missed;
        int rate = total > 0 ? (int) (taken * 100 / total) : 0;
        ((TextView) view.findViewById(R.id.weeklyTakenCount)).setText(String.valueOf(taken));
        ((TextView) view.findViewById(R.id.weeklyMissedCount)).setText(String.valueOf(missed));
        ((TextView) view.findViewById(R.id.weeklyAdherenceRate))
                .setText(getString(R.string.caregiver_adherence_percentage, rate));
    }

    // Switches content to alerts and updates tab button highlight states.
    private void showAlertsTab() {
        alertsContent.setVisibility(View.VISIBLE);
        weeklyContent.setVisibility(View.GONE);
        alertsTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
        alertsTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        weeklyTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.screen_background));
        weeklyTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }

    // Switches content to weekly summary and updates tab button highlight states.
    private void showWeeklyTab() {
        alertsContent.setVisibility(View.GONE);
        weeklyContent.setVisibility(View.VISIBLE);
        weeklyTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
        weeklyTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        alertsTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.screen_background));
        alertsTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }
}
