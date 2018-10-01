package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalDate;

public final class ArtistSetlist {

    @NonNull
    private final LocalDate eventDate;

    @NonNull
    private final String venue;

    ArtistSetlist(@NonNull final LocalDate eventDate, @NonNull final String venue) {
        this.eventDate = eventDate;
        this.venue = venue;
    }

    @NonNull
    public LocalDate getEventDate() {
        return eventDate;
    }

    @NonNull
    public String getVenue() {
        return venue;
    }
}
