package com.github.rstockbridge.showstats.screens.tabbed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
                return new MapFragment();
            case 2:
                return new ShowsFragment();
            case 3:
                return new CompareFragment();
            default:
                throw new IllegalStateException("This line should not be reached");
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return "Statistics";
            case 1:
                return "Map";
            case 2:
                return "Shows";
            case 3:
                return "Compare";
            default:
                throw new IllegalStateException("This line should not be reached");
        }
    }
}
