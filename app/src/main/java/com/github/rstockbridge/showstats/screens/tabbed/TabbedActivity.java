package com.github.rstockbridge.showstats.screens.tabbed;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.ui.MenuHelper;
import com.github.rstockbridge.showstats.screens.googlesignin.GoogleSignInActivity;
import com.github.rstockbridge.showstats.ui.MessageUtil;

import timber.log.Timber;

public final class TabbedActivity
        extends AppCompatActivity
        implements AuthHelper.SignOutListener,
        AuthHelper.RevokeAccessListener,
        DatabaseHelper.FlagForDeletionListener {

    private MenuHelper menuHelper;
    private AuthHelper authHelper;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        menuHelper = new MenuHelper(this);
        authHelper = new AuthHelper(this);
        databaseHelper = new DatabaseHelper();

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

    @Override
    public void onSignOutSuccess() {
        finishAndReturnToSignInActivity();
    }

    @Override
    public void onRevokeAccessSuccess() {
        databaseHelper.flagUserForDeletion(authHelper.getCurrentUserUid(), this);
    }

    @Override
    public void onRevokeAccessFailure(@NonNull final String message) {
        Timber.e(message);
        MessageUtil.makeToast(this, "Could not delete account!");
    }

    @Override
    public void onFlagForDeletionSuccessful() {
        authHelper.signOut(this);
    }

    @Override
    public void onFlagForDeletionFailure(final Exception e) {
        Timber.e(e, "Error flagging Firebase data for deletion!");
        MessageUtil.makeToast(this, "Could not delete account! Signing out only.");
        authHelper.signOut(this);
    }

    private void finishAndReturnToSignInActivity() {
        startActivity(new Intent(this, GoogleSignInActivity.class));
        finish();
    }
}
