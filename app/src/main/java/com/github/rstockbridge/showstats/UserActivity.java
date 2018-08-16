package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.rstockbridge.showstats.api.RetrofitInstance;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class UserActivity extends AppCompatActivity {

    private EditText userIdText;

    private ArrayList<Setlist> storedSetlists;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userIdText = findViewById(R.id.edit_userId_text);
        final Button submit = findViewById(R.id.submit_button);

        userIdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // method intentionally left blank
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                submit.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                //method intentionally left blank
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                submit.setEnabled(false);
                storedSetlists = new ArrayList<>();
                makeUserNetworkCall(getUserIdText());
            }
        });
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
                makeToast(getString(R.string.wrong_message));
            }
        });
    }

    private void processSuccessfulUserResponse(final User user) {
        if (user != null && user.getUserId().equals(getUserIdText())) {
            makeSetlistsNetworkCall(user.getUserId(), 1);
        } else {
            makeToast(getString(R.string.unresolveable_userId_message));
        }
    }

    private void processUnsuccessfulUserResponse(final ResponseBody errorBody) {
        try {
            if (errorBody != null && errorBody.string().contains(getString(R.string.unknown_userId))) {
                makeToast(getString(R.string.unknown_userId_message));
            } else {
                makeToast(getString(R.string.wrong_message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeSetlistsNetworkCall(@NonNull final String userId, final int pageIndex) {
        final SetlistfmService service = RetrofitInstance.getRetrofitInstance().create(SetlistfmService.class);
        final Call<SetlistData> call = service.getSetlistData(userId, pageIndex);

        call.enqueue(new Callback<SetlistData>() {
            @Override
            public void onResponse(@NonNull final Call<SetlistData> call, @NonNull final Response<SetlistData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final SetlistData setlistData = response.body();
                    storedSetlists.addAll(setlistData.getSetlists());

                    if (pageIndex < setlistData.getNumberOfPages()) {
                        makeSetlistsNetworkCall(userId, pageIndex + 1);
                    } else {
                        startActivity(TabbedActivity.newIntent(UserActivity.this, userId, storedSetlists));
                    }
                } else {
                    makeToast(getString(R.string.no_setlist_data));
                }
            }

            @Override
            public void onFailure(@NonNull final Call<SetlistData> call, @NonNull final Throwable t) {
                makeToast(getString(R.string.wrong_message));
            }
        });
    }

    private void makeToast(@NonNull final String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private String getUserIdText() {
        return userIdText.getText().toString();
    }
}
