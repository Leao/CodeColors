package io.leao.codecolors.sample;

import android.app.Application;
import android.util.Log;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.sample.adapter.CcCoordinatorLayoutDefStyleAdapter;
import io.leao.codecolors.sample.adapter.CcStatusBarColorAnchorCallbackAdapter;

public class CodeColorsSample extends Application {
    private static final String LOG_TAG = CodeColorsSample.class.getSimpleName();

    public void onCreate() {
        super.onCreate();

        long time = System.currentTimeMillis();

        // Activate code colors.
        CodeColors.init(this, new CodeColors.Callback() {
            @Override
            public void onCodeColorsInitSuccess() {
                CodeColors.addAttrCallbackAdapter(new CcStatusBarColorAnchorCallbackAdapter());
                CodeColors.addViewDefStyleAdapter(new CcCoordinatorLayoutDefStyleAdapter());
            }

            @Override
            public void onCodeColorsInitFailure() {
                // Do nothing.
            }
        });

        if (CodeColors.isActive()) {
            Log.i(LOG_TAG, "CodeColors active in " + (System.currentTimeMillis() - time) + "ms.");
        } else {
            Log.i(LOG_TAG, "CodeColors activation failed.");
        }
    }
}
