package com.github.rstockbridge.showstats.auth;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
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

public final class AuthHelper implements FirebaseAuth.AuthStateListener {

    public interface SignInListener {
        void onSignIn();

        void onGoogleSignInUnsuccessful();

        void onFirebaseAuthSuccessful();

        void onFirebaseAuthUnsuccessful();
    }

    public interface SignOutListener {
        void onSignOutFromFirebase();

        void onFirebaseSignOutUnsucessful();

        void onGoogleSignOutUnsuccessful();

        void onFirebaseDeletionUnsuccessful();

        void onRevokeFirebaseAccessToGoogleUnsuccessful();
    }

    @NonNull
    private final Context appContext;

    @NonNull
    private final GoogleSignInClient googleSignInClient;

    @NonNull
    private final FirebaseAuth firebaseAuth;

    @Nullable
    private SignOutListener signOutListener = null;

    private boolean revokeFirebaseAccessOnGoogleSignOut;

    public AuthHelper(@NonNull final Context context) {
        appContext = context.getApplicationContext();

        final GoogleSignInClientWrapper googleSignInClientWrapper = new GoogleSignInClientWrapper(context.getApplicationContext());
        googleSignInClient = googleSignInClientWrapper.getGoogleSignInClient();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);
    }

    public AuthHelper(@NonNull final Context context, @NonNull final SignOutListener signOutListener) {
        this(context);
        this.signOutListener = signOutListener;
    }

    public void startSignInActivity(@NonNull final SignInListener listener) {
        listener.onSignIn();
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void finishSignIn(@Nullable final Intent data, @NonNull final SignInListener listener) {
        final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            final GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account, listener);
        } catch (final ApiException e) {
            listener.onGoogleSignInUnsuccessful();
        }
    }

    public String getCurrentUserUid() {
        /* Since we listen for authentication state changes, we will assume getCurrentUser() is
           non-null and representing the same user in all other parts of the code */

        return firebaseAuth.getCurrentUser().getUid();
    }

    public boolean isFullyLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (signOutListener != null) {

            if (firebaseAuth.getCurrentUser() == null) {
                if (revokeFirebaseAccessOnGoogleSignOut) {
                    googleRevokeFirebaseAccessAndSignOut(signOutListener);
                } else {
                    googleSignOut(signOutListener);
                }

                /* Don't wait for Google sign out to complete as app should take action on being signed
                   out of Firebase immediately */
                signOutListener.onSignOutFromFirebase();
            }
        }
    }

    public void clearAuthListener() {
        firebaseAuth.removeAuthStateListener(this);
    }

    public void signOut(@NonNull final SignOutListener listener) {
        revokeFirebaseAccessOnGoogleSignOut = false;
        firebaseSignOut(listener);
    }

    public void removeAccount(@NonNull final SignOutListener listener) {
        revokeFirebaseAccessOnGoogleSignOut = true;

        /* If Firebase deletion is successful,the Firebase authentication status will change and
           trigger onAuthStateChanged(), which changes the Google account status as appropriate */

        firebaseAuth.getCurrentUser().delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        listener.onFirebaseDeletionUnsuccessful();
                        signOut(listener);
                    }
                });
    }

    private void firebaseAuthWithGoogle(
            @NonNull final GoogleSignInAccount account,
            @NonNull final SignInListener listener) {

        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth
                .signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(final AuthResult authResult) {
                        listener.onFirebaseAuthSuccessful();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        listener.onFirebaseAuthUnsuccessful();
                    }
                });
    }

    private void firebaseSignOut(@NonNull final SignOutListener listener) {
        /* If Firebase sign out is successful,the Firebase authentication status will change and
           trigger onAuthStateChanged(), which changes the Google account status as appropriate */

        AuthUI.getInstance()
                .signOut(appContext)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        listener.onFirebaseSignOutUnsucessful();
                    }
                });
    }

    private void googleSignOut(@NonNull final SignOutListener listener) {
        googleSignInClient
                .signOut()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        listener.onGoogleSignOutUnsuccessful();
                    }
                });
    }

    private void googleRevokeFirebaseAccessAndSignOut(@NonNull final SignOutListener listener) {
        googleSignInClient
                .revokeAccess()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        googleSignOut(listener);
                        listener.onRevokeFirebaseAccessToGoogleUnsuccessful();
                    }
                });
    }
}
