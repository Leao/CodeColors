package io.leao.codecolors.core.widget;

import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

class WindowManagerWrapper implements WindowManager {
    private WindowManager mBaseWindowManager;

    public WindowManagerWrapper(WindowManager baseWindowManager) {
        mBaseWindowManager = baseWindowManager;
    }

    public WindowManager getBaseWindowManager() {
        return mBaseWindowManager;
    }

    @Override
    public Display getDefaultDisplay() {
        return mBaseWindowManager.getDefaultDisplay();
    }

    @Override
    public void removeViewImmediate(View view) {
        mBaseWindowManager.removeViewImmediate(view);
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams params) {
        mBaseWindowManager.addView(view, params);
    }

    @Override
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        mBaseWindowManager.updateViewLayout(view, params);
    }

    @Override
    public void removeView(View view) {
        mBaseWindowManager.removeView(view);
    }
}