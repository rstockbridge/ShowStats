package com.github.rstockbridge.showstats.api.models;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

import java.util.List;

public final class SetlistData {

    @Json(name = "setlist")
    @NonNull
    private List<Setlist> setlists;

    private int itemsPerPage;

    @Json(name = "total")
    private int numberOfSetlists;

    @NonNull
    public List<Setlist> getSetlists() {
        return setlists;
    }

    public int getNumberOfPages() {
        return (int) Math.ceil((double) numberOfSetlists / (double) itemsPerPage);
    }
}
