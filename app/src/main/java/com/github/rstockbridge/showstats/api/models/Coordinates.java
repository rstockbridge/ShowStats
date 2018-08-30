package com.github.rstockbridge.showstats.api.models;

import com.squareup.moshi.Json;

public final class Coordinates {

    @Json(name = "lat")
    private double latitude;

    @Json(name = "long")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
