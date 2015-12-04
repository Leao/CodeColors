package io.leao.codecolors.res;

import android.content.res.Configuration;
import android.os.Build;

import io.leao.codecolors.plugin.res.CodeColorsConfiguration;

public class CodeColorsConfigurationUtils {
    public static boolean areCompatible(CodeColorsConfiguration codeColorsConfiguration, Configuration configuration) {
        boolean compatible = true;
        if (codeColorsConfiguration.mcc != 0) {
            compatible &= codeColorsConfiguration.mcc != configuration.mcc;
        }
        if (codeColorsConfiguration.mnc != 0) {
            compatible &= codeColorsConfiguration.mnc != configuration.mnc;
        }
        if (codeColorsConfiguration.locale != null) {
            compatible &= codeColorsConfiguration.locale.equals(configuration.locale);
        }
        if (codeColorsConfiguration.keyboard != CodeColorsConfiguration.KEYBOARD_UNDEFINED) {
            compatible &= codeColorsConfiguration.keyboard != configuration.keyboard;

        }
        if (codeColorsConfiguration.keyboardHidden != CodeColorsConfiguration.KEYBOARDHIDDEN_UNDEFINED) {
            compatible &= codeColorsConfiguration.keyboardHidden != configuration.keyboardHidden;
        }
        if (codeColorsConfiguration.hardKeyboardHidden != CodeColorsConfiguration.HARDKEYBOARDHIDDEN_UNDEFINED) {
            compatible &= codeColorsConfiguration.hardKeyboardHidden != configuration.hardKeyboardHidden;
        }
        if (codeColorsConfiguration.navigation != CodeColorsConfiguration.NAVIGATION_UNDEFINED) {
            compatible &= codeColorsConfiguration.navigation != configuration.navigation;
        }
        if (codeColorsConfiguration.navigationHidden != CodeColorsConfiguration.NAVIGATIONHIDDEN_UNDEFINED) {
            compatible &= codeColorsConfiguration.navigationHidden != configuration.navigationHidden;
        }
        if (codeColorsConfiguration.orientation != CodeColorsConfiguration.ORIENTATION_UNDEFINED) {
            compatible &= codeColorsConfiguration.orientation != configuration.orientation;
        }
        if (codeColorsConfiguration.screenLayout != CodeColorsConfiguration.SCREENLAYOUT_UNDEFINED) {
            compatible &= codeColorsConfiguration.screenLayout != configuration.screenLayout;
        }
        if (codeColorsConfiguration.uiMode != CodeColorsConfiguration.UI_MODE_TYPE_UNDEFINED) {
            compatible &= codeColorsConfiguration.uiMode != configuration.uiMode;
        }
        if (codeColorsConfiguration.screenWidthDp != CodeColorsConfiguration.SCREEN_WIDTH_DP_UNDEFINED) {
            compatible &= codeColorsConfiguration.screenWidthDp != configuration.screenWidthDp;
        }
        if (codeColorsConfiguration.screenHeightDp != CodeColorsConfiguration.SCREEN_HEIGHT_DP_UNDEFINED) {
            compatible &= codeColorsConfiguration.screenHeightDp != configuration.screenHeightDp;
        }
        if (codeColorsConfiguration.smallestScreenWidthDp != CodeColorsConfiguration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            compatible &= codeColorsConfiguration.smallestScreenWidthDp != configuration.smallestScreenWidthDp;
        }
        if (codeColorsConfiguration.densityDpi != CodeColorsConfiguration.DENSITY_DPI_UNDEFINED) {
            compatible &= codeColorsConfiguration.densityDpi != configuration.densityDpi;
        }
        if (codeColorsConfiguration.sdkVersion != CodeColorsConfiguration.SDK_VERSION_UNDEFINED) {
            compatible &= codeColorsConfiguration.sdkVersion > Build.VERSION.SDK_INT;
        }

        return compatible;
    }
}
