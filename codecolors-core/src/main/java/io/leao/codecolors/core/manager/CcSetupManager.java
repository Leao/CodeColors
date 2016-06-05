package io.leao.codecolors.core.manager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LongSparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.adapter.CcAdapterManager;
import io.leao.codecolors.core.adapter.sample.CcTextColorsColorCallbackAdapter;
import io.leao.codecolors.core.color.CcColorManager;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.drawable.CcColorDrawable;
import io.leao.codecolors.core.drawable.CcDrawableCache;
import io.leao.codecolors.core.res.CcResources;

public class CcSetupManager {
    private static final String LOG_TAG = CcSetupManager.class.getSimpleName();

    private static boolean sIsActive = false;

    @SuppressWarnings("unchecked")
    public void setup(Context context, boolean useDefaultCallbackAdapters, Callback callback) {
        try {
            String packageName = context.getPackageName();
            // Initialize colors.
            CcColorManager colorsManager = CcCore.getColorsManager();
            colorsManager.init(packageName);
            // Initialize dependencies.
            CcCore.getDependenciesManager().init(packageName);
            // Configure colors and dependencies for current configuration.
            CcCore.getConfigurationManager().onConfigurationChanged(context.getResources());

            Resources resources = context.getResources();
            // The SparseArray that holds the entries of preloaded colors.
            Field sPreloadedColorStateListsField = Resources.class.getDeclaredField("sPreloadedColorStateLists");
            sPreloadedColorStateListsField.setAccessible(true);
            LongSparseArray sPreloadedColorStateLists = (LongSparseArray) sPreloadedColorStateListsField.get(null);
            // The getter method to retrieve the constant state, or null.
            // If sPreloadedColorStateListsField type is not supported,
            // getColorsConstantStateGetter throws an exception.
            Method colorsConstantStateGetter = getColorsConstantStateGetter(sPreloadedColorStateListsField);

            // The SparseArray (or array) that holds the entries of preloaded drawables.
            Field sPreloadedDrawablesField = Resources.class.getDeclaredField("sPreloadedDrawables");
            sPreloadedDrawablesField.setAccessible(true);
            Object sPreloadedDrawables = sPreloadedDrawablesField.get(null);
            // Whether to layout orientation to cache drawables.
            // If sPreloadedDrawables type is not supported, useLayoutDirectionDrawableCache throws an exception.
            boolean useLayoutDirectionDrawableCache = useLayoutDirectionDrawableCache(sPreloadedDrawables);
            if (useLayoutDirectionDrawableCache) {
                sPreloadedDrawables = new CcDrawableCache[]{
                        onCreateDrawableCache(context, ((LongSparseArray[]) sPreloadedDrawables)[0]),
                        onCreateDrawableCache(context, ((LongSparseArray[]) sPreloadedDrawables)[1])};
            } else {
                sPreloadedDrawables = onCreateDrawableCache(context, (LongSparseArray) sPreloadedDrawables);
            }
            // Override preloaded drawables value.
            sPreloadedDrawablesField.set(null, sPreloadedDrawables);

            for (int colorResId : colorsManager.getColors()) {
                CcColorStateList color = colorsManager.getColor(colorResId);

                long key = CcResources.createKey(resources, colorResId);

                // Load color into Resources cache.
                if (colorsConstantStateGetter != null) {
                    sPreloadedColorStateLists.put(key, colorsConstantStateGetter.invoke(color));
                } else {
                    sPreloadedColorStateLists.put(key, color);
                }

                // Load drawable into Resources cache.
                Drawable.ConstantState drawableConstantState = CcColorDrawable.getConstantStateForColor(color);
                if (useLayoutDirectionDrawableCache) {
                    // Load for both layout directions (LTR and RTL).
                    ((LongSparseArray[]) sPreloadedDrawables)[0].put(key, drawableConstantState);
                    ((LongSparseArray[]) sPreloadedDrawables)[1].put(key, drawableConstantState);
                } else {
                    ((LongSparseArray) sPreloadedDrawables).put(key, drawableConstantState);
                }
            }
        } catch (Exception e) {
            onCodeColorsSetupFailure(e, callback);

            return; // Finish without setting code colors active.
        }

        // Code colors successfully injected.
        sIsActive = true;

        onCodeColorsSetupSuccess(useDefaultCallbackAdapters, callback);
    }

    protected CcDrawableCache onCreateDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        return new CcDrawableCache(context, cache);
    }

    protected void onCodeColorsSetupSuccess(boolean useDefaultCallbackAdapters, Callback callback) {
        // Setup default callback adapters, if desired.
        if (useDefaultCallbackAdapters) {
            onAddDefaultCallbackAdapters(CcCore.getAdapterManager());
        }

        if (callback != null) {
            callback.onCodeColorsSetupSuccess();
        }
    }

    protected void onAddDefaultCallbackAdapters(CcAdapterManager adapterManager) {
        adapterManager.addColorCallbackAdapter(new CcTextColorsColorCallbackAdapter());
    }

    protected void onCodeColorsSetupFailure(Exception e, Callback callback) {
        Log.w(LOG_TAG, "ColorStateList preload failed. Dynamic colors will not work.", e);

        if (callback != null) {
            callback.onCodeColorsSetupFailure(e);
        }
    }

    protected static Method getColorsConstantStateGetter(Field preloadedColorStateListsField)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> argumentClass = getFirstArgumentClass(preloadedColorStateListsField);
        if (argumentClass == ColorStateList.class) {
            // Each entry in preloadedColorStateLists is a (long, ColorStateList).
            // Usually in Build.VERSION.SDK_INT < Build.VERSION_CODES.M.
            return null;
        } else if (argumentClass == Class.forName("android.content.res.ConstantState")) {
            // Each entry in preloadedColorStateLists is a (long, ConstantState<ColorStateList>).
            // Usually in Build.VERSION.SDK_INT >= Build.VERSION_CODES.M.
            return ColorStateList.class.getMethod("getConstantState");

        } else {
            throw new IllegalStateException("Unsupported color cache.");
        }
    }

    protected static Class<?> getFirstArgumentClass(Field field) {
        ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
        Type firstArgumentType = fieldType.getActualTypeArguments()[0];
        if (firstArgumentType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) firstArgumentType).getRawType();
        } else {
            return (Class<?>) firstArgumentType;
        }
    }

    protected static boolean useLayoutDirectionDrawableCache(Object preloadedDrawables) {
        if (preloadedDrawables instanceof LongSparseArray[]) {
            // Usually in Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1.
            return true;
        } else if (preloadedDrawables instanceof LongSparseArray) {
            // Usually in Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1.
            return false;
        } else {
            throw new IllegalStateException("Unsupported drawable cache.");
        }
    }

    public boolean isActive() {
        return sIsActive;
    }

    public interface Callback {
        void onCodeColorsSetupSuccess();

        void onCodeColorsSetupFailure(Exception e);
    }
}
