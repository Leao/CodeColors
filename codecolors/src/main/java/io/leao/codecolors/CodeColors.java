package io.leao.codecolors;

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

import io.leao.codecolors.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.adapter.CcBackgroundTintAttrCallbackAdapter;
import io.leao.codecolors.adapter.CcTextColorsViewCallbackAdapter;
import io.leao.codecolors.adapter.CcTintableBackgroundViewCallbackAdapter;
import io.leao.codecolors.adapter.CcViewCallbackAdapter;
import io.leao.codecolors.adapter.CcViewDefStyleAdapter;
import io.leao.codecolors.drawable.CcColorDrawable;
import io.leao.codecolors.drawable.CcDrawableCache;
import io.leao.codecolors.manager.CcCallbackManager;
import io.leao.codecolors.manager.CcColorsManager;
import io.leao.codecolors.manager.CcConfigurationManager;
import io.leao.codecolors.manager.CcDependenciesManager;
import io.leao.codecolors.res.CcColorStateList;
import io.leao.codecolors.res.CcResources;

public abstract class CodeColors {
    private static final String LOG_TAG = CodeColors.class.getSimpleName();

    private static boolean sIsActive = false;

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, Callback callback) {
        init(context, true, callback);
    }

    @SuppressWarnings("unchecked")
    public static void init(Context context, boolean useDefaultCallbackAdapters, Callback callback) {
        try {
            String packageName = context.getPackageName();
            // Initialize colors.
            CcColorsManager colorsManager = CcColorsManager.getInstance();
            colorsManager.init(packageName);
            // Initialize dependencies.
            CcDependenciesManager.getInstance().init(packageName);
            // Configure colors and dependencies for current configuration.
            CcConfigurationManager.getInstance().onConfigurationChanged(context.getResources());

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
                        new CcDrawableCache(context, ((LongSparseArray[]) sPreloadedDrawables)[0]),
                        new CcDrawableCache(context, ((LongSparseArray[]) sPreloadedDrawables)[1])};
            } else {
                sPreloadedDrawables = new CcDrawableCache(context, (LongSparseArray) sPreloadedDrawables);
            }
            // Override preloaded drawables value.
            sPreloadedDrawablesField.set(null, sPreloadedDrawables);

            for (Integer colorResId : colorsManager.getColors()) {
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
            onCodeColorsInitFailure(e, callback);

            return; // Finish without setting code colors active.
        }

        // Code colors successfully injected.
        sIsActive = true;

        onCodeColorsInitSuccess(useDefaultCallbackAdapters, callback);
    }

    protected static void onCodeColorsInitSuccess(boolean useDefaultCallbackAdapters, Callback callback) {
        // Setup default callback adapters, if desired.
        if (useDefaultCallbackAdapters) {
            CcCallbackManager callbackManager = CcCallbackManager.getInstance();
            callbackManager.addAttrCallbackAdapter(new CcBackgroundTintAttrCallbackAdapter());
            callbackManager.addViewCallbackAdapter(new CcTextColorsViewCallbackAdapter());
            callbackManager.addViewCallbackAdapter(new CcTintableBackgroundViewCallbackAdapter());
        }

        if (callback != null) {
            callback.onCodeColorsInitSuccess();
        }
    }

    protected static void onCodeColorsInitFailure(Exception e, Callback callback) {
        Log.w(LOG_TAG, "ColorStateList preload failed. Dynamic colors will not work.", e);

        if (callback != null) {
            callback.onCodeColorsInitFailure();
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

    /**
     * @return {@code true} if {@link #init(Context)} completed successfully; {@code false}, otherwise.
     */
    public static boolean isActive() {
        return sIsActive;
    }

    public static CcColorStateList getColor(int resId) {
        return CcColorsManager.getInstance().getColor(resId);
    }

    public static void setColor(int resId, int color) {
        CcColorsManager.getInstance().setColor(resId, color);
    }

    public static void setColor(int resId, ColorStateList color) {
        CcColorsManager.getInstance().setColor(resId, color);
    }

    public static void setState(int resId, int[] state, int color) {
        CcColorsManager.getInstance().setState(resId, state, color);
    }

    public static void setStates(int resId, int[][] states, int[] colors) {
        CcColorsManager.getInstance().setStates(resId, states, colors);
    }

    public static void removeState(int resId, int[] state) {
        CcColorsManager.getInstance().removeState(resId, state);
    }

    public static void removeStates(int resId, int[][] states) {
        CcColorsManager.getInstance().removeStates(resId, states);
    }

    public static CcColorStateList.AnimationBuilder animate(int resId) {
        return CcColorsManager.getInstance().animate(resId);
    }

    public static void addAttrCallbackAdapter(CcAttrCallbackAdapter adapter) {
        CcCallbackManager.getInstance().addAttrCallbackAdapter(adapter);
    }

    public static void addViewCallbackAdapter(CcViewCallbackAdapter adapter) {
        CcCallbackManager.getInstance().addViewCallbackAdapter(adapter);
    }

    public static void addViewDefStyleAdapter(CcViewDefStyleAdapter adapter) {
        CcCallbackManager.getInstance().addViewDefStyleAdapter(adapter);
    }

    public static void addColorCallback(int resId, Object anchor, CcColorStateList.AnchorCallback callback) {
        CcColorStateList color = getColor(resId);
        if (color != null) {
            color.addCallback(anchor, callback);
        }
    }

    public interface Callback {
        void onCodeColorsInitSuccess();
        void onCodeColorsInitFailure();
    }
}
