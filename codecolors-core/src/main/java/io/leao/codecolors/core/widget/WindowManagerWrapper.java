package io.leao.codecolors.core.widget;

import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

class WindowManagerWrapper implements WindowManager {
    private WindowManager mWindowManager;

    public WindowManagerWrapper(WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    public WindowManager getBaseWindowManager() {
        return mWindowManager;
    }

    @Override
    public Display getDefaultDisplay() {
        return mWindowManager.getDefaultDisplay();
    }

    @Override
    public void removeViewImmediate(View view) {
        mWindowManager.removeViewImmediate(view);
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams params) {
        mWindowManager.addView(view, params);
    }

    @Override
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        mWindowManager.updateViewLayout(view, params);
    }

    @Override
    public void removeView(View view) {
        mWindowManager.removeView(view);
    }
}