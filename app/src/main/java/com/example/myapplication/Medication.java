package com.example.myapplication;

import java.util.List;

public class Medication {
    private final String name;
    private final String dosage;
    private final List<String> scheduledTimes;

    public Medication(String name, String dosage, List<String> scheduledTimes) {
        this.name = name;
        this.dosage = dosage;
        this.scheduledTimes = scheduledTimes;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public List<String> getScheduledTimes() {
        return scheduledTimes;
    }
}