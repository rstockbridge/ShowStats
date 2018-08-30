package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.github.rstockbridge.showstats.api.RetrofitInstance;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.github.rstockbridge.showstats.ui.TextUtil;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class UserActivity extends AppCompatActivity {

    private EditText userIdText;

    private Button clear;
    private Button submit;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userIdText = findViewById(R.id.edit_userId_text);
        clear = findViewById(R.id.clear_button);
        submit = findViewById(R.id.submit_button);

        userIdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // this method intentionally left blank
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                clear.setEnabled(s.length() > 0);
                submit.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                // this method intentionally left blank
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                userIdText.setText("");
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setViewsForInProgress();
                makeUserNetworkCall(TextUtil.getText(userIdText));
            }
        });

        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setViewsForUse();
    }

    private void makeUserNetworkCall(@NonNull final String userId) {
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
                setViewsForUse();
                MessageUtil.makeToast(UserActivity.this, getString(R.string.wrong_message));
            }
        });
    }

    private void processSuccessfulUserResponse(@Nullable final User user) {
        if (user.getUserId().equals(TextUtil.getText(userIdText))) {
            final ArrayList<Setlist> storedSetlists = new ArrayList<>();
            makeSetlistsNetworkCall(user.getUserId(), 1, storedSetlists);
        } else {
            setViewsForUse();
            MessageUtil.makeToast(this, getString(R.string.unresolveable_userId_message));
        }
    }

    private void processUnsuccessfulUserResponse(@Nullable final ResponseBody errorBody) {
        try {
            setViewsForUse();

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
                        //setViewsForSuccessfulResponse();
                        User1StatisticsHolder.getSharedInstance().setStatistics(new UserStatistics(userId, storedSetlists));

                        final Intent intent = new Intent(UserActivity.this, TabbedActivity.class);
                        startActivity(intent);
                    }
                } else {
                    setViewsForUse();
                    MessageUtil.makeToast(UserActivity.this, getString(R.string.no_setlist_data));
                }
            }

            @Override
            public void onFailure(@NonNull final Call<SetlistData> call, @NonNull final Throwable t) {
                setViewsForUse();
                MessageUtil.makeToast(UserActivity.this, getString(R.string.wrong_message));
            }
        });
    }

    private void setViewsForInProgress() {
        userIdText.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        syncButtonsWithText();
    }

    private void setViewsForUse() {
        userIdText.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        syncButtonsWithText();
    }

    private void syncButtonsWithText() {
        clear.setEnabled(userIdText.isEnabled() && userIdText.getText().length() > 0);
        submit.setEnabled(userIdText.isEnabled() && userIdText.getText().length() > 0);
    }
}
