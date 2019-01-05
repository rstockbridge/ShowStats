package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;

import timber.log.Timber;

abstract class BaseAccountActivity
        extends BaseActivity
        implements AuthHelper.SignOutListener,
        AuthHelper.RevokeAccessListener,
        DatabaseHelper.FlagForDeletionListener {

    private AuthHelper authHelper;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new AuthHelper(this);
        databaseHelper = new DatabaseHelper();
    }

    @Override
    protected void onCreateSpecializedOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                    authHelper.signOut(this);
                return true;
            case R.id.delete_account:
                authHelper.revokeAccountAccess(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        Timber.e(e, "Error flagging rFirebase data for deletion!");
        MessageUtil.makeToast(this, "Could not delete account! Signing out only.");
        authHelper.signOut(this);
    }

    private void finishAndReturnToSignInActivity() {
        startActivity(new Intent(BaseAccountActivity.this, GoogleSignInActivity.class));
        finish();
    }
}
