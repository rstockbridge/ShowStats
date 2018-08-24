package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class User1StatisticsHolder {

    @Nullable
    private static User1StatisticsHolder user1StatisticsHolder;

    private UserStatistics statistics;

    @NonNull
    public static User1StatisticsHolder getSharedInstance() {
        if (user1StatisticsHolder == null) {
            user1StatisticsHolder = new User1StatisticsHolder();
        }

        return user1StatisticsHolder;
    }

    public void initialize(@NonNull final UserStatistics user1Statistics) {
        this.statistics = user1Statistics;
    }

    @NonNull
    public UserStatistics getStatistics() {
        return statistics;
    }

}
