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

    private final Map<String, Long> missedDoseTimestamps = new HashMap<>();
    private final int[] weeklyTakenCounts = new int[7];
    private final int[] weeklyMissedCounts = new int[7];

    public void recordMissedTimestamp(String medicationName) {
        missedDoseTimestamps.put(medicationName, System.currentTimeMillis());
    }

    public Long getMissedTimestamp(String medicationName) {
        return missedDoseTimestamps.get(medicationName);
    }

    public int[] getWeeklyTakenCounts() {
        return weeklyTakenCounts;
    }

    public int[] getWeeklyMissedCounts() {
        return weeklyMissedCounts;
    }

    public void incrementWeeklyTaken(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < 7) weeklyTakenCounts[dayIndex]++;
    }

    public void incrementWeeklyMissed(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < 7) weeklyMissedCounts[dayIndex]++;
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