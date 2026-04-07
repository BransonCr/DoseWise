package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Displays caregiver monitoring dashboard with alerts and weekly summary tabs.
public class CaregiverDashboardFragment extends Fragment {
    private MedicationViewModel viewModel;
    private View alertsContent;
    private View weeklyContent;
    private LinearLayout missedListContainer;
    private MaterialButton alertsTab;
    private MaterialButton weeklyTab;
    private TextView initialsText;
    private TextView nameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_caregiver_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);

        alertsContent = view.findViewById(R.id.alertsContent);
        weeklyContent = view.findViewById(R.id.weeklyContent);
        missedListContainer = view.findViewById(R.id.missedListContainer);
        alertsTab = view.findViewById(R.id.alertsTab);
        weeklyTab = view.findViewById(R.id.weeklyTab);
        initialsText = view.findViewById(R.id.dependentInitials);
        nameText = view.findViewById(R.id.dependentNameText);

        bindDependentInfo(initialsText, nameText);
        bindAdherenceStats(view.findViewById(R.id.adherencePercentageText),
                view.findViewById(R.id.takenCountText), view.findViewById(R.id.missedCountText));
        renderMissedDoses();
        bindWeeklySummaryStats(view);
        buildAdherenceTrendChart(view.findViewById(R.id.adherenceTrendChart));
        buildDailyBreakdownChart(view.findViewById(R.id.dailyBreakdownChart));

        alertsTab.setOnClickListener(v -> showAlertsTab());
        weeklyTab.setOnClickListener(v -> showWeeklyTab());
        view.findViewById(R.id.chevronText).setOnClickListener(v -> showSwitchDependentDialog());
        view.findViewById(R.id.button).setOnClickListener(v -> navigateToHome());
    }

    private void navigateToHome() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_caregiverDashboard_to_home);
    }

    private void showSwitchDependentDialog() {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.caregiver_switch_dependent_hint);
        input.setText(viewModel.getCaregiverDependentName());
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.caregiver_switch_dependent_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    viewModel.setCaregiverDependentName(input.getText().toString().trim());
                    bindDependentInfo(initialsText, nameText);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void bindDependentInfo(TextView initialsText, TextView nameText) {
        String name = viewModel.getCaregiverDependentName();
        if (name == null || name.isEmpty()) {
            initialsText.setText("?");
            nameText.setText(R.string.caregiver_no_dependent);
            return;
        }
        String[] parts = name.trim().split("\\s+");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)
                : String.valueOf(parts[0].charAt(0));
        initialsText.setText(initials.toUpperCase());
        nameText.setText(name);
    }

    private void bindAdherenceStats(TextView percentageText, TextView takenText, TextView missedText) {
        int total = viewModel.getMedicationList().size();
        long taken = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.TAKEN).count();
        long missed = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.MISSED).count();
        int percentage = total > 0 ? (int) (taken * 100 / total) : 0;
        percentageText.setText(getString(R.string.caregiver_adherence_percentage, percentage));
        takenText.setText(getString(R.string.caregiver_taken_label, taken));
        missedText.setText(getString(R.string.caregiver_missed_label, missed));
    }

    private void renderMissedDoses() {
        missedListContainer.removeAllViews();
        boolean hasMissed = false;
        for (Map.Entry<String, DoseStatus> entry : viewModel.getDoseStatusMap().entrySet()) {
            if (entry.getValue() == DoseStatus.MISSED) {
                addMissedCard(entry.getKey());
                hasMissed = true;
            }
        }
        if (!hasMissed) showEmptyState();
    }

    private void addMissedCard(String medName) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_missed_dose, missedListContainer, false);
        ((TextView) card.findViewById(R.id.missedMedName)).setText(medName);
        ((TextView) card.findViewById(R.id.missedActionHint)).setText(R.string.missed_action_hint);
        missedListContainer.addView(card);
    }

    private void showEmptyState() {
        TextView empty = new TextView(requireContext());
        empty.setText(R.string.no_missed_doses);
        empty.setTextSize(16);
        empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        missedListContainer.addView(empty);
    }

    private void bindWeeklySummaryStats(View view) {
        long taken = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.TAKEN).count();
        long missed = viewModel.getDoseStatusMap().values().stream()
                .filter(s -> s == DoseStatus.MISSED).count();
        long total = taken + missed;
        int rate = total > 0 ? (int) (taken * 100 / total) : 0;
        ((TextView) view.findViewById(R.id.weeklyTakenCount)).setText(String.valueOf(taken));
        ((TextView) view.findViewById(R.id.weeklyMissedCount)).setText(String.valueOf(missed));
        ((TextView) view.findViewById(R.id.weeklyAdherenceRate))
                .setText(getString(R.string.caregiver_adherence_percentage, rate));
    }

    // Builds the Adherence Trend line chart showing daily adherence % for the past 7 days.
    private void buildAdherenceTrendChart(LineChart chart) {
        int[] taken = viewModel.getWeeklyTakenCounts();
        int[] missed = viewModel.getWeeklyMissedCounts();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int total = taken[i] + missed[i];
            float pct = total > 0 ? (taken[i] * 100f / total) : 0f;
            entries.add(new Entry(i, pct));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.caregiver_adherence_rate_label));
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(6.5f);

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    // Builds the Daily Breakdown grouped bar chart showing taken vs missed per day.
    private void buildDailyBreakdownChart(BarChart chart) {
        int[] taken = viewModel.getWeeklyTakenCounts();
        int[] missed = viewModel.getWeeklyMissedCounts();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        List<BarEntry> takenEntries = new ArrayList<>();
        List<BarEntry> missedEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            takenEntries.add(new BarEntry(i, taken[i]));
            missedEntries.add(new BarEntry(i, missed[i]));
        }

        BarDataSet takenSet = new BarDataSet(takenEntries, getString(R.string.weekly_legend_taken));
        takenSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        takenSet.setValueTextSize(10f);

        BarDataSet missedSet = new BarDataSet(missedEntries, getString(R.string.weekly_legend_missed));
        missedSet.setColor(ContextCompat.getColor(requireContext(), R.color.accent));
        missedSet.setValueTextSize(10f);

        ValueFormatter hideZero = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return barEntry.getY() == 0f ? "" : String.valueOf((int) barEntry.getY());
            }
        };
        takenSet.setValueFormatter(hideZero);
        missedSet.setValueFormatter(hideZero);

        float groupSpace = 0.1f;
        float barSpace = 0.05f;
        float barWidth = 0.35f;

        BarData data = new BarData(takenSet, missedSet);
        data.setBarWidth(barWidth);

        int maxVal = 0;
        for (int i = 0; i < 7; i++) {
            if (taken[i] > maxVal) maxVal = taken[i];
            if (missed[i] > maxVal) maxVal = missed[i];
        }
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(Math.max(5, maxVal + 1));
        chart.getAxisLeft().setGranularity(1f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * 7);

        chart.setData(data);
        chart.groupBars(0f, groupSpace, barSpace);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setFitBars(true);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.invalidate();
    }

    private void showAlertsTab() {
        alertsContent.setVisibility(View.VISIBLE);
        weeklyContent.setVisibility(View.GONE);
        alertsTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
        alertsTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        weeklyTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.screen_background));
        weeklyTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }

    private void showWeeklyTab() {
        alertsContent.setVisibility(View.GONE);
        weeklyContent.setVisibility(View.VISIBLE);
        weeklyTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
        weeklyTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        alertsTab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.screen_background));
        alertsTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }
}
