package com.github.rstockbridge.showstats.utility;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public final class IntegerValueFormatter implements IValueFormatter {

    @NonNull
    private DecimalFormat format;

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