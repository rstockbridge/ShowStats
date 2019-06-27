package com.github.rstockbridge.showstats.api.models;

import androidx.annotation.NonNull;

public final class SetlistArtist {

    @NonNull
    private final String mbid;

    @NonNull
    private final String name;

    public SetlistArtist(@NonNull final String mbid, @NonNull final String name) {
        this.mbid = mbid;
        this.name = name;
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
