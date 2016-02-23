package io.leao.codecolors.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.R;
import io.leao.codecolors.callback.CcDrawableCallback;
import io.leao.codecolors.res.CcColorStateList;

public class CcBackgroundTintAttrCallbackAdapter implements CcAttrCallbackAdapter<Drawable> {
    @NonNull
    @Override
    public CcColorStateList.AnchorCallback<Drawable> getAnchorCallback() {
        return new CcDrawableCallback();
    }

    @NonNull
    @Override
    public int[] getAttrs() {
        return new int[]{R.attr.backgroundTint};
    }

    @NonNull
    @Override
    public Drawable getAnchor(View view, int attr) {
        return view.getBackground();
    }
}
