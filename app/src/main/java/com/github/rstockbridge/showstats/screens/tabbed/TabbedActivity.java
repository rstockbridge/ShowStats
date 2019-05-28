package com.github.rstockbridge.showstats.screens.tabbed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.github.rstockbridge.showstats.BaseAccountActivity;
import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.menu.MenuHelper;

public final class TabbedActivity extends BaseAccountActivity {

    private MenuHelper menuHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        menuHelper = new MenuHelper(this);

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        return menuHelper.onCreateAccountMenu(this, menu) && menuHelper.onCreateLicensesPrivacyMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        return menuHelper.onOptionsItemSelected(item);
    }
}
