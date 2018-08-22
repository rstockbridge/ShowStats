package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class Artist {

    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private List<ArtistSetlist> setlists;

    Artist(@NonNull final String id, @NonNull final String name, @NonNull final ArtistSetlist setlist) {
        this.id = id;
        this.name = name;

        setlists = new ArrayList<>();
        setlists.add(setlist);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public List<ArtistSetlist> getSetlists() {
        return setlists;
    }

    public void addSetlist(final ArtistSetlist setlist) {
        setlists.add(setlist);
    }
}
