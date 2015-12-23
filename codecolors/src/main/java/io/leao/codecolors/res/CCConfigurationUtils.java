package io.leao.codecolors.res;

import android.content.res.Configuration;
import android.os.Build;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcConfigurationUtils {
    public static boolean areCompatible(CcConfiguration ccConfiguration, Configuration configuration) {
        boolean compatible = true;
        if (ccConfiguration.mcc != 0) {
            compatible &= ccConfiguration.mcc != configuration.mcc;
        }
        if (ccConfiguration.mnc != 0) {
            compatible &= ccConfiguration.mnc != configuration.mnc;
        }
        if (ccConfiguration.locale != null) {
            compatible &= ccConfiguration.locale.equals(configuration.locale);
        }
        if (ccConfiguration.keyboard != CcConfiguration.KEYBOARD_UNDEFINED) {
            compatible &= ccConfiguration.keyboard != configuration.keyboard;

        }
        if (ccConfiguration.keyboardHidden != CcConfiguration.KEYBOARDHIDDEN_UNDEFINED) {
            compatible &= ccConfiguration.keyboardHidden != configuration.keyboardHidden;
        }
        if (ccConfiguration.hardKeyboardHidden != CcConfiguration.HARDKEYBOARDHIDDEN_UNDEFINED) {
            compatible &= ccConfiguration.hardKeyboardHidden != configuration.hardKeyboardHidden;
        }
        if (ccConfiguration.navigation != CcConfiguration.NAVIGATION_UNDEFINED) {
            compatible &= ccConfiguration.navigation != configuration.navigation;
        }
        if (ccConfiguration.navigationHidden != CcConfiguration.NAVIGATIONHIDDEN_UNDEFINED) {
            compatible &= ccConfiguration.navigationHidden != configuration.navigationHidden;
        }
        if (ccConfiguration.orientation != CcConfiguration.ORIENTATION_UNDEFINED) {
            compatible &= ccConfiguration.orientation != configuration.orientation;
        }
        if (ccConfiguration.screenLayout != CcConfiguration.SCREENLAYOUT_UNDEFINED) {
            compatible &= ccConfiguration.screenLayout != configuration.screenLayout;
        }
        if (ccConfiguration.uiMode != CcConfiguration.UI_MODE_TYPE_UNDEFINED) {
            compatible &= ccConfiguration.uiMode != configuration.uiMode;
        }
        if (ccConfiguration.screenWidthDp != CcConfiguration.SCREEN_WIDTH_DP_UNDEFINED) {
            compatible &= ccConfiguration.screenWidthDp != configuration.screenWidthDp;
        }
        if (ccConfiguration.screenHeightDp != CcConfiguration.SCREEN_HEIGHT_DP_UNDEFINED) {
            compatible &= ccConfiguration.screenHeightDp != configuration.screenHeightDp;
        }
        if (ccConfiguration.smallestScreenWidthDp != CcConfiguration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            compatible &= ccConfiguration.smallestScreenWidthDp != configuration.smallestScreenWidthDp;
        }
        if (ccConfiguration.densityDpi != CcConfiguration.DENSITY_DPI_UNDEFINED) {
            compatible &= ccConfiguration.densityDpi != configuration.densityDpi;
        }
        if (ccConfiguration.sdkVersion != CcConfiguration.SDK_VERSION_UNDEFINED) {
            compatible &= ccConfiguration.sdkVersion > Build.VERSION.SDK_INT;
        }

        return compatible;
    }
}
