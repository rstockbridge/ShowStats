package com.github.rstockbridge.showstats.screens.deletionstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.rstockbridge.showstats.screens.googlesignin.GoogleSignInActivity;
import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.screens.user.UserActivity;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;

public final class DeletionStatusActivity
        extends AppCompatActivity
        implements DatabaseHelper.DeletionStatusListener,
        AuthHelper.SignOutListener,
        AuthHelper.RevokeAccessListener {

    private AuthHelper authHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletion_status);

        authHelper = new AuthHelper(this);
        final DatabaseHelper databaseHelper = new DatabaseHelper();

        databaseHelper.getDeletionStatus(authHelper.getCurrentUserUid(), this);
    }

    @Override
    public void onGetDeletionStatusSuccess(final boolean delete) {
        if (delete) {
            Toast.makeText(this, "Account deletion in progress!", Toast.LENGTH_SHORT).show();
            authHelper.signOut(this);
        } else {
            finishAndStartUserActivity();
        }
    }

    @Override
    public void onGetDeletionStatusFailure() {
        finishAndStartUserActivity();
    }

    @Override
    public void onSignOutSuccess() {
        authHelper.revokeAccountAccess(this);
//        finishAndReturnToSignInActivity();
    }

    private void finishAndStartUserActivity() {
        final Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    private void finishAndReturnToSignInActivity() {
        startActivity(new Intent(DeletionStatusActivity.this, GoogleSignInActivity.class));
        finish();
        overridePendingTransition(0,0);
    }


    @Override
    public void onRevokeAccessSuccess() {
        finishAndReturnToSignInActivity();
    }

    @Override
    public void onRevokeAccessFailure(@NonNull final String message) {
        // not ideal that account access has not been revoked
        // but user can still revoke access in their Google account settings
        finishAndReturnToSignInActivity();
    }
}

// edge cases:
// - user has flagged deletion but database request fails - app still works as per usual
// - revoke access fails, app still return to sign in screen
// create AccountManager helper
