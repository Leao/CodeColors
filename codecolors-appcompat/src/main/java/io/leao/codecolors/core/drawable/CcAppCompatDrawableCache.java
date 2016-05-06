package io.leao.codecolors.core.drawable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LongSparseArray;

import io.leao.codecolors.appcompat.tint.CcTintManager;

public class CcAppCompatDrawableCache extends CcDrawableCache {

    public CcAppCompatDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        super(context, cache);
    }

    @Override
    protected CcDrawableWrapper.CcConstantState onCreateDrawableWrapperConstantState(int id) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int[] attrs = CcTintManager.getAttrs(id);
            if (attrs != null) {
                return new CcAppCompatDrawableWrapper.CcAppCompatConstantState(mResources, id, attrs);
            }
        }
        return super.onCreateDrawableWrapperConstantState(id);
    }
}
