package com.github.rstockbridge.showstats.utility;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public final class ActivityFragmentUtil {

    private ActivityFragmentUtil() {
    }

    public static boolean isActivityValid(@NonNull Fragment fragment, @Nullable Activity activity) {
        return fragment.isAdded()
                && activity != null
                && !activity.isFinishing()
                && !activity.isDestroyed();
    }
}
