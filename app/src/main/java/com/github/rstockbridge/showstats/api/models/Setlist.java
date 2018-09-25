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

    public Setlist(@NonNull final String id,
                    @NonNull final String eventDate,
                    @NonNull final SetlistArtist artist,
                    @NonNull final SetlistVenue venue) {

        this.id = id;
        this.eventDate = eventDate;
        this.artist = artist;
        this.venue = venue;
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
}
