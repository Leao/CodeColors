package io.leao.codecolors.sample;

import android.app.Application;
import android.util.Log;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.sample.inflate.CcCoordinatorLayoutDefStyleAdapter;
import io.leao.codecolors.sample.inflate.CcStatusBarColorAnchorCallbackAdapter;
import io.leao.codecolors.sample.color.CyclerColorAdapter;

public class CodeColorsSample extends Application {
    private static final String LOG_TAG = CodeColorsSample.class.getSimpleName();

    private static CodeColorsSample sInstance;

    public static CodeColorsSample getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        sInstance = this;

        super.onCreate();

        long time = System.currentTimeMillis();

        // Activate code colors.
        CodeColors.start(this, new CodeColors.Callback() {
            @Override
            public void onCodeColorsStarted() {
                CodeColors.setColorAdapter(new CyclerColorAdapter());
                CodeColors.addAttrCallbackAdapter(new CcStatusBarColorAnchorCallbackAdapter());
                CodeColors.addViewDefStyleAdapter(new CcCoordinatorLayoutDefStyleAdapter());
            }

            @Override
            public void onCodeColorsFailed(Exception e) {
                // Do nothing.
            }
        });

        if (CodeColors.isActive()) {
            Log.i(LOG_TAG, "CodeColors started in " + (System.currentTimeMillis() - time) + "ms.");
        } else {
            Log.i(LOG_TAG, "CodeColors failed.");
        }
    }
}
