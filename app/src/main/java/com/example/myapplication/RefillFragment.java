package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

public class RefillFragment extends Fragment {

    private MedicationViewModel viewModel;
    private LinearLayout listContainer;
    private View emptyStateContainer;
    private View attentionNeededContainer;
    private TextView attentionNeededCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_refill, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel (Shared with Activity as per conventions)
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        // Initialize common views
        listContainer = view.findViewById(R.id.medicationListContainer);
        attentionNeededContainer = view.findViewById(R.id.attentionNeededContainer);
        attentionNeededCount = view.findViewById(R.id.attentionNeededCount);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        // Set up "Add Medication" button (the one in the layout)
        Button addBtnTop = view.findViewById(R.id.addMedicationBtnTop);
        if (addBtnTop != null) {
            addBtnTop.setOnClickListener(this::showAddDialog);
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        refreshList();
    }

    public void showAddDialog(View view){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_refill_tracking, null);

        EditText nameInput = dialogView.findViewById(R.id.medNameInput);
        EditText pillInput = dialogView.findViewById(R.id.pillInput);
        Spinner frequencySpinner = dialogView.findViewById(R.id.DosageFreq);
        DatePicker datePicker = dialogView.findViewById(R.id.date);
        Button saveBtn = dialogView.findViewById(R.id.save_modification);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String pillsStr = pillInput.getText().toString().trim();

            if (!name.isEmpty() && !pillsStr.isEmpty()) {
                int totalPills = Integer.parseInt(pillsStr);
                int frequency = frequencySpinner.getSelectedItemPosition() + 1;
                String startDate = (datePicker.getMonth() + 1) + "/" + datePicker.getDayOfMonth() + "/" + datePicker.getYear();

                String encodedDosage = "10mg|" + totalPills + "|" + frequency + "|" + startDate;
                viewModel.addMedication(new Medication(name, encodedDosage, new ArrayList<>()));

                refreshList();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void refreshList() {
        if (viewModel == null || listContainer == null) return;
        List<Medication> medications = viewModel.getMedicationList();

        listContainer.removeAllViews();

        if (medications.isEmpty()) {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            if (attentionNeededContainer != null) attentionNeededContainer.setVisibility(View.GONE);
            return;
        }

        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
        int lowSupplyCount = 0;

        for (Medication med : medications) {
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_refill_medication, listContainer, false);

            String[] data = med.getDosage().split("\\|");
            if (data.length < 4) {
                ((TextView) itemView.findViewById(R.id.medNameText)).setText(med.getName());
                listContainer.addView(itemView);
                continue;
            }

            String actualDosage = data[0];
            int currentPills = Integer.parseInt(data[1]);
            int frequency = Integer.parseInt(data[2]);
            String startDate = data[3];

            int daysRemaining = frequency > 0 ? currentPills / frequency : 0;
            boolean isLow = daysRemaining <= 7;

            ((TextView) itemView.findViewById(R.id.medNameText)).setText(med.getName());
            ((TextView) itemView.findViewById(R.id.pillDaysText)).setText(currentPills + " pills left • " + daysRemaining + " days left");
            ((TextView) itemView.findViewById(R.id.dosageText)).setText(actualDosage);
            ((TextView) itemView.findViewById(R.id.startDateText)).setText(startDate);

            ProgressBar pb = itemView.findViewById(R.id.supplyProgressBar);
            if (pb != null) {
                pb.setProgress(Math.min(100, (daysRemaining * 100) / 30));
            }

            if (isLow) {
                lowSupplyCount++;
                View warning = itemView.findViewById(R.id.lowSupplyWarning);
                if (warning != null) warning.setVisibility(View.VISIBLE);

                TextView lowSupplyDetails = itemView.findViewById(R.id.lowSupplyDetails);
                if (lowSupplyDetails != null) {
                    lowSupplyDetails.setVisibility(View.VISIBLE);
                    lowSupplyDetails.setText("Low supply! Only " + daysRemaining + " days remaining");
                }

                Button findBtn = itemView.findViewById(R.id.findPharmacyBtn);
                if (findBtn != null) {
                    findBtn.setVisibility(View.VISIBLE);
                    findBtn.setOnClickListener(v -> {
                        Navigation.findNavController(v).navigate(R.id.action_refill_to_pharmacyLocator);
                    });
                }
            }

            listContainer.addView(itemView);
        }

        if (lowSupplyCount > 0) {
            if (attentionNeededContainer != null) attentionNeededContainer.setVisibility(View.VISIBLE);
            if (attentionNeededCount != null) {
                attentionNeededCount.setText(lowSupplyCount + (lowSupplyCount == 1 ? " medication has low supply" : " medications have low supply"));
            }
        } else {
            if (attentionNeededContainer != null) attentionNeededContainer.setVisibility(View.GONE);
        }
    }
}