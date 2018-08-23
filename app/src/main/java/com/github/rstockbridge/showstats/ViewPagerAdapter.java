package com.github.rstockbridge.showstats;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;

import java.util.ArrayList;

public final class ViewPagerAdapter extends FragmentPagerAdapter {

    @NonNull
    private UserStatistics statistics;

    ViewPagerAdapter(
            @NonNull final FragmentManager fm,
            @NonNull final UserStatistics statistics) {
        super(fm);

        this.statistics = statistics;
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return StatsFragment.newInstance(statistics);
            case 1:
                return CompareFragment.newInstance(statistics);
        }

        throw new IllegalStateException("This line should not be reached");
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return "Statistics";
            case 1:
                return "Compare";
        }

        throw new IllegalStateException("This line should not be reached.");
    }
}
