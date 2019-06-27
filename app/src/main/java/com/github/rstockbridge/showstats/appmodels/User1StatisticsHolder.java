package com.github.rstockbridge.showstats.appmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class User1StatisticsHolder {

    @Nullable
    private static User1StatisticsHolder holder;

    @Nullable
    private UserStatistics statistics;

    @NonNull
    public static User1StatisticsHolder getSharedInstance() {
        if (holder == null) {
            holder = new User1StatisticsHolder();
        }

        return holder;
    }

    private User1StatisticsHolder() {
    }

    public void setStatistics(@NonNull final UserStatistics user1Statistics) {
        this.statistics = user1Statistics;
    }

    @Nullable
    public UserStatistics getStatistics() {
        return statistics;
    }

}
