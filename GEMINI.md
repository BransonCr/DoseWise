# DoseWise - Gemini Context

This document provides foundational context and instructions for AI agents working on the DoseWise project. DoseWise is an Android application built in Java that manages medication schedules, dose logging, reminders, caregiver monitoring, and refill tracking.

---

## Project Overview

- **Core Technology:** Android (Java), Gradle (KTS).
- **Architecture:** Single Activity (`MainActivity`) with multiple Fragments navigated via the **Android Navigation Component**.
- **State Management:** All application state is managed in-memory using a shared `MedicationViewModel`. **There is no backend or database.** Data does not persist between app sessions.
- **Key Dependencies:**
  - `androidx.navigation`: For fragment navigation.
  - `com.google.android.gms:play-services-maps`: For pharmacy location services.
  - `com.github.PhilJay:MPAndroidChart`: For the weekly adherence summary chart.

---

## Development Conventions

### Architecture & Data
- **Shared ViewModel:** Every Fragment must access the shared `MedicationViewModel` via `new ViewModelProvider(requireActivity()).get(MedicationViewModel.class)`.
- **Navigation:** All transitions between screens must be defined in `app/src/main/res/navigation/nav_graph.xml`.
- **Models:** `Medication.java` is the primary data model.

### Coding Style
- **Naming:**
  - Fragments: PascalCase with `Fragment` suffix (e.g., `HomeFragment.java`).
  - Layouts: snake_case with `fragment_` prefix (e.g., `fragment_home.xml`).
  - Variables: camelCase (e.g., `medicationName`).
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_DOSE_COUNT`).
- **Encapsulation:** All ViewModel fields are private with public getters and setters.
- **Documentation:** Every method must have a one-line comment above it describing its purpose.
- **Complexity:** No method should exceed 40 lines. Logic should be broken into helper methods.
- **Implementation:** Click listeners should be set in `onViewCreated`, not `onCreateView`.

### UI/UX
- **Root Views:** All screens must use `ConstraintLayout` as the root view.
- **Units:** Use `sp` for text sizes and `dp` for margins/padding.
- **Resources:**
  - No hardcoded strings in layout files; use `res/values/strings.xml`.
  - All colors must be defined in `res/values/colors.xml`.

---

## Building and Running

- **Build:** `./gradlew assembleDebug`
- **Install:** `./gradlew installDebug`
- **Test:** `./gradlew test` (Local unit tests) or `./gradlew connectedAndroidTest` (Instrumented tests)
- **API Keys:** The project uses Google Maps. Ensure `GOOGLE_CLOUD_API_KEY` is configured in `local.properties` or provided via manifest placeholders in `build.gradle.kts`.

---

## Key Files

- `app/src/main/java/com/example/myapplication/MainActivity.java`: The entry point and host for the navigation graph.
- `app/src/main/java/com/example/myapplication/MedicationViewModel.java`: The central state store.
- `app/src/main/java/com/example/myapplication/Medication.java`: The core data model.
- `app/src/main/res/navigation/nav_graph.xml`: Definition of all app destinations and actions.

---

## Git Workflow

- **Branching:** Work on task-specific branches (e.g., `taskX-feature-name`).
- **Commits:** Use present tense in commit messages (e.g., `Add time picker to medication form`).
- **Merging:** Never commit directly to `main`. All changes require a review and a pull request.
