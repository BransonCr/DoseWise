package com.example.myapplication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Stores fake caregiver dependent data for UI testing. Remove before release.
public class CaregiverMockData {

    // Sentinel value used to identify the real current user in the dependent list.
    public static final String CURRENT_USER_KEY = "You";

    public static final String[] DEPENDENT_NAMES = {
        CURRENT_USER_KEY,       // index 0 — reads from real ViewModel data
        "Margaret Thompson",    // index 1
        "Robert Chen",          // index 2
        "Dorothy Williams"      // index 3
    };

    // Holds all display data for a single mock medication.
    public static class MedRecord {
        public final String name, dosage;
        public final List<String> times;
        public final DoseStatus status;
        public final long missedAt; // epoch ms; 0 if status is not MISSED

        MedRecord(String name, String dosage, List<String> times, DoseStatus status, long missedAt) {
            this.name = name;
            this.dosage = dosage;
            this.times = times;
            this.status = status;
            this.missedAt = missedAt;
        }
    }

    // Maps each dependent name to their list of medication records.
    public static final Map<String, List<MedRecord>> MOCK_MEDS = new HashMap<>();

    // Per-dependent weekly taken/missed counts indexed Mon=0 ... Sun=6.
    public static final Map<String, int[]> WEEKLY_TAKEN = new HashMap<>();
    public static final Map<String, int[]> WEEKLY_MISSED = new HashMap<>();

    // Returns true if the given name refers to the real current user.
    public static boolean isCurrentUser(String name) {
        return CURRENT_USER_KEY.equals(name);
    }

    // Returns the medication records for the given owner, never null.
    public static List<MedRecord> medsFor(String owner) {
        List<MedRecord> result = MOCK_MEDS.get(owner);
        return result != null ? result : new java.util.ArrayList<>();
    }

    // Returns the weekly taken counts for the given owner, never null.
    public static int[] weeklyTakenFor(String owner) {
        int[] result = WEEKLY_TAKEN.get(owner);
        return result != null ? result : new int[7];
    }

    // Returns the weekly missed counts for the given owner, never null.
    public static int[] weeklyMissedFor(String owner) {
        int[] result = WEEKLY_MISSED.get(owner);
        return result != null ? result : new int[7];
    }

    // Seeds the dependent name into the ViewModel and populates static mock maps.
    public static void seed(MedicationViewModel viewModel) {
        if (!viewModel.getCaregiverDependentName().isEmpty()) return;
        viewModel.setCaregiverDependentName(DEPENDENT_NAMES[0]);

        long now = System.currentTimeMillis();

        // --- Margaret Thompson ---
        MOCK_MEDS.put(DEPENDENT_NAMES[1], Arrays.asList(
            new MedRecord("Lisinopril",   "10mg",   Arrays.asList("8:00 AM"),            DoseStatus.TAKEN,  0),
            new MedRecord("Metformin",    "500mg",  Arrays.asList("8:00 AM", "6:00 PM"), DoseStatus.MISSED, now - TimeUnit.HOURS.toMillis(3)),
            new MedRecord("Vitamin D",    "1000IU", Arrays.asList("8:00 AM"),            DoseStatus.TAKEN,  0),
            new MedRecord("Alendronate",  "70mg",   Arrays.asList("7:00 AM"),            DoseStatus.TAKEN,  0)
        ));

        // --- Robert Chen ---
        MOCK_MEDS.put(DEPENDENT_NAMES[2], Arrays.asList(
            new MedRecord("Atorvastatin", "20mg",   Arrays.asList("9:00 PM"),            DoseStatus.TAKEN,  0),
            new MedRecord("Amlodipine",   "5mg",    Arrays.asList("8:00 AM"),            DoseStatus.MISSED, now - TimeUnit.HOURS.toMillis(5)),
            new MedRecord("Omeprazole",   "20mg",   Arrays.asList("7:00 AM"),            DoseStatus.MISSED, now - TimeUnit.HOURS.toMillis(2))
        ));

        // --- Dorothy Williams ---
        MOCK_MEDS.put(DEPENDENT_NAMES[3], Arrays.asList(
            new MedRecord("Aspirin",       "81mg",  Arrays.asList("8:00 AM"),            DoseStatus.MISSED, now - TimeUnit.HOURS.toMillis(1)),
            new MedRecord("Levothyroxine", "50mcg", Arrays.asList("7:00 AM"),            DoseStatus.MISSED, now - TimeUnit.HOURS.toMillis(4)),
            new MedRecord("Furosemide",    "40mg",  Arrays.asList("8:00 AM", "2:00 PM"), DoseStatus.TAKEN,  0)
        ));

        // --- You (current user) — seeds the shared ViewModel weekly arrays ---
        int[] youTaken  = {2, 2, 1, 2, 2, 1, 2};
        int[] youMissed = {0, 0, 1, 0, 0, 1, 0};
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < youTaken[i]; j++) viewModel.incrementWeeklyTaken(i);
            for (int j = 0; j < youMissed[i]; j++) viewModel.incrementWeeklyMissed(i);
        }

        //                                      Mon  Tue  Wed  Thu  Fri  Sat  Sun
        WEEKLY_TAKEN.put(DEPENDENT_NAMES[1],  new int[]{3, 4, 2, 4, 3, 2, 2});
        WEEKLY_MISSED.put(DEPENDENT_NAMES[1], new int[]{0, 0, 1, 0, 1, 1, 0});
        WEEKLY_TAKEN.put(DEPENDENT_NAMES[2],  new int[]{1, 2, 1, 3, 2, 1, 2});
        WEEKLY_MISSED.put(DEPENDENT_NAMES[2], new int[]{2, 1, 2, 0, 1, 2, 1});
        WEEKLY_TAKEN.put(DEPENDENT_NAMES[3],  new int[]{0, 1, 0, 1, 0, 0, 1});
        WEEKLY_MISSED.put(DEPENDENT_NAMES[3], new int[]{3, 2, 3, 2, 3, 3, 2});
    }
}
