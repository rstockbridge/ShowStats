package com.github.rstockbridge.showstats.api;

import android.support.annotation.NonNull;

import com.github.rstockbridge.showstats.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public final class RetrofitInstance {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.setlist.fm/rest/1.0/user/";

    @NonNull
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getClient())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    @NonNull
    private static OkHttpClient getClient() {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        final Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(final Chain chain) throws IOException {
                final Request original = chain.request();

                final Request request = original.newBuilder()
                        .header("x-api-key", BuildConfig.SETLISTFM_KEY)
                        .header("Accept", "application/json")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        };

        final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return httpClient
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(headerInterceptor)
                .build();
    }
}
