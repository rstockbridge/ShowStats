package com.github.rstockbridge.showstats.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ShowNotesHolder {

    @NonNull
    private final List<ShowNote> showNotes = new ArrayList<>();

    @NonNull
    public List<ShowNote> getShowNotes() {
        return showNotes;
    }

    public void updateShowNote(@NonNull final String id, @NonNull final String text) {
        final ShowNote showNote = getShowNoteFromId(id);

        if (showNote == null) {
            showNotes.add(new ShowNote(id, text));
        } else {
            if (!text.equals("")) {
                showNote.setText(text);
            } else {
                showNotes.remove(showNote);
            }
        }
    }

    @Nullable
    private ShowNote getShowNoteFromId(@NonNull final String id) {
        for (final ShowNote showNote : showNotes) {
            if (showNote.getId().equals(id)) {
                return showNote;
            }
        }

        return null;
    }
}
