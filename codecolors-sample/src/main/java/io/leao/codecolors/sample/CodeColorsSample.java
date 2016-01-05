package io.leao.codecolors.sample;

import android.app.Application;
import android.util.Log;

import io.leao.codecolors.CodeColors;

public class CodeColorsSample extends Application {
    private static final String LOG_TAG = CodeColorsSample.class.getSimpleName();

    public void onCreate() {
        super.onCreate();

        long time = System.currentTimeMillis();

        // Activate code colors.
        CodeColors.init(this);

        if (CodeColors.isActive()) {
            Log.i(LOG_TAG, "CodeColors active in " + (System.currentTimeMillis() - time) + "ms.");
        } else {
            Log.i(LOG_TAG, "CodeColors activation failed.");
        }
    }
}
