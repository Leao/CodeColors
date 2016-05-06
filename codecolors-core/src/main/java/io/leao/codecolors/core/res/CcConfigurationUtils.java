package io.leao.codecolors.core.res;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;

import java.util.Locale;
import java.util.Set;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcConfigurationUtils {
    public static boolean areCompatible(CcConfiguration ccConfiguration, Configuration configuration) {
        boolean compatible = true;
        if (ccConfiguration.mcc != 0) {
            compatible &= ccConfiguration.mcc == configuration.mcc;
        }
        if (ccConfiguration.mnc != 0) {
            compatible &= ccConfiguration.mnc == configuration.mnc;
        }
        if (ccConfiguration.locale != null) {
            compatible &= ccConfiguration.locale.equals(configuration.locale);
        }
        if (ccConfiguration.keyboard != CcConfiguration.KEYBOARD_UNDEFINED) {
            compatible &= ccConfiguration.keyboard == configuration.keyboard;
        }
        if (ccConfiguration.keyboardHidden != CcConfiguration.KEYBOARDHIDDEN_UNDEFINED) {
            compatible &= ccConfiguration.keyboardHidden == configuration.keyboardHidden;
        }
        if (ccConfiguration.hardKeyboardHidden != CcConfiguration.HARDKEYBOARDHIDDEN_UNDEFINED) {
            compatible &= ccConfiguration.hardKeyboardHidden == configuration.hardKeyboardHidden;
        }
        if (ccConfiguration.navigation != CcConfiguration.NAVIGATION_UNDEFINED) {
            compatible &= ccConfiguration.navigation == configuration.navigation;
        }
        if (ccConfiguration.navigationHidden != CcConfiguration.NAVIGATIONHIDDEN_UNDEFINED) {
            compatible &= ccConfiguration.navigationHidden == configuration.navigationHidden;
        }
        if (ccConfiguration.orientation != CcConfiguration.ORIENTATION_UNDEFINED) {
            compatible &= ccConfiguration.orientation == configuration.orientation;
        }
        if (ccConfiguration.screenLayout != CcConfiguration.SCREENLAYOUT_UNDEFINED) {
            compatible &= ccConfiguration.screenLayout == configuration.screenLayout;
        }
        if (ccConfiguration.uiMode != CcConfiguration.UI_MODE_TYPE_UNDEFINED) {
            compatible &= ccConfiguration.uiMode == configuration.uiMode;
        }
        if (ccConfiguration.screenWidthDp != CcConfiguration.SCREEN_WIDTH_DP_UNDEFINED) {
            compatible &= ccConfiguration.screenWidthDp == configuration.screenWidthDp;
        }
        if (ccConfiguration.screenHeightDp != CcConfiguration.SCREEN_HEIGHT_DP_UNDEFINED) {
            compatible &= ccConfiguration.screenHeightDp == configuration.screenHeightDp;
        }
        if (ccConfiguration.smallestScreenWidthDp != CcConfiguration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            compatible &= ccConfiguration.smallestScreenWidthDp == configuration.smallestScreenWidthDp;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                ccConfiguration.densityDpi != CcConfiguration.DENSITY_DPI_UNDEFINED) {
            compatible &= ccConfiguration.densityDpi == configuration.densityDpi;
        }
        if (ccConfiguration.sdkVersion != CcConfiguration.SDK_VERSION_UNDEFINED) {
            compatible &= ccConfiguration.sdkVersion <= Build.VERSION.SDK_INT;
        }

        return compatible;
    }

    public static CcConfiguration getBestConfiguration(Configuration contextConfiguration,
                                                       Set<CcConfiguration> configurations) {
        CcConfiguration lastConfiguration = null;
        for (CcConfiguration configuration : configurations) {
            if (CcConfigurationUtils.areCompatible(configuration, contextConfiguration)) {
                return configuration;
            }
            lastConfiguration = configuration;
        }
        return lastConfiguration;
    }

    public static void writeToParcel(CcConfiguration configuration, Parcel dest, int flags) {
        dest.writeFloat(configuration.fontScale);
        dest.writeInt(configuration.mcc);
        dest.writeInt(configuration.mnc);
        if (configuration.locale == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(configuration.locale.getLanguage());
            dest.writeString(configuration.locale.getCountry());
            dest.writeString(configuration.locale.getVariant());
        }
        if (configuration.userSetLocale) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(configuration.touchscreen);
        dest.writeInt(configuration.keyboard);
        dest.writeInt(configuration.keyboardHidden);
        dest.writeInt(configuration.hardKeyboardHidden);
        dest.writeInt(configuration.navigation);
        dest.writeInt(configuration.navigationHidden);
        dest.writeInt(configuration.orientation);
        dest.writeInt(configuration.screenLayout);
        dest.writeInt(configuration.uiMode);
        dest.writeInt(configuration.screenWidthDp);
        dest.writeInt(configuration.screenHeightDp);
        dest.writeInt(configuration.smallestScreenWidthDp);
        dest.writeInt(configuration.densityDpi);
        dest.writeInt(configuration.sdkVersion);
    }

    public static CcConfiguration readFromParcel(Parcel source) {
        CcConfiguration configuration = new CcConfiguration();
        configuration.fontScale = source.readFloat();
        configuration.mcc = source.readInt();
        configuration.mnc = source.readInt();
        if (source.readInt() != 0) {
            configuration.locale = new Locale(source.readString(), source.readString(), source.readString());
        }
        configuration.userSetLocale = (source.readInt() == 1);
        configuration.touchscreen = source.readInt();
        configuration.keyboard = source.readInt();
        configuration.keyboardHidden = source.readInt();
        configuration.hardKeyboardHidden = source.readInt();
        configuration.navigation = source.readInt();
        configuration.navigationHidden = source.readInt();
        configuration.orientation = source.readInt();
        configuration.screenLayout = source.readInt();
        configuration.uiMode = source.readInt();
        configuration.screenWidthDp = source.readInt();
        configuration.screenHeightDp = source.readInt();
        configuration.smallestScreenWidthDp = source.readInt();
        configuration.densityDpi = source.readInt();
        configuration.sdkVersion = source.readInt();
        return configuration;
    }
}
