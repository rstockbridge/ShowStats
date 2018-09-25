package com.github.rstockbridge.showstats.api.models;

import com.squareup.moshi.Json;

import java.util.Objects;

public final class Coordinates {

    @Json(name = "lat")
    private final double latitude;

    @Json(name = "long")
    private final double longitude;

    public Coordinates(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    double getLatitude() {
        return latitude;
    }

    double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Coordinates that = (Coordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(latitude, longitude);
    }
}
