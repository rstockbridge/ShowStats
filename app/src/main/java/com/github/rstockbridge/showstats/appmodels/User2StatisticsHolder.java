package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class User2StatisticsHolder {

    @Nullable
    private static User2StatisticsHolder holder;

    @Nullable
    private UserStatistics statistics;

    @NonNull
    public static User2StatisticsHolder getSharedInstance() {
        if (holder == null) {
            holder = new User2StatisticsHolder();
        }

        return holder;
    }

    private User2StatisticsHolder() {
    }

    public void setStatistics(@NonNull final UserStatistics user2Statistics) {
        this.statistics = user2Statistics;
    }

    @Nullable
    public UserStatistics getStatistics() {
        return statistics;
    }
}
