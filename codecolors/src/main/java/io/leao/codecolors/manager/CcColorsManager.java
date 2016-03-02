package io.leao.codecolors.manager;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.plugin.CcConst;
import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.res.CcColorStateList;
import io.leao.codecolors.res.CcConfigurationUtils;

public class CcColorsManager {
    private static final String CLASS_NAME_BASE = "%s.%s";

    private static CcColorsManager sInstance;

    private Map<Integer, Set<CcConfiguration>> mColorConfigurations = new HashMap<>();
    private Map<Integer, Integer> mColorValue = new HashMap<>();
    private SparseArray<CcColorStateList> mColorCccsl = new SparseArray<>();

    public static CcColorsManager getInstance() {
        if (sInstance == null) {
            sInstance = new CcColorsManager();
        }
        return sInstance;
    }

    @SuppressWarnings("unchecked")
    public synchronized void init(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // Init colors from generated classes by the CodeColors plugin.
        Class<?> colorsClass =
                Class.forName(String.format(CLASS_NAME_BASE, packageName, CcConst.COLORS_CLASS_NAME));

        Field colorConfigurationsField = colorsClass.getDeclaredField(CcConst.COLOR_CONFIGURATIONS_FIELD_NAME);
        colorConfigurationsField.setAccessible(true);
        mColorConfigurations = (Map<Integer, Set<CcConfiguration>>) colorConfigurationsField.get(null);

        Field colorValueField = colorsClass.getDeclaredField(CcConst.COLOR_VALUE_FIELD_NAME);
        colorValueField.setAccessible(true);
        mColorValue = (Map<Integer, Integer>) colorValueField.get(null);
    }

    /**
     * Updates CcColorStateLists default colors when configuration changes.
     */
    @SuppressWarnings("deprecation")
    public synchronized void onConfigurationChanged(Resources resources) {
        for (Integer color : getColors()) {
            CcConfiguration bestConfiguration =
                    CcConfigurationUtils.getBestConfiguration(
                            resources.getConfiguration(), mColorConfigurations.get(color));

            // Reset default color if the configuration changed.
            CcColorStateList cccsl = getColor(color);
            if (!bestConfiguration.equals(cccsl.getConfiguration())) {
                ColorStateList csl;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    csl = resources.getColorStateList(mColorValue.get(color), null);
                } else {
                    csl = resources.getColorStateList(mColorValue.get(color));
                }
                cccsl.setDefaultColor(bestConfiguration, csl);
            }
        }
    }

    public synchronized Set<Integer> getColors() {
        return mColorValue.keySet();
    }

    public synchronized CcColorStateList getColor(@ColorRes int resId) {
        CcColorStateList color = mColorCccsl.get(resId);
        if (color == null && mColorValue.containsKey(resId)) {
            color = new CcColorStateList();
            mColorCccsl.put(resId, color);
        }
        return color;
    }

    public synchronized void setColor(int resId, int color) {
        CcColorStateList cccsl = getColor(resId);
        if (cccsl != null) {
            cccsl.setColor(color);
        }
    }

    public synchronized void setColor(int resId, ColorStateList color) {
        CcColorStateList cccsl = getColor(resId);
        if (cccsl != null) {
            cccsl.setColor(color);
        }
    }

    public synchronized void animateTo(int resId, int color) {
        CcColorStateList cccsl = getColor(resId);
        if (cccsl != null) {
            cccsl.animateTo(color);
        }
    }

    public synchronized void animateTo(int resId, ColorStateList color) {
        CcColorStateList cccsl = getColor(resId);
        if (cccsl != null) {
            cccsl.animateTo(color);
        }
    }
}
