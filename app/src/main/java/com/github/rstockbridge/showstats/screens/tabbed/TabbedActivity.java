package com.github.rstockbridge.showstats.screens.tabbed;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.screens.DeleteDialogFragment;
import com.github.rstockbridge.showstats.screens.googlesignin.GoogleSignInActivity;
import com.github.rstockbridge.showstats.ui.MenuHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.google.android.material.tabs.TabLayout;

import timber.log.Timber;

public final class TabbedActivity
        extends AppCompatActivity
        implements AuthHelper.SignOutListener,
        DeleteDialogFragment.NetworkCallListener,
        AuthHelper.RevokeAccessListener,
        AuthHelper.DeleteAuthenticationListener {

    private MenuHelper menuHelper;
    private AuthHelper authHelper;

    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        menuHelper = new MenuHelper(this);
        authHelper = new AuthHelper(this);

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        progressBar = findViewById(R.id.progress_bar);
        contentLayout = findViewById(R.id.content_layout);
        setDeletionInProgress(false);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        return menuHelper.onCreateAccountMenu(this, menu)
                && menuHelper.onCreateLicensesPrivacyMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        return menuHelper.onOptionsItemSelected(item);
    }

    @Override
    public void onSignOutSuccess() {
        startActivity(new Intent(this, GoogleSignInActivity.class));
        finish();
    }

    @Override
    public void updateUiForDeletionInProgress() {
        setDeletionInProgress(true);
    }

    @Override
    public void onRevokeAccessSuccess() {
        authHelper.deleteUserAuthentication(this);
    }

    @Override
    public void onRevokeAccessFailure(@NonNull final String message) {
        setDeletionInProgress(false);

        Timber.e(message);
        MessageUtil.makeToast(this, "Could not delete account!");
    }

    @Override
    public void onDeleteAuthenticationSuccess() {
        authHelper.signOut(this);
    }

    @Override
    public void onDeleteAuthenticationFailure(@NonNull final Exception exception) {
        /* signing out is not an issue if access is revoked but deleting user authentication fails
           - access will simply be granted again the next time the user signs in */

        setDeletionInProgress(false);

        Timber.e(exception, "Error deleting Firebase user authentication record!");
        MessageUtil.makeToast(this, "Could not delete account! Signing out only.");
        authHelper.signOut(this);
    }

    private void setDeletionInProgress(final boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
        contentLayout.setVisibility(inProgress ? View.INVISIBLE : View.VISIBLE);
    }
}
