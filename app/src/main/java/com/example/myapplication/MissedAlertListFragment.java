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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

// Displays a list of all missed dose alerts for the caregiver.
public class MissedAlertListFragment extends Fragment {
    private MedicationViewModel viewModel;
    private LinearLayout alertListContainer;

    // Inflates the missed alert list layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_missed_alert_list, container, false);
    }

    // Wires the ViewModel, renders the alert list, and sets up the back button.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);
        alertListContainer = view.findViewById(R.id.alertListContainer);

        renderAlertList();

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    // Populates the list with a card for each missed dose, or shows an empty state.
    private void renderAlertList() {
        alertListContainer.removeAllViews();
        String owner = viewModel.getCaregiverDependentName();
        boolean hasMissed = false;
        if (CaregiverMockData.isCurrentUser(owner)) {
            for (Map.Entry<String, DoseStatus> entry : viewModel.getDoseStatusMap().entrySet()) {
                if (entry.getValue() == DoseStatus.MISSED) {
                    addAlertCard(entry.getKey(), viewModel.getMissedTimestamp(entry.getKey()));
                    hasMissed = true;
                }
            }
        } else {
            for (CaregiverMockData.MedRecord r :
                    CaregiverMockData.MOCK_MEDS.getOrDefault(owner, new java.util.ArrayList<>())) {
                if (r.status == DoseStatus.MISSED) {
                    addAlertCard(r.name, r.missedAt > 0 ? r.missedAt : null);
                    hasMissed = true;
                }
            }
        }
        if (!hasMissed) showEmptyState();
    }

    // Inflates and adds an alert card for the given medication name with its timestamp.
    private void addAlertCard(String medName, Long timestamp) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_missed_alert, alertListContainer, false);
        ((TextView) card.findViewById(R.id.alertMedName)).setText(medName);
        ((TextView) card.findViewById(R.id.alertTimestamp)).setText(formatTimestamp(timestamp));
        alertListContainer.addView(card);
    }

    // Returns a formatted timestamp string, or a fallback label if null.
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return getString(R.string.missed_alert_timestamp);
        return new SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault()).format(new Date(timestamp));
    }

    // Shows a message when there are no missed alerts to display.
    private void showEmptyState() {
        TextView empty = new TextView(requireContext());
        empty.setText(R.string.no_missed_alerts);
        empty.setTextSize(16);
        empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        alertListContainer.addView(empty);
    }
}