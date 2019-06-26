package com.github.rstockbridge.showstats.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.rstockbridge.showstats.R;

import java.util.ArrayList;
import java.util.List;

public final class BarChartMakerShowDistribution {

    private static final int NUMBER_OF_MONTHS = 12;

    @NonNull
    private static List<String> getLabels() {
        final List<String> result = new ArrayList<>();

        // invert for display purposes
        result.add("Dec");
        result.add("Nov");
        result.add("Oct");
        result.add("Sep");
        result.add("Aug");
        result.add("Jul");
        result.add("Jun");
        result.add("May");
        result.add("Apr");
        result.add("Mar");
        result.add("Feb");
        result.add("Jan");

        return result;
    }

    @NonNull
    private BarChart barChart;

    @NonNull
    private int[] dataByMonth;

    public BarChartMakerShowDistribution(@NonNull final BarChart barChart, @NonNull final int[] dataByMonth) {
        this.barChart = barChart;
        this.dataByMonth = dataByMonth;
    }

    public void displayBarChart(@NonNull final Context context) {
        barChart.setData(getBarData(context));
        barChart.setTouchEnabled(false);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        configureXAxis();
        configureYAxis(context);

        barChart.invalidate(); // refresh
    }

    @NonNull
    private BarDataSet getBarDataSet(@NonNull final Context context) {
        final ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_MONTHS; i++) {
            entries.add(new BarEntry(i, dataByMonth[i]));
        }

        //return new BarDataSet(entries, "");

        final BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(context, R.color.colorAccent));

        return dataSet;
    }

    @NonNull
    private BarData getBarData(@NonNull final Context context) {
        final BarData result = new BarData(getBarDataSet(context));
        result.setBarWidth(0.9f);
        result.setValueTextSize(16f);

        result.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.valueOf((int) value));

        return result;
    }

    private void configureXAxis() {
        final XAxis xAxis = barChart.getXAxis();

        xAxis.setDrawAxisLine(false); // no axis line
        xAxis.setDrawGridLines(false); // no grid lines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(NUMBER_OF_MONTHS);
        xAxis.setTextSize(16f);

        xAxis.setValueFormatter((value, axis) -> getLabels().get((int) value));
    }

    private void configureYAxis(@NonNull final Context context) {
        final YAxis leftYAxis = barChart.getAxisLeft();
        final YAxis rightYAxis = barChart.getAxisRight();

        leftYAxis.setAxisMinimum(0);
        leftYAxis.setDrawLabels(false);
        leftYAxis.setDrawAxisLine(false);
        leftYAxis.setDrawGridLines(false);

        rightYAxis.setAxisMaximum(getBarData(context).getYMax());
        rightYAxis.setEnabled(false);
    }
}
