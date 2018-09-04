package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.github.rstockbridge.showstats.api.RetrofitInstance;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.github.rstockbridge.showstats.ui.SetlistfmUserStatus;
import com.github.rstockbridge.showstats.ui.TextUtil;
import com.github.rstockbridge.showstats.utility.SimpleTextWatcher;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class UserActivity extends AppCompatActivity {

    private static final String USERS_PATH = "users";
    private static final String USER_ID_KEY = "userId";

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

    private LinearLayout storedUserLayout;
    private TextView storedUserIdLabel;

    private LinearLayout noStoredUserLayout;
    private EditText userIdEditText;
    private Button clearButton;
    private Button submitButton;

    private ProgressBar progressBar;

    private SetlistfmUserStatus setlistfmUserStatus = SetlistfmUserStatus.UNKNOWN;
    private boolean networkCallIsInProgress;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initializeGoogleSignIn();
        initializeFirebase();
        initializeUI();
        determineSetlistfmUser();
    }

    @Override
    protected void onResume() {
        super.onResume();

        syncUI();
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

    private void initializeUI() {
        storedUserLayout = findViewById(R.id.stored_user_layout);
        noStoredUserLayout = findViewById(R.id.no_stored_user_layout);

        storedUserIdLabel = findViewById(R.id.stored_userId);

        userIdEditText = findViewById(R.id.edit_userId_text);
        clearButton = findViewById(R.id.clear_button);
        submitButton = findViewById(R.id.submit_button);

        userIdEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                clearButton.setEnabled(s.length() > 0);
                submitButton.setEnabled(s.length() > 0);
            }
        });

        userIdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    makeUserNetworkCall(TextUtil.getText(userIdEditText));
                    handled = true;
                }
                return handled;
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                userIdEditText.setText("");
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                makeUserNetworkCall(TextUtil.getText(userIdEditText));
            }
        });

        progressBar = findViewById(R.id.progress_bar);
    }

    private void makeUserNetworkCall(@NonNull final String userId) {
        setNetworkCallInProgress(true);

        final SetlistfmService service = RetrofitInstance.getRetrofitInstance().create(SetlistfmService.class);
        final Call<User> call = service.verifyUserId(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull final Call<User> call, @NonNull final Response<User> response) {
                if (response.isSuccessful()) {
                    processSuccessfulUserResponse(response.body());
                } else {
                    processUnsuccessfulUserResponse(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull final Call<User> call, @NonNull final Throwable t) {
                setNetworkCallInProgress(false);
                MessageUtil.makeToast(UserActivity.this, getString(R.string.wrong_message));
            }
        });
    }

    private void processSuccessfulUserResponse(@Nullable final User user) {
        setNetworkCallInProgress(false);

        if (user.getUserId().equals(TextUtil.getText(userIdEditText))) {
            updateDatabase();

            final ArrayList<Setlist> storedSetlists = new ArrayList<>();
            makeSetlistsNetworkCall(user.getUserId(), 1, storedSetlists);
        } else {
            setNetworkCallInProgress(false);
            MessageUtil.makeToast(this, getString(R.string.unresolveable_userId_message));
        }
    }

    private void processUnsuccessfulUserResponse(@Nullable final ResponseBody errorBody) {
        try {
            setNetworkCallInProgress(false);

            if (errorBody != null && errorBody.string().contains(getString(R.string.unknown_userId))) {
                MessageUtil.makeToast(this, getString(R.string.unknown_userId_message));
            } else {
                MessageUtil.makeToast(this, getString(R.string.wrong_message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeSetlistsNetworkCall(
            @NonNull final String userId,
            final int pageIndex,
            @NonNull final ArrayList<Setlist> storedSetlists) {

        setNetworkCallInProgress(true);

        final SetlistfmService service = RetrofitInstance.getRetrofitInstance().create(SetlistfmService.class);
        final Call<SetlistData> call = service.getSetlistData(userId, pageIndex);

        call.enqueue(new Callback<SetlistData>() {
            @Override
            public void onResponse(@NonNull final Call<SetlistData> call, @NonNull final Response<SetlistData> response) {
                if (response.isSuccessful()) {
                    final SetlistData setlistData = response.body();
                    storedSetlists.addAll(setlistData.getSetlists());

                    if (pageIndex < setlistData.getNumberOfPages()) {
                        makeSetlistsNetworkCall(userId, pageIndex + 1, storedSetlists);
                    } else {
                        // update boolean but don't sync UI here as will display before starting activity
                        networkCallIsInProgress = false;

                        User1StatisticsHolder.getSharedInstance().setStatistics(new UserStatistics(userId, storedSetlists));

                        final Intent intent = new Intent(UserActivity.this, TabbedActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    setNetworkCallInProgress(false);
                    MessageUtil.makeToast(UserActivity.this, getString(R.string.no_setlist_data));
                }
            }

            @Override
            public void onFailure(@NonNull final Call<SetlistData> call, @NonNull final Throwable t) {
                setNetworkCallInProgress(false);
                MessageUtil.makeToast(UserActivity.this, getString(R.string.wrong_message));
            }
        });
    }

    private void setNetworkCallInProgress(final boolean inProgress) {
        networkCallIsInProgress = inProgress;
        syncUI();
    }

    private void setSetlistfmUserStatus(final SetlistfmUserStatus inputSetlistfmUserStatus) {
        setlistfmUserStatus = inputSetlistfmUserStatus;
        syncUI();
    }

    private void syncUI() {
        setUserLayout(setlistfmUserStatus);
        setProgessBarVisibility(networkCallIsInProgress || setlistfmUserStatus == SetlistfmUserStatus.UNKNOWN);

        if (setlistfmUserStatus == SetlistfmUserStatus.NOT_STORED) {
            syncNoStoredUserLayoutForNetworkCallStatus(networkCallIsInProgress);
        }
    }

    private void setProgessBarVisibility(final boolean makeVisible) {
        if (makeVisible) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setUserLayout(final SetlistfmUserStatus setlistfmUserStatus) {
        switch (setlistfmUserStatus) {
            case UNKNOWN:
                noStoredUserLayout.setVisibility(View.GONE);
                storedUserLayout.setVisibility(View.GONE);
                break;
            case STORED:
                noStoredUserLayout.setVisibility(View.GONE);
                storedUserLayout.setVisibility(View.VISIBLE);
                break;
            case NOT_STORED:
                noStoredUserLayout.setVisibility(View.VISIBLE);
                storedUserLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void syncNoStoredUserLayoutForNetworkCallStatus(final boolean networkCallIsInProgress) {
        if (networkCallIsInProgress) {
            userIdEditText.setEnabled(false);
            clearButton.setEnabled(false);
            submitButton.setEnabled(false);
        } else {
            userIdEditText.setEnabled(true);
            if (userIdEditText.getText().length() > 0) {
                clearButton.setEnabled(true);
                submitButton.setEnabled(true);
            }
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

    private void updateDatabase() {
        final Map<String, Object> textData = new HashMap<>();
        textData.put(USER_ID_KEY, userIdEditText.getText().toString());

        database.collection(USERS_PATH).document(firebaseUser.getUid())
                .set(textData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MessageUtil.makeToast(UserActivity.this, "Could not update user data!");
                    }
                });
    }

    private void determineSetlistfmUser() {
        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(firebaseUser.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                if (document.exists()
                        && document.getData() != null
                        && document.getData().get(USER_ID_KEY) != null) {

                    setSetlistfmUserStatus(SetlistfmUserStatus.STORED);

                    final String storedUserId = (String) document.getData().get(USER_ID_KEY);
                    storedUserIdLabel.setText(storedUserId);

                    final ArrayList<Setlist> storedSetlists = new ArrayList<>();
                    makeSetlistsNetworkCall(storedUserId, 1, storedSetlists);
                } else {
                    setSetlistfmUserStatus(SetlistfmUserStatus.NOT_STORED);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                setSetlistfmUserStatus(SetlistfmUserStatus.NOT_STORED);
            }
        });
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
                        MessageUtil.makeToast(UserActivity.this, "Could not delete user data! Signing out only.");
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
                        MessageUtil.makeToast(UserActivity.this, "Could not sign out!");
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
                        MessageUtil.makeToast(UserActivity.this, "Could not delete user account! Signing out only");
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
                        MessageUtil.makeToast(UserActivity.this, "Could not sign out of Google!");
                    }
                });
    }

    private void googleRevokeAccess() {
        googleSignInClient
                .revokeAccess()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        MessageUtil.makeToast(UserActivity.this, "Could not revoke Firebase access to Google! Signing out of Google only.");
                        googleSignOut();
                    }
                });
    }

    private void returnToSignInActivity() {
        final Intent intent = new Intent(UserActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
