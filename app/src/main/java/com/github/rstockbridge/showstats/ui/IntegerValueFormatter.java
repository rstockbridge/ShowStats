package com.github.rstockbridge.showstats.ui;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public final class IntegerValueFormatter implements IValueFormatter {

    @NonNull
    private final DecimalFormat format;

    IntegerValueFormatter() {
        format = new DecimalFormat("###,###,###"); // use one decimal
    }

    @Override
    @NonNull
    public String getFormattedValue(
            final float value,
            final Entry entry,
            final int dataSetIndex,
            final ViewPortHandler viewPortHandler) {

        return format.format(value);
    }
}
