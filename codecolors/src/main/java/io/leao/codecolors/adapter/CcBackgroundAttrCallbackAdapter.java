package io.leao.codecolors.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.R;

public class CcBackgroundAttrCallbackAdapter extends CcDrawableAttrCallbackAdapter {
    @NonNull
    @Override
    public int[] getAttrs() {
        return new int[]{android.R.attr.background, R.attr.backgroundTint};
    }

    @NonNull
    @Override
    public Drawable getAnchor(View view, int attr) {
        return view.getBackground();
    }
}
