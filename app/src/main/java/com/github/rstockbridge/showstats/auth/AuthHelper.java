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

public class AuthHelper {

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
                new ActivityResultGetter.OnActivityResultListener() {
                    @Override
                    public void onActivityResult(@Nullable final Intent data) {
                        signInToFirebase(data, signInListener);
                        activityResultGetter.removeOnActivityResultListener(REQUEST_CODE_GOOGLE_SIGN_IN);
                    }
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(final Void aVoid) {
                        listener.onRevokeAccessSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        listener.onRevokeAccessFailure("Could not revoke Firebase access to Google account.");
                    }
                });
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

    private void signOutOfGoogle(@NonNull final SignOutListener signOutListener) {
        googleSignInClient
                .signOut()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(final Void aVoid) {
                        signOutListener.onSignOutSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        // Force user to re-authenticate despite partial failure.
                        signOutListener.onSignOutSuccess();
                    }
                });
    }
}
