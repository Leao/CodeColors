package io.leao.codecolors.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;

import io.leao.codecolors.R;
import io.leao.codecolors.callback.CcInvalidateDrawableCallback;

public class CcBackgroundTintAttrCallbackAdapter implements CcAttrCallbackAdapter<Drawable> {
    @Override
    public boolean onCache(CacheResult<Drawable> outResult) {
        outResult.set(new int[]{R.attr.backgroundTint}, new CcInvalidateDrawableCallback());
        return true;
    }

    @Override
    public boolean onInflate(View view, int attr, InflateResult<Drawable> outResult) {
        outResult.set(view.getBackground());
        return true;
    }
}
