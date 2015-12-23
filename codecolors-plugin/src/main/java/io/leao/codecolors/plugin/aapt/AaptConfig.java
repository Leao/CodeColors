package io.leao.codecolors.plugin.aapt;


import io.leao.codecolors.plugin.res.CcConfiguration;

/**
 * Based on AaptConfig.cpp.
 */
public class AaptConfig {
    private static final String kWildcardName = "any";

    public static CcConfiguration parse(String str) {
        String[] parts = AaptUtil.splitAndLowerCase(str, "-");

        CcConfiguration config = new CcConfiguration();
        AaptLocaleValue locale = new AaptLocaleValue();
        int index = 0;
        int N = parts.length;
        String part = parts[index];

        if (str.length() == 0) {
            return config;
        }

        if (parseMcc(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseMnc(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        // Locale spans a few '-' separators, so we let it
        // control the index.
        int localeIndex = locale.initFromDirName(parts, index);
        if (localeIndex < 0) {
            return null;
        } else if (localeIndex > index) {
            locale.writeTo(config);
            index = localeIndex;
            if (index >= N) {
                return config;
            }
            part = parts[index];
        }

        if (parseLayoutDirection(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseSmallestScreenWidthDp(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseScreenWidthDp(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseScreenHeightDp(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseScreenLayoutSize(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseScreenLayoutLong(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseScreenRound(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseOrientation(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseUiModeType(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseUiModeNight(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseDensity(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseTouchscreen(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseKeysHidden(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseKeyboard(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseNavHidden(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseNavigation(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseScreenSize(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        if (parseVersion(part, config)) {
            index++;
            if (index == N) {
                return config;
            }
            part = parts[index];
        }

        // Unrecognized.
        return null;
    }

    private static boolean parseMcc(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.mcc = 0;
            return true;
        }

        if (name.length() < 4) {
            return false;
        }

        if (!"mcc".equals(name.substring(0, 3).toLowerCase())) {
            return false;
        }

        String code = name.substring(3);
        if (code.length() == 3 && code.matches("[0-9]+")) {
            int codeValue = Integer.valueOf(code);
            if (codeValue != 0) {
                out.mcc = codeValue;
                return true;
            }
        }

        return false;
    }

    private static boolean parseMnc(String name, CcConfiguration out) {
        if (name.equals(kWildcardName)) {
            out.mnc = 0;
            return true;
        }

        if (name.length() < 4) {
            return false;
        }

        if (!"mnc".equals(name.substring(0, 3).toLowerCase())) {
            return false;
        }

        String code = name.substring(3);
        if (code.length() <= 3 && code.matches("[0-9]+")) {
            out.mnc = Integer.valueOf(code);
            if (out.mnc == 0) {
                out.mnc = CcConfiguration.MNC_ZERO;
            }
        }

        return false;
    }

    private static boolean parseLayoutDirection(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_LAYOUTDIR_MASK)
                    | CcConfiguration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED;
            return true;
        } else if ("ldltr".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_LAYOUTDIR_MASK)
                    | CcConfiguration.SCREENLAYOUT_LAYOUTDIR_LTR;
            return true;
        } else if ("ldrtl".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_LAYOUTDIR_MASK)
                    | CcConfiguration.SCREENLAYOUT_LAYOUTDIR_RTL;
            return true;
        }

        return false;
    }

    private static boolean parseScreenLayoutSize(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_SIZE_MASK)
                    | CcConfiguration.SCREENLAYOUT_SIZE_UNDEFINED;
            return true;
        } else if ("small".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_SIZE_MASK)
                    | CcConfiguration.SCREENLAYOUT_SIZE_SMALL;
            return true;
        } else if ("normal".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_SIZE_MASK)
                    | CcConfiguration.SCREENLAYOUT_SIZE_NORMAL;
            return true;
        } else if ("large".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_SIZE_MASK)
                    | CcConfiguration.SCREENLAYOUT_SIZE_LARGE;
            return true;
        } else if ("xlarge".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_SIZE_MASK)
                    | CcConfiguration.SCREENLAYOUT_SIZE_XLARGE;
            return true;
        }

        return false;
    }

    private static boolean parseScreenLayoutLong(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_LONG_MASK)
                    | CcConfiguration.SCREENLAYOUT_LONG_UNDEFINED;
            return true;
        } else if ("long".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_LONG_MASK)
                    | CcConfiguration.SCREENLAYOUT_LONG_YES;
            return true;
        } else if ("notlong".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_LONG_MASK)
                    | CcConfiguration.SCREENLAYOUT_LONG_NO;
            return true;
        }

        return false;
    }

    private static boolean parseScreenRound(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_ROUND_MASK)
                    | CcConfiguration.SCREENLAYOUT_ROUND_UNDEFINED;
            return true;
        } else if ("round".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_ROUND_MASK)
                    | CcConfiguration.SCREENLAYOUT_ROUND_YES;
            return true;
        } else if ("notround".equals(name)) {
            out.screenLayout = (out.screenLayout & ~CcConfiguration.SCREENLAYOUT_ROUND_MASK)
                    | CcConfiguration.SCREENLAYOUT_ROUND_NO;
            return true;
        }

        return false;
    }

    private static boolean parseOrientation(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.orientation = CcConfiguration.ORIENTATION_UNDEFINED;
            return true;
        } else if ("port".equals(name)) {
            out.orientation = CcConfiguration.ORIENTATION_PORTRAIT;
            return true;
        } else if ("land".equals(name)) {
            out.orientation = CcConfiguration.ORIENTATION_LANDSCAPE;
            return true;
        } else if ("square".equals(name)) {
            out.orientation = CcConfiguration.ORIENTATION_SQUARE;
            return true;
        }

        return false;
    }

    private static boolean parseUiModeType(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_TYPE_MASK) |
                    CcConfiguration.UI_MODE_TYPE_UNDEFINED;
            return true;
        } else if ("desk".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_TYPE_MASK) |
                    CcConfiguration.UI_MODE_TYPE_DESK;
            return true;
        } else if ("car".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_TYPE_MASK) |
                    CcConfiguration.UI_MODE_TYPE_CAR;
            return true;
        } else if ("television".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_TYPE_MASK) |
                    CcConfiguration.UI_MODE_TYPE_TELEVISION;
            return true;
        } else if ("appliance".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_TYPE_MASK) |
                    CcConfiguration.UI_MODE_TYPE_APPLIANCE;
            return true;
        } else if ("watch".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_TYPE_MASK) |
                    CcConfiguration.UI_MODE_TYPE_WATCH;
            return true;
        }

        return false;
    }

    private static boolean parseUiModeNight(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_NIGHT_MASK) |
                    CcConfiguration.UI_MODE_NIGHT_UNDEFINED;
            return true;
        } else if ("night".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_NIGHT_MASK) |
                    CcConfiguration.UI_MODE_NIGHT_YES;
            return true;
        } else if ("notnight".equals(name)) {
            out.uiMode = (out.uiMode & ~CcConfiguration.UI_MODE_NIGHT_MASK) |
                    CcConfiguration.UI_MODE_NIGHT_NO;
            return true;
        }

        return false;
    }

    private static boolean parseDensity(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_UNDEFINED;
            return true;
        }

        if ("anydpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_ANY;
            return true;
        }

        if ("nodpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_NONE;
            return true;
        }

        if ("ldpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_LOW;
            return true;
        }

        if ("mdpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_MEDIUM;
            return true;
        }

        if ("tvdpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_TV;
            return true;
        }

        if ("hdpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_HIGH;
            return true;
        }
        if ("xhdpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_XHIGH;
            return true;
        }

        if ("xxhdpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_XXHIGH;
            return true;
        }

        if ("xxxhdpi".equals(name)) {
            out.densityDpi = CcConfiguration.DENSITY_DPI_XXXHIGH;
            return true;
        }

        if (name.length() <= 3) {
            return false;
        }

        if (!"dpi".equals(name.substring(name.length() - 3, name.length()).toLowerCase())) {
            return false;
        }

        String density = name.substring(0, name.length() - 3);
        if (density.matches("[0-9]+")) {
            int densityValue = Integer.valueOf(density);
            if (densityValue != 0) {
                out.densityDpi = densityValue;
                return true;
            }
        }

        return false;
    }

    private static boolean parseTouchscreen(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.touchscreen = CcConfiguration.TOUCHSCREEN_UNDEFINED;
            return true;
        } else if ("notouch".equals(name)) {
            out.touchscreen = CcConfiguration.TOUCHSCREEN_NOTOUCH;
            return true;
        } else if ("stylus".equals(name)) {
            out.touchscreen = CcConfiguration.TOUCHSCREEN_STYLUS;
            return true;
        } else if ("finger".equals(name)) {
            out.touchscreen = CcConfiguration.TOUCHSCREEN_FINGER;
            return true;
        }

        return false;
    }

    private static boolean parseKeysHidden(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.keyboardHidden = CcConfiguration.KEYBOARDHIDDEN_UNDEFINED;
            return true;
        } else if ("keysexposed".equals(name)) {
            out.keyboardHidden = CcConfiguration.KEYBOARDHIDDEN_NO;
            return true;
        } else if ("keyshidden".equals(name)) {
            out.keyboardHidden = CcConfiguration.KEYBOARDHIDDEN_YES;
            return true;
        } else if ("keyssoft".equals(name)) {
            out.keyboardHidden = CcConfiguration.KEYBOARDHIDDEN_SOFT;
            return true;
        }

        return false;
    }

    private static boolean parseKeyboard(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.keyboard = CcConfiguration.KEYBOARD_UNDEFINED;
            return true;
        } else if ("nokeys".equals(name)) {
            out.keyboard = CcConfiguration.KEYBOARD_NOKEYS;
            return true;
        } else if ("qwerty".equals(name)) {
            out.keyboard = CcConfiguration.KEYBOARD_QWERTY;
            return true;
        } else if ("12key".equals(name)) {
            out.keyboard = CcConfiguration.KEYBOARD_12KEY;
            return true;
        }

        return false;
    }

    private static boolean parseNavHidden(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.navigationHidden = CcConfiguration.NAVIGATIONHIDDEN_UNDEFINED;
            return true;
        } else if ("navexposed".equals(name)) {
            out.navigationHidden = CcConfiguration.NAVIGATIONHIDDEN_NO;
            return true;
        } else if ("navhidden".equals(name)) {
            out.navigationHidden = CcConfiguration.NAVIGATIONHIDDEN_YES;
            return true;
        }

        return false;
    }

    private static boolean parseNavigation(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.navigation = CcConfiguration.NAVIGATION_UNDEFINED;
            return true;
        } else if ("nonav".equals(name)) {
            out.navigation = CcConfiguration.NAVIGATION_NONAV;
            return true;
        } else if ("dpad".equals(name)) {
            out.navigation = CcConfiguration.NAVIGATION_DPAD;
            return true;
        } else if ("trackball".equals(name)) {
            out.navigation = CcConfiguration.NAVIGATION_TRACKBALL;
            return true;
        } else if ("wheel".equals(name)) {
            out.navigation = CcConfiguration.NAVIGATION_WHEEL;
            return true;
        }

        return false;
    }

    /**
     * Probably deprecated. CcConfiguration doesn't hold absolute screen size.
     */
    private static boolean parseScreenSize(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenWidthDp = CcConfiguration.SCREEN_WIDTH_DP_UNDEFINED;
            out.screenHeightDp = CcConfiguration.SCREEN_HEIGHT_DP_UNDEFINED;
            return true;
        }

        if (name.length() < 4) {
            return false;
        }

        int xIndex = name.indexOf('x');
        int yIndex = name.indexOf('y');
        if (xIndex == 0 || yIndex - xIndex < 2 || yIndex != name.length() - 1) {
            return false;
        }

        String x = name.substring(0, xIndex);
        String y = name.substring(xIndex + 1, yIndex);
        if (x.matches("[0-9]+") && y.matches("[0-9]+")) {
            int xValue = Integer.valueOf(x);
            int yValue = Integer.valueOf(y);
            if (xValue < yValue) {
                return false;
            }
            out.screenWidthDp = xValue;
            out.screenHeightDp = yValue;
            return true;
        }

        return false;
    }

    private static boolean parseSmallestScreenWidthDp(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.smallestScreenWidthDp = CcConfiguration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED;
            return true;
        }

        if (name.length() < 5) {
            return false;
        }

        if (!"sw".equals(name.substring(0, 2).toLowerCase()) ||
                !"dp".equals(name.substring(name.length() - 2, name.length()).toLowerCase())) {
            return false;
        }

        String width = name.substring(2, name.length() - 2);
        if (width.matches("[0-9]+")) {
            out.smallestScreenWidthDp = Integer.valueOf(width);
            return true;
        }

        return false;
    }

    private static boolean parseScreenWidthDp(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenWidthDp = CcConfiguration.SCREEN_WIDTH_DP_UNDEFINED;
            return true;
        }

        if (name.length() < 4) {
            return false;
        }

        if (!"w".equals(name.substring(0, 1).toLowerCase()) ||
                !"dp".equals(name.substring(name.length() - 2, name.length()).toLowerCase())) {
            return false;
        }

        String width = name.substring(1, name.length() - 2);
        if (width.matches("[0-9]+")) {
            out.screenWidthDp = Integer.valueOf(width);
            return true;
        }

        return false;
    }

    private static boolean parseScreenHeightDp(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.screenHeightDp = CcConfiguration.SCREEN_HEIGHT_DP_UNDEFINED;
            return true;
        }

        if (name.length() < 4) {
            return false;
        }

        if (!"h".equals(name.substring(0, 1).toLowerCase()) ||
                !"dp".equals(name.substring(name.length() - 2, name.length()).toLowerCase())) {
            return false;
        }

        String height = name.substring(1, name.length() - 2);
        if (height.matches("[0-9]+")) {
            out.screenHeightDp = Integer.valueOf(height);
            return true;
        }

        return false;
    }

    private static boolean parseVersion(String name, CcConfiguration out) {
        if (kWildcardName.equals(name)) {
            out.sdkVersion = CcConfiguration.SDK_VERSION_UNDEFINED;
            return true;
        }

        if (name.length() < 2) {
            return false;
        }

        if (!"v".equals(name.substring(0, 1).toLowerCase())) {
            return false;
        }

        String version = name.substring(1);
        if (version.matches("[0-9]+")) {
            out.sdkVersion = Integer.valueOf(version);
            return true;
        }

        return false;
    }
}
