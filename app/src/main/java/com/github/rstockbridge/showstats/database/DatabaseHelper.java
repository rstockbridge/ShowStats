package com.github.rstockbridge.showstats.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private static final String DELETE_KEY = "delete";

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

    public interface FlagForDeletionListener {
        void onFlagForDeletionSuccessful();

        void onFlagForDeletionFailure(final Exception e);
    }

    public interface DeletionStatusListener {
        void onGetDeletionStatusSuccess(boolean delete);

        void onGetDeletionStatusFailure();
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

    public void flagUserForDeletion(@NonNull final String authUserUid, @NonNull final FlagForDeletionListener listener) {
        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(authUserUid);

        final Map<String, Object> deleteData = new HashMap<>();
        deleteData.put(DELETE_KEY, "true");

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                if (document.exists() && document.getData() != null) {
                    database.collection(USERS_PATH)
                            .document(authUserUid)
                            .update(deleteData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(final Void aVoid) {
                                    listener.onFlagForDeletionSuccessful();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    listener.onFlagForDeletionFailure(e);
                                }
                            });

                } else {
                    database.collection(USERS_PATH)
                            .document(authUserUid)
                            .set(deleteData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(final Void aVoid) {
                                    listener.onFlagForDeletionSuccessful();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    listener.onFlagForDeletionFailure(e);
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                listener.onFlagForDeletionFailure(null);

            }
        });
    }

    public void getDeletionStatus(@NonNull final String authUserUid, @NonNull final DeletionStatusListener listener) {

        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(authUserUid);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                if (document.exists()
                        && document.getData() != null
                        && document.getData().get(DELETE_KEY) != null) {

                    final String deletionStatus = (String) document.getData().get(DELETE_KEY);
                    if (deletionStatus == null || !deletionStatus.equals("true")) {
                        listener.onGetDeletionStatusSuccess(false);
                    } else {
                        listener.onGetDeletionStatusSuccess(true);
                    }
                } else {
                    listener.onGetDeletionStatusSuccess(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                listener.onGetDeletionStatusFailure();
            }
        });
    }
}
