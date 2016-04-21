package io.leao.codecolors.core.callback;

import android.view.View;

import io.leao.codecolors.core.res.CcColorStateList;

public class CcRefreshDrawableStateCallback<T extends View> implements CcColorStateList.AnchorCallback<T> {
    @Override
    public void invalidateColor(T anchor, CcColorStateList color) {
        anchor.refreshDrawableState();
    }
}
