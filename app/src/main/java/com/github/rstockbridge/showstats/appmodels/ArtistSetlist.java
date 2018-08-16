package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;

import com.github.rstockbridge.showstats.api.models.SetlistVenue;

import org.threeten.bp.LocalDate;

public final class ArtistSetlist {

    @NonNull
    private LocalDate eventDate;

    @NonNull
    private String venue;

    public ArtistSetlist(@NonNull final LocalDate eventDate, @NonNull final String venue) {
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
