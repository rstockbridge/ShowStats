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

import java.util.ArrayList;

public final class TabbedActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "userId";
    private static final String EXTRA_SETLISTS = "setlists";

    @NonNull
    public static Intent newIntent(
            @NonNull final Context context,
            @NonNull final String userId,
            @NonNull final ArrayList<Setlist> setlists) {

        final Intent intent = new Intent(context, TabbedActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putParcelableArrayListExtra(EXTRA_SETLISTS, setlists);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        final String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        final ArrayList<Setlist> setlists = getIntent().getParcelableArrayListExtra(EXTRA_SETLISTS);

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), userId, setlists));

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
