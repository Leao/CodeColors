package io.leao.codecolors.core.callback;

import android.view.View;

import java.util.Set;

import io.leao.codecolors.core.color.CodeColor;

public class CcRefreshDrawableStateCallback<T extends View> implements CodeColor.AnchorCallback<T> {
    @Override
    public void invalidateColor(T anchor, CodeColor color) {
        refreshDrawableState(anchor);
    }

    @Override
    public <U extends CodeColor> void invalidateColors(T anchor, Set<U> colors) {
        refreshDrawableState(anchor);
    }

    private void refreshDrawableState(T anchor) {
        anchor.refreshDrawableState();
    }
}
