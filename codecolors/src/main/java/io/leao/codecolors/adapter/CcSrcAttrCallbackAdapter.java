package io.leao.codecolors.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

public class CcSrcAttrCallbackAdapter extends CcDrawableAttrCallbackAdapter {
    @NonNull
    @Override
    public int[] getAttrs() {
        return new int[]{android.R.attr.src};
    }

    @Override
    public Drawable getAnchor(View view, int attr) {
        if (view instanceof ImageView) {
            return ((ImageView) view).getDrawable();
        } else {
            return null;
        }
    }
}
