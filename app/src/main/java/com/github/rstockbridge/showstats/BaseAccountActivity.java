package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.screens.googlesignin.GoogleSignInActivity;
import com.github.rstockbridge.showstats.ui.MessageUtil;

import timber.log.Timber;

public abstract class BaseAccountActivity
        extends AppCompatActivity
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
        startActivity(new Intent(BaseAccountActivity.this, GoogleSignInActivity.class));
        finish();
    }
}
