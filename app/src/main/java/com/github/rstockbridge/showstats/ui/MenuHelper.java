package com.github.rstockbridge.showstats.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.auth.AuthHelper;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public final class MenuHelper {

    private Activity activity;
    private AuthHelper authHelper;

    public MenuHelper(@NonNull final Activity activity) {
        this.activity = activity;
        authHelper = new AuthHelper(this.activity);
    }

    public final boolean onCreateLicensesPrivacyMenu(@NonNull final Activity activity, final Menu menu) {
        final MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.menu_legal, menu);
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
                authHelper.revokeAccountAccess(revokeAccessListener);
                return true;
            default:
                throw new IllegalStateException("This line should not be reached.");
        }
    }
}
