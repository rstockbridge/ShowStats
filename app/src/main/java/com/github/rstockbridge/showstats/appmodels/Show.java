package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Show {
    @NonNull
    private String eventDate;

    @NonNull
    private String venueName;

    @NonNull
    private Map<String, String> artistIdNameMap = new HashMap<>();

    public Show(@NonNull final String eventDate, @NonNull final String venueName) {
        this.eventDate = eventDate;
        this.venueName = venueName;
    }

    @NonNull
    public String getEventDate() {
        return eventDate;
    }

    @NonNull
    public String getVenueName() {
        return venueName;
    }

    @NonNull
    public List<String> getArtistIds() {
        return new ArrayList<>(artistIdNameMap.keySet());
    }

    @NonNull
    public List<String> getArtistNames() {
        final List<String> result = new ArrayList<>(artistIdNameMap.values());
        Collections.sort(result);

        return result;
    }

    public void addArtist(final String artistId, final String artist) {
        if (!artistIdNameMap.containsKey(artistId)) {
            artistIdNameMap.put(artistId, artist);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Show show = (Show) o;
        return Objects.equals(eventDate, show.eventDate) &&
                Objects.equals(venueName, show.venueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventDate, venueName);
    }
}
