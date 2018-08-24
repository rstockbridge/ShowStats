package com.github.rstockbridge.showstats.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public final class SetlistArtist {

    @NonNull
    private String mbid;

    @NonNull
    private String name;

    private SetlistArtist(final Parcel in) {
        name = in.readString();
        mbid = in.readString();
    }

    @NonNull
    public String getMbid() {
        return mbid;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
