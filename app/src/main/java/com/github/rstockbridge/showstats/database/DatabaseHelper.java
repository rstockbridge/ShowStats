package com.github.rstockbridge.showstats.database;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class DatabaseHelper {

    private static final String USERS_PATH = "users";
    private static final String SETLISTFM_USER_ID_KEY = "userId";
    private static final String SHOWS_PATH = "shows";
    private static final String SHOW_NOTES_KEY = "showNotesKey";

    public interface SetlistfmUserListener {
        void onStoredSetlistfmUser(final String setlistfmUserId);

        void onNoStoredSetlistfmUser();
    }

    public interface ShowNotesListener {
        void onShowNotes(final String notes);
    }

    public interface UpdateDatabaseListener {
        void onUpdateDatabaseUnsuccessful();
    }

    public interface DeleteDatabaseListener {
        void onDeleteUserDataSuccessful();

        void onDeleteUserDataUnsuccessful();
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
                        listener.onUpdateDatabaseUnsuccessful();
                    }
                });
    }

    public void getShowNotes(
            @NonNull final String authUserUid,
            @NonNull final String showId,
            @NonNull final ShowNotesListener listener) {

        final DocumentReference docRef = database
                .collection(USERS_PATH)
                .document(authUserUid)
                .collection(SHOWS_PATH)
                .document(showId);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot document) {
                if (document.exists()
                        && document.getData() != null
                        && document.getData().get(SHOW_NOTES_KEY) != null) {

                    listener.onShowNotes((String) document.getData().get(SHOW_NOTES_KEY));
                }
            }
        });
    }

    public void updateShowNotesInDatabase(
            @NonNull final String authUserUid,
            @NonNull final String showId,
            @NonNull final String showNotes,
            @NonNull final UpdateDatabaseListener listener) {

        final Map<String, Object> showNotesData = new HashMap<>();
        showNotesData.put(SHOW_NOTES_KEY, showNotes);

        database.collection(USERS_PATH)
                .document(authUserUid)
                .collection(SHOWS_PATH)
                .document(showId)
                .set(showNotesData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onUpdateDatabaseUnsuccessful();
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
                        listener.onDeleteUserDataUnsuccessful();
                    }
                });
    }
}
