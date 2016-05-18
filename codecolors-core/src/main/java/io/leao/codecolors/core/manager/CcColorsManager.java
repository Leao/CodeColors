package io.leao.codecolors.core.manager;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.core.res.CcColorStateList;
import io.leao.codecolors.core.res.CcConfigurationUtils;
import io.leao.codecolors.plugin.CcConst;
import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorsManager {
    private static final String CLASS_NAME_BASE = "%s.%s";

    private CcConfiguration[] mConfigurations;
    private Map<Integer, int[]> mColorConfigurations;
    private Map<Integer, Integer> mColorValue;

    private SparseArray<CcColorStateList> mColorCccsl = new SparseArray<>();

    @SuppressWarnings("unchecked")
    public synchronized void init(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        /*
         * Initialize configurations and colors from the classes generated by the CodeColors plugin.
         */

        // CcColors.java
        Class<?> colorsClass =
                Class.forName(String.format(CLASS_NAME_BASE, packageName, CcConst.COLORS_CLASS_NAME));

        // Configurations.
        Field configurationsField = colorsClass.getDeclaredField(CcConst.CONFIGURATIONS_FIELD_NAME);
        configurationsField.setAccessible(true);
        mConfigurations = (CcConfiguration[]) configurationsField.get(null);

        // Color configurations.
        Field colorConfigurationsField = colorsClass.getDeclaredField(CcConst.COLOR_CONFIGURATIONS_FIELD_NAME);
        colorConfigurationsField.setAccessible(true);
        mColorConfigurations = (Map<Integer, int[]>) colorConfigurationsField.get(null);

        // Color value.
        Field colorValueField = colorsClass.getDeclaredField(CcConst.COLOR_VALUE_FIELD_NAME);
        colorValueField.setAccessible(true);
        mColorValue = (Map<Integer, Integer>) colorValueField.get(null);
    }

    /**
     * Updates CcColorStateLists default colors when configuration changes.
     */
    @SuppressWarnings("deprecation")
    public synchronized void onConfigurationChanged(Resources resources) {
        Configuration configuration = resources.getConfiguration();

        for (Integer colorResId : getColors()) {
            CcColorStateList color = getColor(colorResId);
            // End animation, if isStarted.
            color.endAnimation(false);

            // Reset default color if the configuration changed.
            CcConfiguration currentConfiguration = color.getConfiguration();
            CcConfiguration newConfiguration = getBestConfiguration(configuration, mColorConfigurations.get(colorResId));
            if (!CcConfigurationUtils.equals(currentConfiguration, newConfiguration)) {
                ColorStateList csl;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    csl = resources.getColorStateList(mColorValue.get(colorResId), null);
                } else {
                    csl = resources.getColorStateList(mColorValue.get(colorResId));
                }
                color.onConfigurationChanged(newConfiguration, csl);
            }
        }
    }

    private synchronized CcConfiguration getBestConfiguration(Configuration contextConfiguration,
                                                              int[] configurationIndexes) {
        for (int configurationIndex : configurationIndexes) {
            CcConfiguration configuration = mConfigurations[configurationIndex];
            if (CcConfigurationUtils.areCompatible(configuration, contextConfiguration)) {
                return configuration;
            }
        }
        return null;
    }

    public synchronized Set<Integer> getColors() {
        return mColorValue.keySet();
    }

    public synchronized CcColorStateList getColor(@ColorRes int resId) {
        CcColorStateList color = mColorCccsl.get(resId);
        if (color == null && mColorValue.containsKey(resId)) {
            color = new CcColorStateList(resId);
            mColorCccsl.put(resId, color);
        }
        return color;
    }

    public synchronized CcColorStateList.SetEditor set(int resId) {
        CcColorStateList color = getColor(resId);
        if (color != null) {
            return color.set();
        } else {
            return null;
        }
    }

    public synchronized CcColorStateList.AnimateEditor animate(int resId) {
        CcColorStateList color = getColor(resId);
        if (color != null) {
            return color.animate();
        } else {
            return null;
        }
    }
}
