package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

// Displays a list of medications marked as missed with next actions.
public class MissedDoseFragment extends Fragment {
    private MedicationViewModel viewModel;
    private LinearLayout missedListContainer;

    // Inflates the missed dose screen layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_missed_dose, container, false);
    }

    // Wires the back button and renders the missed medications list.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);
        missedListContainer = view.findViewById(R.id.missedDoseListContainer);
        Button backBtn = view.findViewById(R.id.returnHomeBtn);

        renderMissedDoses();
        backBtn.setOnClickListener(v -> navigateBack());
    }

    // Clears the list and populates it with currently missed medications.
    private void renderMissedDoses() {
        missedListContainer.removeAllViews();
        boolean hasMissed = false;
        for (String medName : viewModel.getDoseStatusMap().keySet()) {
            if (viewModel.getDoseStatusMap().get(medName) == DoseStatus.MISSED) {
                addMissedDoseCard(medName);
                hasMissed = true;
            }
        }
        if (!hasMissed) showEmptyState();
    }

    // Adds a card view for a single missed medication.
    private void addMissedDoseCard(String medName) {
        View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_missed_dose, missedListContainer, false);
        TextView nameText = card.findViewById(R.id.missedMedName);
        TextView actionText = card.findViewById(R.id.missedActionHint);
        nameText.setText(medName);
        actionText.setText(getString(R.string.missed_action_hint));
        missedListContainer.addView(card);
    }

    // Displays a placeholder when no doses have been missed.
    private void showEmptyState() {
        TextView emptyText = new TextView(requireContext());
        emptyText.setText(R.string.no_missed_doses);
        emptyText.setTextSize(16);
        emptyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        missedListContainer.addView(emptyText);
    }

    // Returns to the main home screen.
    private void navigateBack() {
        NavHostFragment.findNavController(this).popBackStack();
    }
}