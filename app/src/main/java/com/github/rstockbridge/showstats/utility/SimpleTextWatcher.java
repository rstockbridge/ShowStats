package com.github.rstockbridge.showstats.utility;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        // this method intentionally left blank
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        // this method intentionally left blank
    }

    @Override
    public void afterTextChanged(final Editable s) {
        // this method intentionally left blank
    }
}
