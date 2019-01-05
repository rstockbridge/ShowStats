package com.github.rstockbridge.showstats.auth;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthHelper2 {

    public interface SignInListener {
        void onSignInSuccess();
        void onSignInFailure(@NonNull final Exception exception);
        void onSignInFailure(@NonNull final String string);
    }

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    @NonNull
    private final GoogleSignInClient googleSignInClient;

    @NonNull
    private final FirebaseAuth firebaseAuth;

    public AuthHelper2(@NonNull final Context context) {
        final GoogleSignInClientWrapper googleSignInClientWrapper =
                new GoogleSignInClientWrapper(context.getApplicationContext());

        googleSignInClient = googleSignInClientWrapper.getGoogleSignInClient();

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public boolean isUserLoggedIn() {
        return false;
    }

    public void signIn(@NonNull final ActivityResultGetter activityResultGetter,
                       @NonNull final SignInListener signInListener) {

        activityResultGetter.setOnActivityResultListener(
                REQUEST_CODE_GOOGLE_SIGN_IN,
                new ActivityResultGetter.OnActivityResultListener() {
                    @Override
                    public void onActivityResult(@Nullable final Intent data) {
                        processGoogleSignInResult(data, signInListener);
                        activityResultGetter.removeOnActivityResultListener(REQUEST_CODE_GOOGLE_SIGN_IN);
                    }
                });

        activityResultGetter.startActivityForResult(
                googleSignInClient.getSignInIntent(),
                REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    public String getCurrentUserUid() {
        /* Since we listen for authentication state changes, we will assume getCurrentUser() is
           non-null and representing the same user in all other parts of the code */

        return firebaseAuth.getCurrentUser().getUid();
    }

    private void processGoogleSignInResult(
            @Nullable final Intent data,
            @NonNull final SignInListener signInListener
    ) {
        final Task<GoogleSignInAccount> task =
                GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            final GoogleSignInAccount account = task.getResult(ApiException.class);

            if (account != null) {
                final String googleIdToken = account.getIdToken();

                if (googleIdToken != null) {
                    firebaseAuthWithGoogle(googleIdToken, signInListener);
                } else {
                    signInListener.onSignInFailure("Google Sign In Account token was null...");
                }
            } else {
                signInListener.onSignInFailure("Google Sign In Account was null...");
            }
        } catch (final ApiException e) {
            signInListener.onSignInFailure(e);
        }
    }

    private void firebaseAuthWithGoogle(
            @NonNull final String googleIdToken,
            @NonNull final SignInListener signInListener
    ) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        firebaseAuth
                .signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(final AuthResult authResult) {
                        signInListener.onSignInSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        signInListener.onSignInFailure(e);
                    }
                });
    }

}
