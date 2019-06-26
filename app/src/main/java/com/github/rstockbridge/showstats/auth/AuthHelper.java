package com.github.rstockbridge.showstats.auth;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.functions.FirebaseFunctions;

public class AuthHelper {

    /* account deletion consists of:
       1. revoking Firebase access to Google Account
       2. deleting Firebase user authentication record
       3. signing out

       A scheduled cloud function will take care of deleting database data. Note that if a user deletes
       their account and tries to sign back in before that account data is deleted, they will be allowed
       to create a new account. The cloud function will still delete the previous account data.
     */

    public interface SignInListener {
        void onSignInSuccess();

        void onSignInFailure(@NonNull Exception exception);

        void onSignInFailure(@NonNull String string);
    }

    public interface SignOutListener {
        void onSignOutSuccess();
    }

    public interface RevokeAccessListener {
        void onRevokeAccessSuccess();

        void onRevokeAccessFailure(@NonNull String message);
    }

    public interface DeleteAuthenticationListener {
        void onDeleteAuthenticationSuccess();

        void onDeleteAuthenticationFailure(@NonNull Exception exception);
    }

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    @NonNull
    private final GoogleSignInClient googleSignInClient;

    @NonNull
    private final FirebaseAuth firebaseAuth;

    public AuthHelper(@NonNull final Context context) {
        final GoogleSignInClientWrapper googleSignInClientWrapper =
                new GoogleSignInClientWrapper(context.getApplicationContext());

        googleSignInClient = googleSignInClientWrapper.getGoogleSignInClient();

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public String getCurrentUserUid() {
        return firebaseAuth.getCurrentUser().getUid();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void signIn(@NonNull final ActivityResultGetter activityResultGetter,
                       @NonNull final SignInListener signInListener) {

        activityResultGetter.setOnActivityResultListener(
                REQUEST_CODE_GOOGLE_SIGN_IN,
                data -> {
                    signInToFirebase(data, signInListener);
                    activityResultGetter.removeOnActivityResultListener(REQUEST_CODE_GOOGLE_SIGN_IN);
                });

        // Sign in to Google
        activityResultGetter.startActivityForResult(
                googleSignInClient.getSignInIntent(),
                REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    public void signOut(@NonNull final SignOutListener signOutListener) {
        firebaseAuth.signOut();
        signOutOfGoogle(signOutListener);
    }

    public void revokeAccountAccess(@NonNull final RevokeAccessListener listener) {
        googleSignInClient
                .revokeAccess()
                .addOnSuccessListener(aVoid -> listener.onRevokeAccessSuccess())
                .addOnFailureListener(e -> listener.onRevokeAccessFailure("Could not revoke Firebase access to Google account."));
    }

    public void deleteUserAuthentication(@NonNull final DeleteAuthenticationListener deleteAuthenticationListener) {
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("deleteUserAuthentication")
                .call()
                .addOnSuccessListener(httpsCallableResult -> deleteAuthenticationListener.onDeleteAuthenticationSuccess())
                .addOnFailureListener(deleteAuthenticationListener::onDeleteAuthenticationFailure);
    }

    private void signInToFirebase(
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
                    signInListener.onSignInFailure("Google Sign In Account token was null.");
                }
            } else {
                signInListener.onSignInFailure("Google Sign In Account was null.");
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
                .addOnSuccessListener(authResult -> signInListener.onSignInSuccess())
                .addOnFailureListener(signInListener::onSignInFailure);
    }

    private void signOutOfGoogle(@NonNull final SignOutListener signOutListener) {
        googleSignInClient
                .signOut()
                .addOnSuccessListener(aVoid -> signOutListener.onSignOutSuccess())
                .addOnFailureListener(e -> {
                    // Force user to re-authenticate despite partial failure.
                    signOutListener.onSignOutSuccess();
                });
    }
}
