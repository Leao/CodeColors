package io.leao.codecolors.core.drawable;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LongSparseArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.core.CcCore;

public class CcDrawableCache extends LongSparseArray<Drawable.ConstantState> {
    protected Resources mResources;
    protected String mPackageName;
    protected Set<Long> mOriginalKeys;

    public CcDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        mResources = context.getApplicationContext().getResources();
        mPackageName = context.getApplicationContext().getPackageName();

        if (cache != null) {
            mOriginalKeys = new HashSet<>(cache.size());

            int N = cache.size();
            for (int i = 0; i < N; i++) {
                long key = cache.keyAt(i);
                mOriginalKeys.add(key);
                put(key, cache.valueAt(i));
            }
        } else {
            mOriginalKeys = new HashSet<>(0);
        }
    }

    @Override
    public Drawable.ConstantState get(long key, Drawable.ConstantState valueIfKeyNotFound) {
        Drawable.ConstantState cs = super.get(key, valueIfKeyNotFound);
        if (cs == null) {
            cs = getConstantState(key, null);
            if (cs != null) {
                put(key, cs);
            }
        } else if (mOriginalKeys.contains(key)) {
            mOriginalKeys.remove(key);
            cs = getConstantState(key, cs);
            put(key, cs);
        }
        return cs;
    }

    private Drawable.ConstantState getConstantState(long key, Drawable.ConstantState cs) {
        int assetCookie = (int) (key >> 32);
        int data = (int) key;
        int id;
        try {
            id = retrieveId(mResources, mPackageName, assetCookie, data);
        } catch (Exception e) {
            id = 0;
        }

        if (id != 0) {
            CcDrawableWrapper.ConstantState wrapper = onCreateDrawableWrapperConstantState(id, cs);
            if (wrapper != null) {
                return wrapper;
            }
        }

        return cs;
    }

    protected CcDrawableWrapper.ConstantState onCreateDrawableWrapperConstantState(int id, Drawable.ConstantState cs) {
        if (CcCore.getDependenciesManager().hasDependencies(mResources, id)) {
            return new CcDrawableWrapper.CcConstantState(mResources, id);
        }
        return null;
    }

    public static int retrieveId(Resources resources, String appPackageName, int assetCookie, int data)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AssetManager assetManager = resources.getAssets();
        String getPooledStringMethodName =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? "getPooledString" : "getPooledStringForCookie";
        Method getPooledStringMethod =
                assetManager.getClass().getDeclaredMethod(getPooledStringMethodName, int.class, int.class);
        getPooledStringMethod.setAccessible(true);
        CharSequence pooledString = (CharSequence) getPooledStringMethod.invoke(assetManager, assetCookie, data);
        String file = pooledString.toString();
        int lastFileSeparator = file.lastIndexOf("/");
        String name = file.substring(lastFileSeparator + 1, file.indexOf("."));
        String defTypeWithModifiers =
                file.substring(file.substring(0, lastFileSeparator).lastIndexOf("/") + 1, lastFileSeparator);
        int modifierSeparator = defTypeWithModifiers.indexOf("-");
        String defType =
                modifierSeparator != -1 ? defTypeWithModifiers.substring(0, modifierSeparator) : defTypeWithModifiers;
        String packageName = assetCookie == 1 ? "android" : appPackageName;
        return resources.getIdentifier(name, defType, packageName);
    }
}