package com.github.rstockbridge.showstats.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

final class ShowNotesHolder {

    @NonNull
    private final List<ShowNote> showNotes = new ArrayList<>();

    // this method does not add a new empty note and will remove an empty updated note
    void updateShowNote(@NonNull final String id, @NonNull final String text) {
        final ShowNote showNote = getShowNoteFromId(id);

        if (showNote == null) {
            if (!text.equals("")) {
                showNotes.add(new ShowNote(id, text));
            }
        } else {
            if (!text.equals("")) {
                showNote.setText(text);
            } else {
                showNotes.remove(showNote);
            }
        }
    }

    @Nullable
    ShowNote getShowNoteFromId(@NonNull final String id) {
        for (final ShowNote showNote : showNotes) {
            if (showNote.getId().equals(id)) {
                return showNote;
            }
        }

        return null;
    }
}
