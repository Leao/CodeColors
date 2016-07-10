package io.leao.codecolors.core.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.LongSparseArray;

import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.res.CcResources;

public class CcDrawableCache extends LongSparseArray<Drawable.ConstantState> {
    protected Resources mResources;
    protected String mPackageName;
    protected Set<Long> mCheckDependenciesKeys;

    public CcDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        mResources = context.getApplicationContext().getResources();
        mPackageName = context.getApplicationContext().getPackageName();

        if (cache != null) {
            mCheckDependenciesKeys = new HashSet<>(cache.size());

            int N = cache.size();
            for (int i = 0; i < N; i++) {
                long key = cache.keyAt(i);
                mCheckDependenciesKeys.add(key);
                put(key, cache.valueAt(i));
            }
        } else {
            mCheckDependenciesKeys = new HashSet<>(0);
        }
    }

    @Override
    public Drawable.ConstantState valueAt(int index) {
        /*
         * valueAt(int) is called by the system to iterate and validate the content of the preloaded drawables.
         * We return the default drawable, even if it is already wrapped, making sure everything works as expected.
         */
        Drawable.ConstantState cs = super.valueAt(index);
        if (cs instanceof CcDrawableWrapper.CcConstantState) {
            return ((CcDrawableWrapper.CcConstantState) cs).getBaseConstantState();
        } else {
            return cs;
        }
    }

    @Override
    public Drawable.ConstantState get(long key, Drawable.ConstantState valueIfKeyNotFound) {
        Drawable.ConstantState cs = super.get(key, null);
        if (cs == null) {
            int id = CcResources.getId(mResources, mPackageName, key);
            if (id != 0) {
                // Check if code-color.
                cs = onCreateColorDrawableConstantState(id, null);
                if (cs == null) {
                    // Check if code-color dependent.
                    cs = onCreateDrawableWrapperConstantState(id, valueIfKeyNotFound);
                }
            }
        } else if (mCheckDependenciesKeys.contains(key)) {
            // Each key only needs to be checked once for dependencies.
            mCheckDependenciesKeys.remove(key);
            // If the key has dependencies, replace its value with a wrapper.
            int id = CcResources.getId(mResources, mPackageName, key);
            if (id != 0) {
                cs = onCreateDrawableWrapperConstantState(id, cs);
                put(key, cs);
            }
        }
        return cs;
    }

    protected Drawable.ConstantState onCreateColorDrawableConstantState(int id, Drawable.ConstantState defaultValue) {
        CcColorStateList color = CcCore.getColorManager().getColor(id);
        if (color != null) {
            return CcColorDrawable.getConstantStateForColor(color);
        }
        return defaultValue;
    }

    protected Drawable.ConstantState onCreateDrawableWrapperConstantState(int id, Drawable.ConstantState defaultValue) {
        if (CcCore.getDependencyManager().hasDependencies(mResources, id)) {
            return new CcDrawableWrapper.CcConstantState(mResources, id);
        }
        return defaultValue;
    }
}
