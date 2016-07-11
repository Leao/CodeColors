package io.leao.codecolors.sample;

import android.app.Application;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.sample.color.CyclerColorAdapter;
import io.leao.codecolors.sample.inflate.CcCoordinatorLayoutDefStyleAdapter;
import io.leao.codecolors.sample.inflate.CcStatusBarBackgroundAttrCallbackAdapter;

public class CodeColorsSample extends Application {
    private static CodeColorsSample sInstance;

    public static CodeColorsSample getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        sInstance = this;

        super.onCreate();

        // Activate code colors.
        CodeColors.start(this, new CodeColors.Callback() {
            @Override
            public void onCodeColorsStarted() {
                CodeColors.setColorAdapter(new CyclerColorAdapter());
                CodeColors.addAttrCallbackAdapter(new CcStatusBarBackgroundAttrCallbackAdapter());
                CodeColors.addViewDefStyleAdapter(new CcCoordinatorLayoutDefStyleAdapter());
            }

            @Override
            public void onCodeColorsFailed(Exception e) {
                // Do nothing.
            }
        });
    }
}
