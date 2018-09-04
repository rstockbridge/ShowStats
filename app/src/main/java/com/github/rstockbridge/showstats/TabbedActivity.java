package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public final class TabbedActivity extends AppCompatActivity {

    private static final String USERS_PATH = "users";

    @NonNull
    private GoogleSignInClient googleSignInClient;

    @NonNull
    private FirebaseAuth firebaseAuth;

    @NonNull
    private FirebaseAuth.AuthStateListener authStateListener;

    @NonNull
    private FirebaseFirestore database;

    /* Since we listen for authentication state changes, we will assume this is non-null and
       representing the same user in all other parts of the code */
    private FirebaseUser firebaseUser;

    private boolean revokeAccessOnGoogleSignOut;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        initializeGoogleSignIn();
        initializeFirebase();

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onDestroy() {
        firebaseAuth.removeAuthStateListener(authStateListener);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;
            case R.id.sign_out_remove_account:
                signOutAndRemoveAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeGoogleSignIn() {
        final GoogleSignInClientWrapper googleSignInClientWrapper = new GoogleSignInClientWrapper(this);
        googleSignInClient = googleSignInClientWrapper.getGoogleSignInClient();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Finish activity if detects no user is signed in to Firebase
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    if (revokeAccessOnGoogleSignOut) {
                        googleRevokeAccess();
                    } else {
                        googleSignOut();
                    }

                    // return to signInActivity even if still signed in to Google - it doesn't
                    // make sense to stay in this activity if signed out of Firebase
                    returnToSignInActivity();
                }
            }
        };

        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void signOut() {
        revokeAccessOnGoogleSignOut = false;
        firebaseSignOut();
    }

    private void signOutAndRemoveAccount() {
        revokeAccessOnGoogleSignOut = true;

        // Delete Firebase user data
        database.collection(USERS_PATH)
                .document(firebaseUser.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Deleting Firebase account will also sign out of Firebase
                        deleteFirebaseAccount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MessageUtil.makeToast(TabbedActivity.this, "Could not delete user data! Signing out only.");
                        signOut();
                    }
                });
    }

    private void firebaseSignOut() {
        /* If Firebase sign out is successful,the Firebase authentication status will change and
           trigger onAuthStateChanged(), which changes the Google account status as appropriate */

        AuthUI.getInstance()
                .signOut(this)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        MessageUtil.makeToast(TabbedActivity.this, "Could not sign out!");
                    }
                });
    }

    private void deleteFirebaseAccount() {
        /* If Firebase deletion is successful,the Firebase authentication status will change and
           trigger onAuthStateChanged(), which changes the Google account status as appropriate */

        firebaseUser.delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        MessageUtil.makeToast(TabbedActivity.this, "Could not delete user account! Signing out only");
                        signOut();
                    }
                });
    }

    private void googleSignOut() {
        googleSignInClient
                .signOut()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        MessageUtil.makeToast(TabbedActivity.this, "Could not sign out of Google!");
                    }
                });
    }

    private void googleRevokeAccess() {
        googleSignInClient
                .revokeAccess()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        MessageUtil.makeToast(TabbedActivity.this, "Could not revoke Firebase access to Google! Signing out of Google only.");
                        googleSignOut();
                    }
                });
    }

    private void returnToSignInActivity() {
        final Intent intent = new Intent(TabbedActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
