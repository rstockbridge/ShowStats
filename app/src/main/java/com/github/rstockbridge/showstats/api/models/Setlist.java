package com.github.rstockbridge.showstats.api.models;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalDate;

public final class Setlist {

    @NonNull
    private final String id;

    @NonNull
    private final String eventDate;

    @NonNull
    private final SetlistArtist artist;

    @NonNull
    private final SetlistVenue venue;

    @NonNull
    private final String url;

    public Setlist(@NonNull final String id,
                   @NonNull final String eventDate,
                   @NonNull final SetlistArtist artist,
                   @NonNull final SetlistVenue venue,
                   @NonNull final String url) {

        this.id = id;
        this.eventDate = eventDate;
        this.artist = artist;
        this.venue = venue;
        this.url = url;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public LocalDate getEventDate() {
        final String[] parsedDate = eventDate.split("-");

        final int year = Integer.parseInt(parsedDate[2]);
        final int month = Integer.parseInt(parsedDate[1]);
        final int dayOfMonth = Integer.parseInt(parsedDate[0]);

        return LocalDate.of(year, month, dayOfMonth);
    }

    @NonNull
    public SetlistArtist getArtist() {
        return artist;
    }

    @NonNull
    public SetlistVenue getVenue() {
        return venue;
    }

    @NonNull
    public String getUrl() {
        return url;
    }
}
