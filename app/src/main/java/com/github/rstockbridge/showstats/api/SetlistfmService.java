package com.github.rstockbridge.showstats.api;

import androidx.annotation.NonNull;

import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SetlistfmService {

    @GET("{userId}")
    Call<User> verifyUserId(@Path("userId") @NonNull final String userId);

    @GET("{userId}/attended")
    Call<SetlistData> getSetlistData(
            @Path("userId") @NonNull final String userId,
            @Query("p") final int pageIndex);
}
