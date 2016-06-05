package io.leao.codecolors.core.callback.sample;

import android.view.View;

import java.util.Set;

import io.leao.codecolors.core.color.CcColorStateList;

public class CcRefreshDrawableStateCallback<T extends View> implements CcColorStateList.AnchorCallback<T> {
    @Override
    public void invalidateColor(T anchor, CcColorStateList color) {
        refreshDrawableState(anchor);
    }

    @Override
    public void invalidateColors(T anchor, Set<CcColorStateList> colors) {
        refreshDrawableState(anchor);
    }

    private void refreshDrawableState(T anchor) {
        anchor.refreshDrawableState();
    }
}
