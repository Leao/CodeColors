package io.leao.codecolors.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import io.leao.codecolors.callback.CcDrawableCallback;
import io.leao.codecolors.res.CcColorStateList;

public abstract class CcDrawableAttrCallbackAdapter implements CcAttrCallbackAdapter<Drawable> {
    @NonNull
    @Override
    public CcColorStateList.AnchorCallback<Drawable> getAnchorCallback() {
        return new CcDrawableCallback();
    }
}
