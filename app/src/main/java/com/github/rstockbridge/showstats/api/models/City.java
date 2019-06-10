package com.github.rstockbridge.showstats.api.models;

import androidx.annotation.NonNull;

import com.squareup.moshi.Json;

import java.util.Objects;

public final class City {

    @NonNull
    private final String name;

    @Json(name = "coords")
    @NonNull
    private final Coordinates coordinates;

    public City(@NonNull final String name, @NonNull final Coordinates coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return coordinates.getLatitude();
    }

    public double getLongitude() {
        return coordinates.getLongitude();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final City city = (City) o;
        return Objects.equals(name, city.name) &&
                Objects.equals(coordinates, city.coordinates);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, coordinates);
    }
}

