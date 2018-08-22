package com.github.rstockbridge.showstats.utility;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class PieChartMaker {

    @NonNull
    private PieChart pieChart;

    @NonNull
    private Map<String, Integer> data;

    public PieChartMaker(@NonNull final PieChart pieChart, @NonNull final Map<String, Integer> data) {
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

        int i = 0;
        final Iterator<Map.Entry<String, Integer>> iterator = data.entrySet().iterator();
        while (i < data.size()) {
            // list is guaranteed to not have null entries by method of construction
            final Map.Entry<String, Integer> pair = iterator.next();
            entries.add(new PieEntry(pair.getValue(), pair.getKey()));
            i++;
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
