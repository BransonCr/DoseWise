package com.example.myapplication;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicationViewModel extends ViewModel {
    private final List<Medication> medicationList = new ArrayList<>();
    private final Map<String, DoseStatus> doseStatusMap = new HashMap<>();
    private int streakCount = 0;
    private String caregiverDependentName = "";
    private final Map<String, Boolean> refillAlertFlags = new HashMap<>();

    public List<Medication> getMedicationList() {
        return medicationList;
    }

    public void addMedication(Medication medication) {
        medicationList.add(medication);
    }

    public Map<String, DoseStatus> getDoseStatusMap() {
        return doseStatusMap;
    }

    public void updateDoseStatus(String medicationName, DoseStatus status) {
        doseStatusMap.put(medicationName, status);
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void incrementStreak() {
        streakCount++;
    }

    public String getCaregiverDependentName() {
        return caregiverDependentName;
    }

    public void setCaregiverDependentName(String name) {
        caregiverDependentName = name;
    }

    public Map<String, Boolean> getRefillAlertFlags() {
        return refillAlertFlags;
    }

    public void updateRefillFlag(String medicationName, boolean isLow) {
        refillAlertFlags.put(medicationName, isLow);
    }

    private Medication pendingMedication = null;

    public Medication getPendingMedication() {
        return pendingMedication;
    }

    public void setPendingMedication(Medication medication) {
        pendingMedication = medication;
    }

    public void clearPendingMedication() {
        pendingMedication = null;
    }
}