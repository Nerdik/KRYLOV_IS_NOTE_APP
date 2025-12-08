package com.example.krylov_is_note_app;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class DynamicThemeApplication extends Application {
    @Override
    public void onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this);
        super.onCreate();
    }
}
