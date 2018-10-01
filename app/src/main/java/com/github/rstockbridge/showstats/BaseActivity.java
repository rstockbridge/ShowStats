package com.github.rstockbridge.showstats;

import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

abstract class BaseActivity extends AppCompatActivity {

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        onCreateSpecializedOptionsMenu(menu, inflater); // Insert subclass-specific entries first.
        inflater.inflate(R.menu.menu_open_source, menu);
        return true;
    }

    @CallSuper
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.licenses) {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onCreateSpecializedOptionsMenu(
            @NonNull final Menu menu,
            @NonNull final MenuInflater inflater) {

        // This method intentionally left blank.
    }

}
