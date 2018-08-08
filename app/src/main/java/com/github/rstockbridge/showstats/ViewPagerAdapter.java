package com.github.rstockbridge.showstats;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private String userId;

    ViewPagerAdapter(@NonNull final FragmentManager fm, @NonNull final String userId) {
        super(fm);

        this.userId = userId;
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return StatsFragment.newInstance(userId);
            case 1:
                return CompareFragment.newInstance(userId);
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
