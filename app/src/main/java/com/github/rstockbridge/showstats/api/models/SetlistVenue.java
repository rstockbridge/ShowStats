package com.github.rstockbridge.showstats.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Objects;

public final class SetlistVenue implements Parcelable {

    @NonNull
    private String name;

    private SetlistVenue(final Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SetlistVenue> CREATOR = new Creator<SetlistVenue>() {
        @Override
        public SetlistVenue createFromParcel(final Parcel in) {
            return new SetlistVenue(in);
        }

        @Override
        public SetlistVenue[] newArray(final int size) {
            return new SetlistVenue[size];
        }
    };

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
