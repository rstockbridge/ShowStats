package com.github.rstockbridge.showstats.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public final class DatabaseHelper {

    private static final String USERS_PATH = "users";
    private static final String SETLISTFM_USER_ID_KEY = "userId";
    private static final String SHOW_NOTES_KEY = "showNotesKey";

    public interface SetlistfmUserListener {
        void onStoredSetlistfmUser(final String setlistfmUserId);

        void onNoStoredSetlistfmUser();
    }

    public interface ShowNoteListener {
        void onGetShowNoteCompleted(@Nullable final String text);
    }

    public interface UpdateDatabaseListener {
        void onUpdateDatabaseSuccessful();

        void onUpdateDatabaseUnsuccessful(@Nullable final Exception e);
    }

    public interface DeleteDatabaseListener {
        void onDeleteUserDataSuccessful();

        void onDeleteUserDataUnsuccessful(@NonNull final Exception e);
    }

    @NonNull
    private final FirebaseFirestore database;

    public DatabaseHelper() {
        database = FirebaseFirestore.getInstance();
    }

    public void getSetlistfmUser(@NonNull final String authUserUid, @NonNull final SetlistfmUserListener listener) {
        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(authUserUid);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                if (document.exists()
                        && document.getData() != null
                        && document.getData().get(SETLISTFM_USER_ID_KEY) != null) {

                    listener.onStoredSetlistfmUser((String) document.getData().get(SETLISTFM_USER_ID_KEY));
                } else {
                    listener.onNoStoredSetlistfmUser();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                listener.onNoStoredSetlistfmUser();
            }
        });
    }

    public void updateSetlistfmUserInDatabase(
            @NonNull final String authUserUid,
            @NonNull final String setlistfmUserId,
            @NonNull final UpdateDatabaseListener listener) {

        final Map<String, Object> setlistfmUserIdData = new HashMap<>();
        setlistfmUserIdData.put(SETLISTFM_USER_ID_KEY, setlistfmUserId);

        database.collection(USERS_PATH)
                .document(authUserUid)
                .set(setlistfmUserIdData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onUpdateDatabaseUnsuccessful(e);
                    }
                });
    }

    public void getShowNote(
            @NonNull final String authUserUid,
            @NonNull final String showId,
            @NonNull final ShowNoteListener listener) {

        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(authUserUid);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                if (document.exists()
                        && document.getData() != null
                        && document.getData().get(SHOW_NOTES_KEY) != null) {

                    final Gson gson = new Gson();
                    final String showNotesAsJson = (String) document.getData().get(SHOW_NOTES_KEY);
                    final ShowNotesHolder showNotesHolder = gson.fromJson(showNotesAsJson, ShowNotesHolder.class);

                    final ShowNote note = showNotesHolder.getShowNoteFromId(showId);
                    if (note != null) {
                        listener.onGetShowNoteCompleted(note.getText());
                    } else {
                        listener.onGetShowNoteCompleted(null);
                    }
                } else {
                    listener.onGetShowNoteCompleted(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                listener.onGetShowNoteCompleted(null);
            }
        });
    }

    public void updateShowNoteInDatabase(
            @NonNull final String authUserUid,
            @NonNull final String id,
            @NonNull final String text,
            @NonNull final UpdateDatabaseListener listener) {

        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(authUserUid);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                ShowNotesHolder showNotesHolder = new ShowNotesHolder();
                final Gson gson = new Gson();

                if (document.exists() && document.getData() != null) {
                    if (document.getData().get(SHOW_NOTES_KEY) != null) {
                        final String originalShowNotesAsJson = (String) document.getData().get(SHOW_NOTES_KEY);
                        showNotesHolder = gson.fromJson(originalShowNotesAsJson, ShowNotesHolder.class);
                    }

                    showNotesHolder.updateShowNote(id, text);
                    final String newShowNotesAsJson = gson.toJson(showNotesHolder);

                    final Map<String, Object> showNotesData = new HashMap<>();
                    showNotesData.put(SHOW_NOTES_KEY, newShowNotesAsJson);

                    database.collection(USERS_PATH)
                            .document(authUserUid)
                            .update(showNotesData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(final Void aVoid) {
                                    listener.onUpdateDatabaseSuccessful();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    listener.onUpdateDatabaseUnsuccessful(e);
                                }
                            });

                } else {
                    listener.onUpdateDatabaseUnsuccessful(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                listener.onUpdateDatabaseUnsuccessful(e);
            }
        });
    }

    public void deleteUserData(@NonNull final String authUserUid, @NonNull final DeleteDatabaseListener listener) {
        database.collection(USERS_PATH)
                .document(authUserUid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onDeleteUserDataSuccessful();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onDeleteUserDataUnsuccessful(e);
                    }
                });
    }
}
