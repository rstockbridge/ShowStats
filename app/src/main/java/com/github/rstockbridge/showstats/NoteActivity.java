package com.github.rstockbridge.showstats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.database.DatabaseHelper;
import com.github.rstockbridge.showstats.ui.MessageUtil;

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

    @NonNull
    private AuthHelper authHelper;

    @NonNull
    private DatabaseHelper databaseHelper;

    private String showId;

    private EditText editText;
    private Button editTextButton;
    private Button saveButton;

    private boolean editTextButtonEnabled;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        authHelper = new AuthHelper(this);
        databaseHelper = new DatabaseHelper();

        showId = getIntent().getStringExtra(EXTRA_SHOW_ID);

        initializeUI();

        databaseHelper.getShowNote(authHelper.getCurrentUserUid(), showId, this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {

            case R.id.clear_button:
                editTextButtonEnabled = false;
                syncUI();
                break;

            case R.id.go_button:
                editTextButtonEnabled = true;
                syncUI();
                saveToDatabase();
                break;

            case R.id.exit_button:
                finish();
                break;

            default:
                throw new IllegalStateException("This line should never be reached.");
        }
    }

    @Override
    protected void onDestroy() {
        authHelper.clearAuthListener();
        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI() {
        editText = findViewById(R.id.edit_notes_text);
        editTextButton = findViewById(R.id.clear_button);
        saveButton = findViewById(R.id.go_button);

        final Button exitButton = findViewById(R.id.exit_button);

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        editText.setText("");
                        return true;
                    }
                }
                return false;
            }

        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editTextButtonEnabled = true;
                    syncUI();

                    saveToDatabase();
                    handled = true;
                }
                return handled;
            }
        });

        editTextButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        editTextButtonEnabled = true;
        syncUI();
    }

    private void syncUI() {
        if (editTextButtonEnabled) {
            editText.setEnabled(false);
            editTextButton.setEnabled(true);
            saveButton.setEnabled(false);
        } else {
            editText.setEnabled(true);
            editTextButton.setEnabled(false);
            saveButton.setEnabled(true);
        }
    }

    private void saveToDatabase() {
        databaseHelper.updateShowNoteInDatabase(
                authHelper.getCurrentUserUid(),
                showId,
                editText.getText().toString(),
                this);
    }

    @Override
    public void onShowNote(final String text) {
        editText.setText(text);
        editText.setEnabled(false);
    }

    @Override
    public void onUpdateDatabaseUnsuccessful() {
        MessageUtil.makeToast(this, "Could not update show note!");
    }
}
