package io.leao.codecolors.core.color;

import android.content.res.ColorStateList;
import android.content.res.Resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorStateListUtils {
    private static final Constructor<ColorStateList> sCloneConstructor;
    private static final Method sApplyThemeMethod;
    private static final Method sGetConstantStateMethod;
    private static final Field sStateSpecsField;
    private static final Field sColorsField;
    private static final Field sThemeAttrsField;

    static {
        Constructor<ColorStateList> cloneConstructor;
        try {
            cloneConstructor = ColorStateList.class.getDeclaredConstructor(ColorStateList.class);
            cloneConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            cloneConstructor = null;
        }
        sCloneConstructor = cloneConstructor;

        Method getConstantStateMethod;
        try {
            getConstantStateMethod = ColorStateList.class.getDeclaredMethod("getConstantState");
            getConstantStateMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            getConstantStateMethod = null;
        }
        sGetConstantStateMethod = getConstantStateMethod;

        Method applyThemeMethod;
        try {
            applyThemeMethod = ColorStateList.class.getDeclaredMethod("applyTheme", Resources.Theme.class);
            applyThemeMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            applyThemeMethod = null;
        }
        sApplyThemeMethod = applyThemeMethod;

        Field stateSpecsField;
        try {
            stateSpecsField = ColorStateList.class.getDeclaredField("mStateSpecs");
            stateSpecsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            stateSpecsField = null;
        }
        sStateSpecsField = stateSpecsField;

        Field colorsField;
        try {
            colorsField = ColorStateList.class.getDeclaredField("mColors");
            colorsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            colorsField = null;
        }
        sColorsField = colorsField;

        Field themeAttrsField;
        try {
            themeAttrsField = ColorStateList.class.getDeclaredField("mThemeAttrs");
            themeAttrsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            themeAttrsField = null;
        }
        sThemeAttrsField = themeAttrsField;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public static ColorStateList clone(ColorStateList color) {
        if (sCloneConstructor != null) {
            try {
                return sCloneConstructor.newInstance(color);
            } catch (InstantiationException e) {
                // Do nothing.
            } catch (IllegalAccessException e) {
                // Do nothing.
            } catch (InvocationTargetException e) {
                // Do nothing.
            }
        }
        return color;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public static Object getConstantState(ColorStateList color) {
        if (sGetConstantStateMethod != null) {
            try {
                return sGetConstantStateMethod.invoke(color);
            } catch (InvocationTargetException e) {
                // Do nothing.
            } catch (IllegalAccessException e) {
                // Do nothing.
            }
        }
        return null;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public static void applyTheme(ColorStateList color, Resources.Theme theme) {
        if (sApplyThemeMethod != null) {
            try {
                sApplyThemeMethod.invoke(color, theme);
            } catch (InvocationTargetException e) {
                // Do nothing.
            } catch (IllegalAccessException e) {
                // Do nothing.
            }
        }
    }

    public static int[][] getStateSpecs(ColorStateList color) {
        if (sStateSpecsField != null) {
            try {
                return (int[][]) sStateSpecsField.get(color);
            } catch (IllegalAccessException e) {
                // Do nothing.
            }
        }
        return CodeColor.EMPTY_STATES;
    }

    public static int[] getColors(ColorStateList color) {
        if (sColorsField != null) {
            try {
                return (int[]) sColorsField.get(color);
            } catch (IllegalAccessException e) {
                // Do nothing.
            }
        }
        return CodeColor.DEFAULT_COLORS;
    }

    public static int[][] getThemeAttrs(ColorStateList color) {
        if (sThemeAttrsField != null) {
            try {
                return (int[][]) sThemeAttrsField.get(color);
            } catch (IllegalAccessException e) {
                // Do nothing.
            }
        }
        return null;
    }
}
