package com.github.rstockbridge.showstats.api.models;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalDate;

public final class Setlist {

    @NonNull
    private String eventDate;

    @NonNull
    private SetlistArtist artist;

    @NonNull
    private SetlistVenue venue;

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
