package com.github.rstockbridge.showstats;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public final class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
