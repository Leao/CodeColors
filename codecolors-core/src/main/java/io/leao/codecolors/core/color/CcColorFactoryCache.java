package io.leao.codecolors.core.color;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.LongSparseArray;

/**
 * Same as {@link CcColorCache} but instead of returning {@link ColorStateList}s, returns their ConstantState, meaning,
 * their factories.
 */
public class CcColorFactoryCache extends CcColorCache {

    public CcColorFactoryCache(Context context, LongSparseArray cache) {
        super(context, cache);
    }

    protected Object onCreateCodeColor(int id, Object defaultValue) {
        Object color = super.onCreateCodeColor(id, null);
        if (color != null) {
            return ColorStateListUtils.getConstantState((ColorStateList) color);
        }

        return defaultValue;
    }

    protected Object onCreateColorStateList(int id, Object defaultValue) {
        Object color = super.onCreateColorStateList(id, null);
        if (color != null) {
            return ColorStateListUtils.getConstantState((ColorStateList) color);
        }

        return defaultValue;
    }
}
