package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

import timber.log.Timber;

public final class SignInActivity
        extends AppCompatActivity
        implements AuthHelper.SignInListener {

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    private AuthHelper authHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        authHelper = new AuthHelper(this);

        if (authHelper.isFullyLoggedIn()) {
            startUserActivity();
        }

        final SignInButton signIn = findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                authHelper.startSignInActivity(SignInActivity.this);
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_GOOGLE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    authHelper.finishSignIn(data, this);
                }
                break;
            default:
                throw new IllegalStateException("This line should not be reached.");
        }
    }

    @Override
    public void onSignIn() {
        startActivityForResult(authHelper.getSignInIntent(), REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    public void onGoogleSignInUnsuccessful(@NonNull final ApiException e) {
        Timber.e(e, "Error signing in to Google account!");
        MessageUtil.makeToast(this, "Could not sign in to Google account!");
    }

    @Override
    public void onFirebaseAuthSuccessful() {
        startUserActivity();
    }

    @Override
    public void onFirebaseAuthUnsuccessful(@NonNull final Exception e) {
        Timber.e(e, "Error authenticating with Firebase!");
        MessageUtil.makeToast(SignInActivity.this, "Could not authenticate with Firebase!");
    }

    private void startUserActivity() {
        final Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
    }
}
