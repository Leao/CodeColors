package io.leao.codecolors.core.color;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.LongSparseArray;

import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.res.CcResources;

public class CcColorCache extends LongSparseArray {
    protected Resources mResources;
    protected String mPackageName;

    protected Set<Long> mCheckDependenciesKeys;

    public CcColorCache(Context context, LongSparseArray cache) {
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
    public Object get(long key, Object valueIfKeyNotFound) {
        // 'value' might be a ColorStateList or a ColorStateList.ConstantState (its factory).
        // If depends on the Android versions.
        Object value = super.get(key, null);

        if (value == null) {
            int id = CcResources.getId(mResources, mPackageName, key);
            if (id != 0) {
                // Check if code-color.
                value = onCreateCodeColor(id, null);
                if (value == null) {
                    // Check if code-color dependent.
                    value = onCreateColorStateList(id, valueIfKeyNotFound);
                }
            }
        } else if (mCheckDependenciesKeys.contains(key)) {
            // Each key only needs to be checked once for dependencies.
            mCheckDependenciesKeys.remove(key);
            // If the key has dependencies, override ColoStateListFactory mSrc ColorStateList.
            int id = CcResources.getId(mResources, mPackageName, key);
            if (id != 0) {
                value = onCreateColorStateList(id, value);
                put(key, value);
            }
        }
        return value;
    }

    protected Object onCreateCodeColor(int id, Object defaultValue) {
        ColorStateList color = CcCore.getColorManager().getColor(id);

        return color != null ? color : defaultValue;
    }

    protected Object onCreateColorStateList(int id, Object defaultValue) {
        if (CcCore.getDependencyManager().hasDependencies(mResources, id)) {
            return CcResources.loadColorStateList(mResources, id, null);
        }
        return defaultValue;
    }
}
