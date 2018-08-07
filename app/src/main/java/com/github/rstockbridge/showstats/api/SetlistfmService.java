package com.github.rstockbridge.showstats.api;

import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface SetlistfmService {
    @GET("user/{userId}")
    Call<User> verifyUserId(@Path("userId") @NonNull final String userId);
}