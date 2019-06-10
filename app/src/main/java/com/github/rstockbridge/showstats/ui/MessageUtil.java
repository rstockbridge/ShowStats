package com.github.rstockbridge.showstats.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.Toast;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static void makeToast(@NonNull final Context context, @NonNull final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
