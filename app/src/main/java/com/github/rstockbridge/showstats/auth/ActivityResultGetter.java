package com.github.rstockbridge.showstats.auth;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ActivityResultGetter {

    interface OnActivityResultListener {
        void onActivityResult(@Nullable Intent data);
    }

    // Is implemented by Activity by default.
    void startActivityForResult(Intent intent, int resultCode);

    void setOnActivityResultListener(int resultCode, @NonNull OnActivityResultListener listener);

    void removeOnActivityResultListener(int resultCode);
}
