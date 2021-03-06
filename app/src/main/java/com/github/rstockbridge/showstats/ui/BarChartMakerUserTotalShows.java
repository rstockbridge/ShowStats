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
import com.github.rstockbridge.showstats.appmodels.UserStatistics;

import java.util.ArrayList;
import java.util.List;

public final class BarChartMakerUserTotalShows {

    @NonNull
    private final List<String> labels = new ArrayList<>();

    @NonNull
    private BarChart barChart;

    private String user1;
    private String user2;

    private int numberOfShows1;
    private int numberOfShows2;

    public BarChartMakerUserTotalShows(@NonNull final BarChart barChart,
                                       @NonNull final UserStatistics user1Statistics,
                                       @NonNull final UserStatistics user2Statistics) {

        this.barChart = barChart;

        this.user1 = user1Statistics.getUserId();
        this.user2 = user2Statistics.getUserId();

        this.numberOfShows1 = user1Statistics.getNumberOfShows();
        this.numberOfShows2 = user2Statistics.getNumberOfShows();

        setLabels();
    }

    private void setLabels() {
        // invert for display purposes
        labels.add(user2);
        labels.add(user1);
    }

    public void displayBarChart(@NonNull final Context context) {
        barChart.setTouchEnabled(false);
        barChart.setFitBars(true);

        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setExtraRightOffset(30f);

        configureXAxis();
        configureYAxis(context);

        // must come after configureXAxis() for chart to fit correctly horizontally
        barChart.setData(getBarData(context));

        barChart.invalidate(); // refresh
    }

    @NonNull
    private BarDataSet getBarDataSet(@NonNull final Context context) {
        final ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, numberOfShows2));
        entries.add(new BarEntry(1, numberOfShows1));

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
        xAxis.setLabelCount(2);
        xAxis.setTextSize(16f);

        xAxis.setValueFormatter((value, axis) -> labels.get((int) value));
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
