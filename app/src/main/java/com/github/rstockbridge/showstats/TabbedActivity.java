package com.github.rstockbridge.showstats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;

import java.util.ArrayList;

public final class TabbedActivity extends AppCompatActivity {

    private static final String EXTRA_USER_STATISTICS = "statistics";

    @NonNull
    public static Intent newIntent(
            @NonNull final Context context,
            @NonNull final UserStatistics userStatistics) {

        final Intent intent = new Intent(context, TabbedActivity.class);
        intent.putExtra(EXTRA_USER_STATISTICS, userStatistics);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        final UserStatistics statistics = getIntent().getParcelableExtra(EXTRA_USER_STATISTICS);

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), statistics));

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
