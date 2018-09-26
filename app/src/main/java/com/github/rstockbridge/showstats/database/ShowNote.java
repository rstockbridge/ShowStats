package com.github.rstockbridge.showstats.database;

import android.support.annotation.NonNull;

public final class ShowNote {

    @NonNull
    private final String id;

    @NonNull
    private String text;

    public ShowNote(@NonNull final String id, @NonNull final String text) {
        this.id = id;
        this.text = text;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(@NonNull final String text) {
        this.text = text;
    }
}
