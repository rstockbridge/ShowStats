package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.google.android.gms.common.SignInButton;

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

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            authHelper.finishSignIn(data, this);
        }
    }

    @Override
    public void onSignIn() {
        startActivityForResult(authHelper.getSignInIntent(), REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    public void onGoogleSignInUnsuccessful() {
        MessageUtil.makeToast(this, "Could not sign in to Google account!");
    }

    @Override
    public void onFirebaseAuthSuccessful() {
        startUserActivity();
    }

    @Override
    public void onFirebaseAuthUnsuccessful() {
        MessageUtil.makeToast(SignInActivity.this, "Could not authenticate with Firebase!");
    }

    private void startUserActivity() {
        final Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
    }
}
