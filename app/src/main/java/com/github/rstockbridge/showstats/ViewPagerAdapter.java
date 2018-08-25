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

    ViewPagerAdapter(@NonNull final FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return new StatsFragment();
            case 1:
                return new ShowsFragment();
            case 2:
                return new CompareFragment();
        }

        throw new IllegalStateException("This line should not be reached");
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return "Statistics";
            case 1:
                return "Shows";
            case 2:
                return "Compare";
        }

        throw new IllegalStateException("This line should not be reached.");
    }
}
