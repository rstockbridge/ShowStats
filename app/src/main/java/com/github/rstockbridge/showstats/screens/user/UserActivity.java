package com.github.rstockbridge.showstats.screens.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.api.RetrofitWrapper;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.screens.DeleteDialogFragment;
import com.github.rstockbridge.showstats.screens.googlesignin.GoogleSignInActivity;
import com.github.rstockbridge.showstats.screens.tabbed.TabbedActivity;
import com.github.rstockbridge.showstats.ui.MenuHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.github.rstockbridge.showstats.ui.SetlistfmUserStatus;
import com.github.rstockbridge.showstats.utility.SimpleTextWatcher;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public final class UserActivity
        extends AppCompatActivity
        implements
        DatabaseHelper.SetlistfmUserListener,
        DatabaseHelper.UpdateDatabaseListener,
        AuthHelper.SignOutListener,
        DeleteDialogFragment.NetworkCallListener,
        AuthHelper.RevokeAccessListener,
        AuthHelper.DeleteAuthenticationListener {

    private MenuHelper menuHelper;
    private AuthHelper authHelper;
    private DatabaseHelper databaseHelper;

    private LinearLayout storedUserLayout;
    private TextView storedUserIdLabel;

    private LinearLayout noStoredUserLayout;
    private EditText userIdEditText;
    private Button clearButton;
    private Button goButton;

    private ProgressBar progressBar;

    private SetlistfmUserStatus setlistfmUserStatus = SetlistfmUserStatus.UNKNOWN;
    private boolean networkCallIsInProgress;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        menuHelper = new MenuHelper(this);
        authHelper = new AuthHelper(this);
        databaseHelper = new DatabaseHelper();

        initializeUI();
        databaseHelper.getSetlistfmUser(authHelper.getCurrentUserUid(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        syncUI();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        return menuHelper.onCreateAccountMenu(this, menu) && menuHelper.onCreateLicensesPrivacyMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        return menuHelper.onOptionsItemSelected(item);
    }

    private void initializeUI() {
        storedUserLayout = findViewById(R.id.stored_user_layout);
        noStoredUserLayout = findViewById(R.id.no_stored_user_layout);

        storedUserLayout.setVisibility(View.GONE);
        noStoredUserLayout.setVisibility(View.GONE);

        storedUserIdLabel = findViewById(R.id.stored_userId);

        userIdEditText = findViewById(R.id.edit_note_view);
        clearButton = findViewById(R.id.clear_button);
        goButton = findViewById(R.id.go_button);

        userIdEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                clearButton.setEnabled(s.length() > 0);
                goButton.setEnabled(s.length() > 0);
            }
        });

        userIdEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                makeUserNetworkCall(userIdEditText.getText().toString());
                handled = true;
            }
            return handled;
        });

        clearButton.setOnClickListener(v -> userIdEditText.setText(""));

        goButton.setOnClickListener(v -> makeUserNetworkCall(userIdEditText.getText().toString()));

        progressBar = findViewById(R.id.progress_bar);

        clearButton.setEnabled(false);
        goButton.setEnabled(false);
    }

    private void makeUserNetworkCall(@NonNull final String userId) {
        setNetworkCallInProgress(true);

        final SetlistfmService service = RetrofitWrapper.getRetrofitInstance().create(SetlistfmService.class);
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

        if (user.getUserId().equals(userIdEditText.getText().toString())) {
            databaseHelper.updateSetlistfmUserInDatabase(
                    authHelper.getCurrentUserUid(),
                    userIdEditText.getText().toString(),
                    this);

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

        final SetlistfmService service = RetrofitWrapper.getRetrofitInstance().create(SetlistfmService.class);
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

                        finishAndStartTabbedActivity();
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

    private void finishAndStartTabbedActivity() {
        startActivity(new Intent(UserActivity.this, TabbedActivity.class));
        finish();
        overridePendingTransition(0, 0);
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
        syncProgressBarWithNetworkCall(networkCallIsInProgress || setlistfmUserStatus == SetlistfmUserStatus.UNKNOWN);

        if (setlistfmUserStatus == SetlistfmUserStatus.NOT_STORED) {
            syncNoStoredUserLayoutForNetworkCallStatus(networkCallIsInProgress);
        }
    }

    private void syncProgressBarWithNetworkCall(final boolean makeVisible) {
        progressBar.setVisibility(makeVisible ? View.VISIBLE : View.INVISIBLE);
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

            default:
                throw new IllegalStateException("This line should never be reached.");
        }
    }

    private void syncNoStoredUserLayoutForNetworkCallStatus(final boolean networkCallIsInProgress) {
        if (networkCallIsInProgress) {
            userIdEditText.setEnabled(false);
            clearButton.setEnabled(false);
            goButton.setEnabled(false);
        } else {
            userIdEditText.setEnabled(true);
            if (userIdEditText.getText().length() > 0) {
                clearButton.setEnabled(true);
                goButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onStoredSetlistfmUser(final String setlistfmUserId) {
        setSetlistfmUserStatus(SetlistfmUserStatus.STORED);
        storedUserIdLabel.setText(setlistfmUserId);

        final ArrayList<Setlist> storedSetlists = new ArrayList<>();
        makeSetlistsNetworkCall(setlistfmUserId, 1, storedSetlists);
    }

    @Override
    public void onNoStoredSetlistfmUser() {
        setSetlistfmUserStatus(SetlistfmUserStatus.NOT_STORED);
    }

    @Override
    public void onUpdateDatabaseSuccessful() {
        MessageUtil.makeToast(this, "Data saved!");
    }

    @Override
    public void onUpdateDatabaseUnsuccessful(@Nullable final Exception e) {
        if (e != null) {
            Timber.e(e, "Error updating Firebase database!");
        }

        MessageUtil.makeToast(this, "Could not save data!");
    }

    @Override
    public void onSignOutSuccess() {
        startActivity(new Intent(this, GoogleSignInActivity.class));
        finish();
    }

    @Override
    public void updateUiForDeletionInProgress() {
        setSetlistfmUserStatus(SetlistfmUserStatus.UNKNOWN);
        setNetworkCallInProgress(true);
        syncUI();
    }

    @Override
    public void onRevokeAccessSuccess() {
        authHelper.deleteUserAuthentication(this);
    }

    @Override
    public void onRevokeAccessFailure(@NonNull final String message) {
        setNetworkCallInProgress(false);
        syncUI();

        Timber.e(message);
        MessageUtil.makeToast(this, "Could not delete account!");
    }

    @Override
    public void onDeleteAuthenticationSuccess() {
        authHelper.signOut(this);
    }

    @Override
    public void onDeleteAuthenticationFailure(@NonNull final Exception exception) {
        /* signing out is not an issue if access is revoked but deleting user authentication fails
           - access will simply be granted again the next time the user signs in */

        setNetworkCallInProgress(false);
        syncUI();

        Timber.e(exception, "Error deleting Firebase user authentication record!");
        MessageUtil.makeToast(this, "Could not delete account! Signing out only.");
        authHelper.signOut(this);
    }
}
