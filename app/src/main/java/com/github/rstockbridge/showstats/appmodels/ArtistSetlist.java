package com.github.rstockbridge.showstats.appmodels;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalDate;

final class ArtistSetlist {

    @NonNull
    private final LocalDate eventDate;

    @NonNull
    private final String venue;

    ArtistSetlist(@NonNull final LocalDate eventDate, @NonNull final String venue) {
        this.eventDate = eventDate;
        this.venue = venue;
    }

    @NonNull
    LocalDate getEventDate() {
        return eventDate;
    }

    @NonNull
    String getVenue() {
        return venue;
    }
}
