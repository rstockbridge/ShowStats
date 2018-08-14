package com.github.rstockbridge.showstats.utility;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public final class PieChartMaker {

    @NonNull
    private PieChart pieChart;

    @NonNull
    private List<Pair<Integer, String>> data;

    public PieChartMaker(@NonNull final PieChart pieChart, @NonNull final List<Pair<Integer, String>> data) {
        this.pieChart = pieChart;
        this.data = data;
    }

    public void displayPieChart() {
        pieChart.setData(getPieData());
        pieChart.setTouchEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setExtraOffsets(0, 0, 0, -10); // shift legend up towards chart

        configureLegend();

        pieChart.invalidate(); // refresh
    }

    @NonNull
    private PieDataSet getPieDataSet() {
        final List<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            // list is guaranteed to not have null entries by method of construction
            entries.add(new PieEntry(data.get(i).first, data.get(i).second));
        }

        final PieDataSet result = new PieDataSet(entries, "");
        result.setColors(ColorTemplate.COLORFUL_COLORS);

        return result;
    }

    @NonNull
    private PieData getPieData() {
        final PieData result = new PieData(getPieDataSet());
        result.setValueFormatter(new IntegerValueFormatter());
        result.setValueTextSize(16f);
        return result;
    }

    private void configureLegend() {
        final Legend legend = pieChart.getLegend();

        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(16f);
        legend.setWordWrapEnabled(true);
    }
}
