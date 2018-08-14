package com.github.rstockbridge.showstats.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDate;

public final class Setlist implements Parcelable {

    @NonNull
    private String eventDate;

    @NonNull
    private SetlistArtist artist;

    @NonNull
    private SetlistVenue venue;

    private Setlist(final Parcel in) {
        eventDate = in.readString();
        artist = in.readParcelable(SetlistArtist.class.getClassLoader());
        venue = in.readParcelable(SetlistVenue.class.getClassLoader());
    }

    public static final Creator<Setlist> CREATOR = new Creator<Setlist>() {
        @Override
        public Setlist createFromParcel(final Parcel in) {
            return new Setlist(in);
        }

        @Override
        public Setlist[] newArray(final int size) {
            return new Setlist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(eventDate);
        dest.writeParcelable(artist, flags);
        dest.writeParcelable(venue, flags);
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
