package com.github.rstockbridge.showstats.utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static void makeToast(@NonNull final Context context, @NonNull final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
