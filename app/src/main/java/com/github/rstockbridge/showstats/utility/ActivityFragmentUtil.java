package com.github.rstockbridge.showstats.utility;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public final class ActivityFragmentUtil {

    public static boolean isActivityValid(@NonNull Fragment fragment, @Nullable Activity activity) {
        return fragment.isAdded()
                && activity != null
                && !activity.isFinishing()
                && !activity.isDestroyed();
    }
}
