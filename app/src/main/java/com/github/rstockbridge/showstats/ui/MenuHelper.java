package com.github.rstockbridge.showstats.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.github.rstockbridge.showstats.BuildConfig;
import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.github.rstockbridge.showstats.screens.DeleteDialogFragment;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public final class MenuHelper {

    private FragmentActivity activity;
    private AuthHelper authHelper;

    public MenuHelper(@NonNull final FragmentActivity activity) {
        this.activity = activity;
        authHelper = new AuthHelper(this.activity);
    }

    public final boolean onCreateLicensesPrivacyMenu(@NonNull final Activity activity, final Menu menu) {
        final MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.menu_legal, menu);

        if (BuildConfig.ALLOW_FORCE_CRASH) {
            addForceCrashButton(menu);
        }

        return true;
    }

    public final boolean onCreateAccountMenu(@NonNull final Activity activity, final Menu menu) {
        final MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.menu_account, menu);
        return true;
    }

    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.licenses:
                return onLicensesOrPrivacyItemSelected(activity, item);
            case R.id.privacy:
                return onLicensesOrPrivacyItemSelected(activity, item);
            case R.id.sign_out:
                return onAccountItemSelected(activity, item);
            case R.id.delete_account:
                return onAccountItemSelected(activity, item);
            default:
                throw new IllegalStateException("This line should not be reached.");

        }
    }

    private boolean onLicensesOrPrivacyItemSelected(@NonNull final Activity activity, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.licenses:
                activity.startActivity(new Intent(activity, OssLicensesMenuActivity.class));
                return true;
            case R.id.privacy:
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.privacy_policy_url))));
                return true;
            default:
                throw new IllegalStateException("This line should not be reached.");
        }
    }

    private boolean onAccountItemSelected(@NonNull final Context context, final MenuItem item) {
        AuthHelper.SignOutListener signOutListener;
        AuthHelper.RevokeAccessListener revokeAccessListener;

        try {
            signOutListener = (AuthHelper.SignOutListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AuthHelper.SignOutListener");
        }

        try {
            revokeAccessListener = (AuthHelper.RevokeAccessListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AuthHelper.RevokeAccessListener");
        }

        switch (item.getItemId()) {
            case R.id.sign_out:
                authHelper.signOut(signOutListener);
                return true;
            case R.id.delete_account:
                showDeleteDialog();
//                authHelper.revokeAccountAccess(revokeAccessListener);
                return true;
            default:
                throw new IllegalStateException("This line should not be reached.");
        }
    }

    private void addForceCrashButton(final Menu menu) {
        final MenuItem menuItem = menu.add(R.string.force_crash);
        menuItem.setOnMenuItemClickListener(item -> {
            throw new RuntimeException("Test Crash");
        });
    }

    private void showDeleteDialog() {
        final DialogFragment newFragment = new DeleteDialogFragment();
        newFragment.show(activity.getSupportFragmentManager(), "dialog");
    }
}
