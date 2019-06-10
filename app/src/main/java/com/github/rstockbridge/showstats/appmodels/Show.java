package com.github.rstockbridge.showstats.appmodels;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Show {

    @NonNull
    private final String id;

    @NonNull
    private final String eventDate;

    @NonNull
    private final String venueName;

    @NonNull
    private Map<String, String> artistIdNameMap = new HashMap<>();

    @NonNull
    private Map<String, String> artistNameUrlMap = new HashMap<>();

    public Show(
            @NonNull final String id,
            @NonNull final String eventDate,
            @NonNull final String venueName) {

        this.id = id;
        this.eventDate = eventDate;
        this.venueName = venueName;
    }

    @NonNull
    public String getId() {
        return id;
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

    @NonNull
    public ArrayList<String> getUrls() {
        final ArrayList<String> result = new ArrayList<>();

        final List<String> artistNames = getArtistNames();
        for (final String artistName : artistNames) {
            result.add(artistNameUrlMap.get(artistName));
        }

        return result;
    }

    @NonNull
    public String getArtistNameFromId(@NonNull final String artistId) {
        return artistIdNameMap.get(artistId);
    }

    @NonNull
    public String getArtistUrlFromName(@NonNull final String artistName) {
        return artistNameUrlMap.get(artistName);
    }

    public void addArtist(
            @NonNull final String artistId,
            @NonNull final String artist,
            @NonNull final String artistSetlistUrl) {

        if (!artistIdNameMap.containsKey(artistId)) {
            artistIdNameMap.put(artistId, artist);
        }

        if(!artistNameUrlMap.containsKey(artist)) {
            artistNameUrlMap.put(artist, artistSetlistUrl);
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
