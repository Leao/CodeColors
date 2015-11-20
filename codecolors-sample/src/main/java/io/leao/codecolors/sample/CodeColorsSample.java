package io.leao.codecolors.sample;

import android.app.Application;
import android.util.Log;

import io.leao.codecolors.CodeColors;

public class CodeColorsSample extends Application {
    private static final String LOG_TAG = CodeColorsSample.class.getSimpleName();

    public void onCreate() {
        super.onCreate();

        CodeColors.init(this);
        Log.i(LOG_TAG, "CodeColors active: " + CodeColors.isActive());
    }
}
