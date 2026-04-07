package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

// Shows a grouped bar chart of taken vs missed doses over the past 7 days.
public class WeeklySummaryFragment extends Fragment {
    private MedicationViewModel viewModel;

    // Inflates the weekly summary layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weekly_summary, container, false);
    }

    // Wires the ViewModel, builds the chart, and sets up the back button.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);
        buildBarChart(view.findViewById(R.id.weeklyBarChart));
        view.findViewById(R.id.weeklySummaryBackButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    // Builds and renders the grouped bar chart for the current dependent's weekly data.
    private void buildBarChart(BarChart chart) {
        String owner = viewModel.getCaregiverDependentName();
        int[] taken  = CaregiverMockData.isCurrentUser(owner)
                ? viewModel.getWeeklyTakenCounts()
                : CaregiverMockData.weeklyTakenFor(owner);
        int[] missed = CaregiverMockData.isCurrentUser(owner)
                ? viewModel.getWeeklyMissedCounts()
                : CaregiverMockData.weeklyMissedFor(owner);

        List<BarEntry> takenEntries = new ArrayList<>();
        List<BarEntry> missedEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            takenEntries.add(new BarEntry(i, taken[i]));
            missedEntries.add(new BarEntry(i, missed[i]));
        }

        BarDataSet takenSet = buildDataSet(takenEntries,
                getString(R.string.weekly_legend_taken), R.color.primary);
        BarDataSet missedSet = buildDataSet(missedEntries,
                getString(R.string.weekly_legend_missed), R.color.accent);

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

        chart.setData(data);
        chart.groupBars(0f, groupSpace, barSpace);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setFitBars(true);

        configureXAxis(chart, groupSpace, barSpace, data);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    // Creates a styled BarDataSet with the given entries, label, and color resource.
    private BarDataSet buildDataSet(List<BarEntry> entries, String label, int colorRes) {
        BarDataSet set = new BarDataSet(entries, label);
        set.setColor(ContextCompat.getColor(requireContext(), colorRes));
        set.setValueTextSize(10f);
        return set;
    }

    // Configures the X axis with day labels centered under each bar group.
    private void configureXAxis(BarChart chart, float groupSpace, float barSpace, BarData data) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * 7);
    }
}
