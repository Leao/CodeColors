package io.leao.codecolors;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.config.BaseCodeColorFactory;
import io.leao.codecolors.config.BaseCodeColorHandler;
import io.leao.codecolors.config.CodeColorFactory;
import io.leao.codecolors.config.CodeColorHandler;
import io.leao.codecolors.plugin.res.CodeColorsConfiguration;
import io.leao.codecolors.res.CodeColorDrawable;
import io.leao.codecolors.res.CodeColorStateList;
import io.leao.codecolors.res.CodeColorsConfigurationUtils;
import io.leao.codecolors.res.CodeResources;

public abstract class CodeColors {
    private static final String LOG_TAG = CodeColors.class.getSimpleName();

    private static final int[] sDefaultCodeColorResIds = new int[]{
            R.color.cc__color_accent,
            R.color.cc__color_primary,
            R.color.cc__color_primary_dark
    };

    private static boolean sIsActive = false;

    private static final SparseArray<CodeColorStateList> sColorCache = new SparseArray<>();

    private static final Map<String, Map<CodeColorsConfiguration, Map<Integer, Set<Integer>>>> sDependencies =
            new HashMap<>();

    public static void init(Context context) {
        init(context, new BaseCodeColorFactory());
    }

    public static void init(Context context, CodeColorFactory factory) {
        init(context, factory, sDefaultCodeColorResIds);
    }

    public static void init(Context context, CodeColorFactory factory, int[] colorResIds) {
        init(context, new BaseCodeColorHandler(context, factory, colorResIds));
    }

    @SuppressWarnings("unchecked")
    public static void init(Context context, CodeColorHandler handler) {
        try {
            addPackageDependencies(context.getPackageName());

            Resources resources = context.getResources();
            // The SparseArray that holds the entries of preloaded colors.
            Field sPreloadedColorStateListsField = Resources.class.getDeclaredField("sPreloadedColorStateLists");
            sPreloadedColorStateListsField.setAccessible(true);
            LongSparseArray sPreloadedColorStateLists = (LongSparseArray) sPreloadedColorStateListsField.get(null);
            // The getter method to retrieve the constant state, or null.
            // If sPreloadedColorStateListsField type is not supported,
            // getColorsConstantStateGetter throws an exception.
            Method colorsConstantStateGetter = getColorsConstantStateGetter(sPreloadedColorStateListsField);

            // The SparseArray that holds the entries of preloaded drawables.
            Field sPreloadedDrawablesField = Resources.class.getDeclaredField("sPreloadedDrawables");
            sPreloadedDrawablesField.setAccessible(true);
            Object sPreloadedDrawables = sPreloadedDrawablesField.get(null);
            // Whether to layout orientation to cache drawables.
            // If sPreloadedDrawables type is not supported, useLayoutDirectionDrawableCache throws an exception.
            boolean useLayoutDirectionDrawableCache = useLayoutDirectionDrawableCache(sPreloadedDrawables);

            for (int i = 0; i < handler.getColorCount(); i++) {
                int id = handler.getColorResId(i);
                if (id <= 0) {
                    continue;
                }

                CodeColorStateList color = handler.getColor(i);
                color.setId(id);

                long key = CodeResources.createKey(resources, id);

                // Load color into Resources cache.
                if (colorsConstantStateGetter != null) {
                    sPreloadedColorStateLists.put(key, colorsConstantStateGetter.invoke(color));
                } else {
                    sPreloadedColorStateLists.put(key, color);
                }
                // Cache colors by id.
                sColorCache.put(id, color);

                // Load drawable into Resources cache.
                Drawable.ConstantState drawableConstantState = CodeColorDrawable.getConstantStateForColor(color);
                if (useLayoutDirectionDrawableCache) {
                    // Load for both layout directions (LTR and RTL).
                    ((LongSparseArray[]) sPreloadedDrawables)[0].put(key, drawableConstantState);
                    ((LongSparseArray[]) sPreloadedDrawables)[1].put(key, drawableConstantState);
                } else {
                    ((LongSparseArray) sPreloadedDrawables).put(key, drawableConstantState);
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "ColorStateList preload failed. Dynamic colors will not work.", e);
            return; // Finish without setting code colors active.
        }

        // Code colors successfully injected.
        sIsActive = true;
    }

    @SuppressWarnings("unchecked")
    public static void addPackageDependencies(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> codeColorResourcesClass = Class.forName(packageName + ".CodeColorResources");
        Field dependenciesField = codeColorResourcesClass.getDeclaredField("sDependencies");
        dependenciesField.setAccessible(true);
        sDependencies.put(
                packageName,
                (Map<CodeColorsConfiguration, Map<Integer, Set<Integer>>>) dependenciesField.get(null));
    }

    public static Map<Integer, Set<Integer>> getDependencies(Context context) {
        Map<CodeColorsConfiguration, Map<Integer, Set<Integer>>> configurationDependencies =
                sDependencies.get(context.getPackageName());
        Configuration contextConfiguration = context.getResources().getConfiguration();
        for (CodeColorsConfiguration configuration : configurationDependencies.keySet()) {
            if (CodeColorsConfigurationUtils.areCompatible(configuration, contextConfiguration)) {
                return configurationDependencies.get(configuration);
            }
        }
        return configurationDependencies.get(configurationDependencies.keySet().iterator().next());
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

    public static CodeColorStateList getColor(int resId) {
        return sColorCache.get(resId);
    }

    public static void setColor(int resId, int color) {
        CodeColorStateList ccsl = getColor(resId);
        if (ccsl != null) {
            ccsl.setColor(color);
        }
    }
}
