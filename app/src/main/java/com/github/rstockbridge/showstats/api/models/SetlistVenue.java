package com.github.rstockbridge.showstats.api.models;

import android.support.annotation.NonNull;

import java.util.Objects;

public final class SetlistVenue {

    @NonNull
    private final String name;

    @NonNull
    private final City city;

    public SetlistVenue(@NonNull final String name, @NonNull final City city) {
        this.name = name;
        this.city = city;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public City getCity() {
        return city;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SetlistVenue that = (SetlistVenue) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
