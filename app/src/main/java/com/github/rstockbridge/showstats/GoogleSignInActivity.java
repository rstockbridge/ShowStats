package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.auth.ActivityResultGetter;
import com.github.rstockbridge.showstats.auth.AuthHelper2;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.github.rstockbridge.showstats.ui.TextUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class GoogleSignInActivity
        extends BaseActivity
        implements ActivityResultGetter, AuthHelper2.SignInListener {

    private AuthHelper2 authHelper2;
    private Map<Integer, OnActivityResultListener> onActivityResultListeners = new HashMap<>();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        authHelper2 = new AuthHelper2(this);

        if (authHelper2.isUserLoggedIn()) {
            startUserActivity();
        }

        final SignInButton signIn = findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                authHelper2.signIn(GoogleSignInActivity.this, GoogleSignInActivity.this);
            }
        });

        final TextView setlistfmAttributionLabel = findViewById(R.id.setlistfm_attribution);
        final String linkedString = getString(R.string.powered_hyperlink, getString(R.string.setlistfm_homepage), getString(R.string.setlistfm));
        setlistfmAttributionLabel.setText(TextUtil.fromHtml(linkedString));
        setlistfmAttributionLabel.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (onActivityResultListeners.containsKey(requestCode)) {
            onActivityResultListeners.get(requestCode).onActivityResult(data);
            return;
        }

        throw new IllegalStateException("This line should not be reached.");
    }

    @Override
    public void setOnActivityResultListener(
            final int resultCode,
            @NonNull final OnActivityResultListener listener) {

        onActivityResultListeners.put(resultCode, listener);
    }

    @Override
    public void removeOnActivityResultListener(final int resultCode) {
        onActivityResultListeners.remove(resultCode);
    }

    @Override
    public void onSignInSuccess() {
        startUserActivity();
    }

    @Override
    public void onSignInFailure(@NonNull final Exception exception) {
        MessageUtil.makeToast(this, "Sign in failed");
        Timber.e(exception);
    }

    @Override
    public void onSignInFailure(@NonNull final String string) {
        MessageUtil.makeToast(this, "Sign in failed");
        Timber.e(string);
    }

    private void startUserActivity() {
        final Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
    }

}
