package com.github.rstockbridge.showstats.screens;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.auth.AuthHelper;

public final class DeleteDialogFragment extends DialogFragment {

    public interface NetworkCallListener {
        void updateUiForDeletionInProgress();
    }

    private AuthHelper authHelper;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        authHelper = new AuthHelper(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AuthHelper.RevokeAccessListener revokeAccessListener;
        final NetworkCallListener networkCallListener;

        try {
            revokeAccessListener = (AuthHelper.RevokeAccessListener) getActivity();
        } catch (final ClassCastException e) {
            throw new IllegalStateException("Hosting context must implement RevokeAccessListener", e);
        }

        try {
            networkCallListener = (NetworkCallListener) getActivity();
        } catch (final ClassCastException e) {
            throw new IllegalStateException("Hosting context must implement RevokeAccessListener", e);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.confirm_delete))
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    authHelper.revokeAccountAccess(revokeAccessListener);
                    networkCallListener.updateUiForDeletionInProgress();
                })
                .setNegativeButton(getString(R.string.no), (dialog, id) -> {
                });

        return builder.create();
    }
}
