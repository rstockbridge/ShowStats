package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;

public final class TabbedActivity
        extends AppCompatActivity
        implements AuthHelper.SignOutListener,
        DatabaseHelper.DeleteDatabaseListener {

    @NonNull
    private AuthHelper authHelper;

    @NonNull
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        authHelper = new AuthHelper(this, this);
        databaseHelper = new DatabaseHelper();

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onDestroy() {
        authHelper.clearAuthListener();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                authHelper.signOut(this);
                return true;
            case R.id.sign_out_remove_account:
                // if successful, Firebase account will then be removed
                databaseHelper.deleteUserData(authHelper.getCurrentUserUid(), this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void returnToSignInActivity() {
        final Intent intent = new Intent(TabbedActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSignOutFromFirebase() {
        // return to signInActivity even if still signed in to Google - it doesn't
        // make sense to stay in this activity if signed out of Firebase
        returnToSignInActivity();
    }

    @Override
    public void onFirebaseSignOutUnsucessful(@NonNull final Exception e) {
        Log.e(TabbedActivity.class.getSimpleName(), "Error signing out of Firebase!", e);
        MessageUtil.makeToast(this, "Could not sign out of Firebase!");
    }

    @Override
    public void onGoogleSignOutUnsuccessful(@NonNull final Exception e) {
        Log.e(TabbedActivity.class.getSimpleName(), "Error signing out of Google!", e);
        MessageUtil.makeToast(this, "Could not sign out of Google!");
    }

    @Override
    public void onFirebaseDeletionUnsuccessful(@NonNull final Exception e) {
        Log.e(TabbedActivity.class.getSimpleName(), "Error deleting Firebase data!", e);
        MessageUtil.makeToast(this, "Could not delete user account! Signing out only");
    }

    @Override
    public void onRevokeFirebaseAccessToGoogleUnsuccessful(@NonNull final Exception e) {
        Log.e(TabbedActivity.class.getSimpleName(), "Error revoking Firebase access to Google!", e);
        MessageUtil.makeToast(this, "Could not revoke Firebase access to Google! Signing out of Google only.");
    }

    @Override
    public void onDeleteUserDataSuccessful() {
        authHelper.removeAccount(this);
    }

    @Override
    public void onDeleteUserDataUnsuccessful(@NonNull final Exception e) {
        MessageUtil.makeToast(this, "Could not delete user data! Signing out only.");
        authHelper.signOut(this);
    }
}
