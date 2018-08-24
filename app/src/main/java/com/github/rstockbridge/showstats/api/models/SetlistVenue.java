package com.github.rstockbridge.showstats.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Objects;

public final class SetlistVenue {

    @NonNull
    private String name;

    private SetlistVenue(final Parcel in) {
        name = in.readString();
    }

    @NonNull
    public String getName() {
        return name;
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
