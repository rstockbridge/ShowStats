package com.github.rstockbridge.showstats.api.models;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

public final class City {

    @NonNull
    private String name;

    @Json(name = "coords")
    @NonNull
    private Coordinates coordinates;

    public double getLatitude() {
        return coordinates.getLatitude();
    }

    public double getLongitude() {
        return coordinates.getLongitude();
    }
}
