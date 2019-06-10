package com.github.rstockbridge.showstats.auth;

import android.content.Context;
import androidx.annotation.NonNull;

import com.github.rstockbridge.showstats.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

final class GoogleSignInClientWrapper {

    private GoogleSignInClient googleSignInClient;

    GoogleSignInClientWrapper(@NonNull final Context context) {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getResources().getString(R.string.default_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }
}
