package io.leao.codecolors.manager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
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

    private static final Map<String, Map<Integer, Set<CcConfiguration>>> sPackageColorConfigurations = new HashMap<>();
    private static final Map<String, Map<Integer, Integer>> sPackageColorValue = new HashMap<>();

    private static final Map<String, CcColorsManager> sPackageManagers = new HashMap<>();
    private static final SparseArray<CcColorStateList> sColorCccsl = new SparseArray<>();

    private Map<Integer, Set<CcConfiguration>> mColorConfigurations = new HashMap<>();
    private Map<Integer, Integer> mColorValue = new HashMap<>();
    private SparseArray<CcColorStateList> mColorCsssl;

    private Configuration mConfiguration;

    public static CcColorsManager obtain(Context context) {
        String packageName = context.getPackageName();
        CcColorsManager manager = sPackageManagers.get(packageName);
        if (manager == null) {
            manager = new CcColorsManager(context);
            sPackageManagers.put(packageName, manager);
        }
        return manager;
    }

    protected CcColorsManager(Context context) {
        String packageName = context.getPackageName();
        mColorConfigurations = sPackageColorConfigurations.get(packageName);
        mColorValue = sPackageColorValue.get(packageName);
        // Get/create colors for given package.
        mColorCsssl = new SparseArray<>();
        for (Integer color : getColors()) {
            CcColorStateList cccsl = sColorCccsl.get(color);
            if (cccsl == null) {
                cccsl = new CcColorStateList();
                sColorCccsl.put(color, cccsl);
            }
            mColorCsssl.put(color, cccsl);
        }
    }

    @SuppressWarnings("deprecation")
    public void onNewContext(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (!configuration.equals(mConfiguration)) {
            // Set new configuration.
            if (mConfiguration == null) {
                mConfiguration = new Configuration(configuration);
            } else {
                mConfiguration.setTo(configuration);
            }

            for (Integer color : getColors()) {
                CcConfiguration bestConfiguration =
                        CcConfigurationUtils.getBestConfiguration(mConfiguration, mColorConfigurations.get(color));

                // Reset default color if the configuration changed.
                CcColorStateList cccsl = mColorCsssl.get(color);
                if (!bestConfiguration.equals(cccsl.getConfiguration())) {
                    ColorStateList csl;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        csl = resources.getColorStateList(mColorValue.get(color), context.getTheme());
                    } else {
                        csl = resources.getColorStateList(mColorValue.get(color));
                    }
                    cccsl.setDefaultColor(bestConfiguration, csl);
                }
            }
        }
    }

    public Set<Integer> getColors() {
        return mColorConfigurations.keySet();
    }

    public CcColorStateList getColor(@ColorRes int resId) {
        return mColorCsssl.get(resId);
    }

    public void setColor(int resId, int color) {
        CcColorStateList cccsl = getColor(resId);
        if (cccsl != null) {
            cccsl.setColor(color);
        }
    }

    @SuppressWarnings("unchecked")
    public static void addPackageColors(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> colorsClass =
                Class.forName(String.format(CLASS_NAME_BASE, packageName, CcConst.COLORS_CLASS_NAME));

        Field colorConfigurationsField = colorsClass.getDeclaredField(CcConst.COLOR_CONFIGURATIONS_FIELD_NAME);
        colorConfigurationsField.setAccessible(true);
        sPackageColorConfigurations.put(
                packageName,
                (Map<Integer, Set<CcConfiguration>>) colorConfigurationsField.get(null));

        Field colorValueField = colorsClass.getDeclaredField(CcConst.COLOR_VALUE_FIELD_NAME);
        colorValueField.setAccessible(true);
        sPackageColorValue.put(
                packageName,
                (Map<Integer, Integer>) colorValueField.get(null));
    }
}
