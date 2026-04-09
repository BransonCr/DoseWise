package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class RefillFragment extends AppCompatActivity {

    private MedicationViewModel viewModel;
    private LinearLayout listContainer;
    private View emptyStateContainer;
    private View attentionNeededContainer;
    private TextView attentionNeededCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_refill);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);

        // Initialize common views
        listContainer = findViewById(R.id.medicationListContainer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        refreshList();
    }

    public void showAddDialog(View view){
        Button buttonAddMedTop = findViewById(R.id.addMedicationBtnTop);
        if (buttonAddMedTop != null) {
            buttonAddMedTop.setVisibility(View.GONE);
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_refill_tracking, null);

        EditText nameInput = dialogView.findViewById(R.id.medNameInput);
        EditText pillInput = dialogView.findViewById(R.id.pillInput);
        Spinner frequencySpinner = dialogView.findViewById(R.id.DosageFreq);
        DatePicker datePicker = dialogView.findViewById(R.id.date);
        Button saveBtn = dialogView.findViewById(R.id.save_modification);

        AlertDialog dialog = new AlertDialog.Builder(this)
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
        if (viewModel == null) return;
        List<Medication> medications = viewModel.getMedicationList();

        if (listContainer == null) {
            listContainer = findViewById(R.id.medicationListContainer);
        }

        if (listContainer == null) return;

        listContainer.removeAllViews();

        if (medications.isEmpty()) {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            if (attentionNeededContainer != null) attentionNeededContainer.setVisibility(View.GONE);
            return;
        }

        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
        int lowSupplyCount = 0;

        for (Medication med : medications) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_refill_medication, listContainer, false);

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
                // Assume 30 days is 100% for the progress bar
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
                        // Note: If PharmacyLocatorFragment is a Fragment, this will still crash.
                        // It should be launched via FragmentManager or be converted to an Activity.
                        Intent intent = new Intent(this, PharmacyLocatorFragment.class);
                        startActivity(intent);
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