package com.github.rstockbridge.showstats.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public final class SetlistArtist implements Parcelable {

    @NonNull
    private String mbid;

    @NonNull
    private String name;

    private SetlistArtist(final Parcel in) {
        name = in.readString();
        mbid = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(name);
        dest.writeString(mbid);
    }

    public static final Creator<SetlistArtist> CREATOR = new Creator<SetlistArtist>() {
        @Override
        public SetlistArtist createFromParcel(final Parcel in) {
            return new SetlistArtist(in);
        }

        @Override
        public SetlistArtist[] newArray(final int size) {
            return new SetlistArtist[size];
        }
    };


    @NonNull
    public String getMbid() {
        return mbid;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
