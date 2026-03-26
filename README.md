# DoseWise - Android Development Guide

**Team Members:** Branson, Daniel, Aaron, Ben, Talon

---

## Project Overview

DoseWise is an Android application built in Java using Android Studio. The app manages medication schedules, dose logging, reminders, caregiver monitoring, and refill tracking. There is no backend or database. All state is managed in memory using ViewModels shared across fragments.

---

## Shared Architecture

The app uses a single Activity with multiple Fragments navigated through the Android Navigation Component. All data is stored in a shared ViewModel that every fragment accesses. No data persists between app sessions.

---

## Conventions

### Naming
- All fragment files are named in PascalCase with the Fragment suffix. Example: `HomeFragment.java`
- All layout files are named in snake_case with the fragment prefix. Example: `fragment_home.xml`
- All ViewModel fields are private with public getters and setters
- All variable names are camelCase. Example: `medicationName`
- All constants are UPPER_SNAKE_CASE. Example: `MAX_DOSE_COUNT`

### Layout
- All screens use `ConstraintLayout` as the root view
- All text sizes use `sp` units
- All margins and padding use `dp` units
- No hardcoded strings in layout files. All strings go in `res/values/strings.xml`
- All colors go in `res/values/colors.xml`

### Code
- Every method has a one line comment above it describing what it does
- No method exceeds 40 lines. Break logic into helper methods if needed
- All click listeners are set in `onViewCreated`, not `onCreateView`
- No business logic sits inside XML layout files

### Git
- Each person works on their own branch named after their task. Example: `task1-medication-setup`
- Commit messages are written in present tense. Example: `Add time picker to medication form`
- No one merges their own pull request. One other team member must review it first
- Never commit directly to `main`

---

## Responsibilities

### Branson - Task 1: Medication Schedule Setup

**Owns the following files:**
- `AddMedicationFragment.java` and `fragment_add_medication.xml`
- `ReviewMedicationFragment.java` and `fragment_review_medication.xml`
- `MedicationConfirmationFragment.java` and `fragment_medication_confirmation.xml`
- `Medication.java` (the shared data model used by all tasks)

**Responsibilities:**
- Build the Add Medication form with fields for name, dosage, and scheduled times
- Implement a time picker for selecting dose times, supporting multiple times per day
- Build the Review screen summarizing the entry before the user confirms
- Build the Confirmation screen shown after saving
- Define `Medication.java` model class since every other task depends on it
- Add the initial medication list to the shared ViewModel after confirmation

> **Note:** Branson completes `Medication.java` and ViewModel setup first so other tasks can begin building against it.

---

### Aaron - Task 2: Dose Logging

**Owns the following files:**
- `HomeFragment.java` and `fragment_home.xml`
- `DoseConfirmationFragment.java` and `fragment_dose_confirmation.xml`
- `MissedDoseFragment.java` and `fragment_missed_dose.xml`

**Responsibilities:**
- Build the Home screen displaying the daily medication list
- Implement taken, missed, and upcoming status indicators using both icons and color
- Implement the single-tap log action that opens a confirmation prompt
- Update dose status in the shared ViewModel after confirmation
- Build the streak counter on the Home screen that increments after each successful log
- Build the Missed Dose screen showing status and available next actions

---

### Ben - Task 3: Reminder Configuration

**Owns the following files:**
- `ReminderTimingFragment.java` and `fragment_reminder_timing.xml`
- `ReminderToneFragment.java` and `fragment_reminder_tone.xml`
- `ReminderConfirmationFragment.java` and `fragment_reminder_confirmation.xml`

**Responsibilities:**
- Build the three step reminder flow: timing, tone and message, confirmation
- Implement the tone selector showing Caring and Supportive vs Direct and Efficient with a preview message for each
- Display the Reminder All Set confirmation screen after saving
- Implement push notifications using Android's `AlarmManager` so reminders fire when the app is closed
- Wire the selected tone to the notification message content

---

### Talon - Task 4: Caregiver Monitoring

**Owns the following files:**
- `CaregiverDashboardFragment.java` and `fragment_caregiver_dashboard.xml`
- `MissedAlertListFragment.java` and `fragment_missed_alert_list.xml`
- `WeeklySummaryFragment.java` and `fragment_weekly_summary.xml`

**Responsibilities:**
- Build the caregiver dashboard showing a linked dependent's name and today's adherence status
- Build the missed dose alert list with timestamps
- Build the weekly summary screen showing taken vs missed doses over 7 days using a simple bar chart via MPAndroidChart
- Handle caregiver account switching logic in the shared ViewModel

---

### Daniel - Task 5: Refill Tracking

**Owns the following files:**
- `RefillFragment.java` and `fragment_refill.xml`
- `PharmacyLocatorFragment.java` and `fragment_pharmacy_locator.xml`

**Responsibilities:**
- Build the supply counter logic that calculates remaining doses based on frequency and start date using the Medication model
- Build the low supply warning banner that appears on the Home screen when 7 days of supply remain
- Build the Refill screen showing medication details and a Go to Pharmacy button
- Integrate Google Maps SDK to display nearby pharmacies with name and distance on an in-app map

---

## Shared ViewModel

`MedicationViewModel.java` is owned collectively. Any change to this file requires a message in the group chat before editing. It holds:

- `List<Medication>` - list of all medications
- `Map<String, DoseStatus>` - dose status keyed by medication name
- `int streakCount` - current streak integer
- `String caregiverDependentName` - selected caregiver dependent name
- `Map<String, Boolean>` - refill alert flag per medication

---

## Navigation

All navigation between fragments is defined in `res/navigation/nav_graph.xml`. Branson sets up the initial navigation graph with placeholder destinations. Each person adds their own fragment destinations and actions to the graph when their screens are ready.
