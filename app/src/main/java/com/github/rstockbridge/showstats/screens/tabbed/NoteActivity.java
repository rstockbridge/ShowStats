package com.github.rstockbridge.showstats.screens.tabbed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.google.android.material.textfield.TextInputLayout;

import timber.log.Timber;

public final class NoteActivity
        extends AppCompatActivity
        implements
        View.OnClickListener,
        DatabaseHelper.ShowNoteListener,
        DatabaseHelper.UpdateDatabaseListener {

    private static final String EXTRA_SHOW_ID = "showId";

    @NonNull
    public static Intent newIntent(@NonNull final Context context, @NonNull final String showId) {
        final Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra(EXTRA_SHOW_ID, showId);
        return intent;
    }

    private AuthHelper authHelper;

    private DatabaseHelper databaseHelper;

    private String showId;

    private RelativeLayout noteLayout;

    private TextView displayedNoteView;
    private TextInputLayout editNoteLayout;
    private EditText editNoteView;

    private Button editNoteButton;
    private Button saveNoteButton;

    private boolean setDisplayedNoteVisible;

    private ProgressBar progressBar;

    private boolean databaseCallIsInProgress;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        authHelper = new AuthHelper(this);
        databaseHelper = new DatabaseHelper();

        showId = getIntent().getStringExtra(EXTRA_SHOW_ID);

        initializeUI();

        setDatabaseCallInProgress(true);
        databaseHelper.getShowNote(authHelper.getCurrentUserUid(), showId, this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {

            case R.id.edit_note_button:
                final String displayedNoteText = displayedNoteView.getText().toString();
                if (!displayedNoteText.equals(getString(R.string.no_note_saved))) {
                    setEditNoteText(displayedNoteView.getText().toString());
                }
                setDisplayedNoteVisible = false;
                syncUI();
                break;

            case R.id.save_note_button:
                saveToDatabase();
                break;

            case R.id.exit_button:
                finish();
                break;

            default:
                throw new IllegalStateException("This line should never be reached.");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI() {
        noteLayout = findViewById(R.id.note_layout);

        displayedNoteView = findViewById(R.id.displayed_note_view);
        editNoteLayout = findViewById(R.id.edit_note_layout);
        editNoteView = findViewById(R.id.edit_note_view);
        editNoteButton = findViewById(R.id.edit_note_button);
        saveNoteButton = findViewById(R.id.save_note_button);
        final Button exitButton = findViewById(R.id.exit_button);

        editNoteView.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editNoteView.getRight() - editNoteView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    editNoteView.setText(getString(R.string.empty_string));
                    setDisplayedNoteText(getString(R.string.empty_string));
                    return true;
                }
            }
            return false;
        });

        editNoteView.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveToDatabase();
                handled = true;
            }
            return handled;
        });

        editNoteButton.setOnClickListener(this);
        saveNoteButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        progressBar = findViewById(R.id.progress_bar);
    }

    private void setDatabaseCallInProgress(final boolean inProgress) {
        databaseCallIsInProgress = inProgress;
        syncUI();
    }

    private void setDisplayedNoteText(@NonNull final String text) {
        displayedNoteView.setText(text);
    }

    private void setEditNoteText(@NonNull final String text) {
        editNoteView.setText(text);
        editNoteView.setSelection(editNoteView.getText().length());
    }

    private void syncUI() {
        syncViewsWithDatabaseCall(databaseCallIsInProgress);

        if (setDisplayedNoteVisible) {
            displayedNoteView.setVisibility(View.VISIBLE);
            editNoteLayout.setVisibility(View.INVISIBLE);

            editNoteButton.setEnabled(true);
            saveNoteButton.setEnabled(false);
        } else {
            displayedNoteView.setVisibility(View.INVISIBLE);
            editNoteLayout.setVisibility(View.VISIBLE);

            editNoteButton.setEnabled(false);
            saveNoteButton.setEnabled(true);

        }
    }

    private void syncViewsWithDatabaseCall(final boolean makeVisible) {
        progressBar.setVisibility(makeVisible ? View.VISIBLE : View.INVISIBLE);
        noteLayout.setVisibility(makeVisible ? View.INVISIBLE : View.VISIBLE);
    }

    private void saveToDatabase() {
        databaseHelper.updateShowNoteInDatabase(
                authHelper.getCurrentUserUid(),
                showId,
                editNoteView.getText().toString(),
                this);
    }

    @Override
    public void onGetShowNoteCompleted(@Nullable final String text) {
        if (text != null) {
            setDisplayedNoteText(text);
        } else {
            setDisplayedNoteText(getString(R.string.no_note_saved));
        }

        setDisplayedNoteVisible = true;
        setDatabaseCallInProgress(false);
    }

    @Override
    public void onUpdateDatabaseSuccessful() {
        final String editNoteText = editNoteView.getText().toString();
        if (editNoteText.equals(getString(R.string.empty_string))) {
            setDisplayedNoteText(getString(R.string.no_note_saved));
        } else {
            setDisplayedNoteText(editNoteView.getText().toString());
        }
        setDisplayedNoteVisible = true;
        syncUI();

        MessageUtil.makeToast(this, "Data updated!");
    }

    @Override
    public void onUpdateDatabaseUnsuccessful(@Nullable final Exception e) {
        if (e != null) {
            Timber.e(e, "Error updating Firebase data!");
        }

        MessageUtil.makeToast(this, "Could not update data!");
    }
}
