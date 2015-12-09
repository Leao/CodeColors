package io.leao.codecolors.plugin.res;

import java.util.Locale;

public class CodeColorsConfiguration implements Comparable<CodeColorsConfiguration> {
    public static final CodeColorsConfiguration EMPTY = new CodeColorsConfiguration();

    /**
     * Current user preference for the scaling factor for fonts, relative
     * to the base density scaling.
     */
    public float fontScale;

    /**
     * IMSI MCC (Mobile Country Code), corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#MccQualifier">mcc</a>
     * resource qualifier.  0 if undefined.
     */
    public int mcc;

    /**
     * IMSI MNC (Mobile Network Code), corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#MccQualifier">mnc</a>
     * resource qualifier.  0 if undefined. Note that the actual MNC may be 0; in order to check
     * for this use the {@link #MNC_ZERO} symbol.
     */
    public int mnc;

    /**
     * Constant used to to represent MNC (Mobile Network Code) zero.
     * 0 cannot be used, since it is used to represent an undefined MNC.
     */
    public static final int MNC_ZERO = 0xffff;

    /**
     * Current user preference for the locale, corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#LocaleQualifier">locale</a>
     * resource qualifier.
     */
    public Locale locale;

    /**
     * Locale should persist on setting.  This is hidden because it is really
     * questionable whether this is the right way to expose the functionality.
     */
    public boolean userSetLocale;

    /**
     * Constant for {@link #screenLayout}: bits that encode the size.
     */
    public static final int SCREENLAYOUT_SIZE_MASK = 0x0f;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_SIZE_MASK}
     * value indicating that no size has been set.
     */
    public static final int SCREENLAYOUT_SIZE_UNDEFINED = 0x00;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_SIZE_MASK}
     * value indicating the screen is at least approximately 320x426 dp units,
     * corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenSizeQualifier">small</a>
     * resource qualifier.
     * See <a href="{@docRoot}guide/practices/screens_support.html">Supporting
     * Multiple Screens</a> for more information.
     */
    public static final int SCREENLAYOUT_SIZE_SMALL = 0x01;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_SIZE_MASK}
     * value indicating the screen is at least approximately 320x470 dp units,
     * corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenSizeQualifier">normal</a>
     * resource qualifier.
     * See <a href="{@docRoot}guide/practices/screens_support.html">Supporting
     * Multiple Screens</a> for more information.
     */
    public static final int SCREENLAYOUT_SIZE_NORMAL = 0x02;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_SIZE_MASK}
     * value indicating the screen is at least approximately 480x640 dp units,
     * corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenSizeQualifier">large</a>
     * resource qualifier.
     * See <a href="{@docRoot}guide/practices/screens_support.html">Supporting
     * Multiple Screens</a> for more information.
     */
    public static final int SCREENLAYOUT_SIZE_LARGE = 0x03;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_SIZE_MASK}
     * value indicating the screen is at least approximately 720x960 dp units,
     * corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenSizeQualifier">xlarge</a>
     * resource qualifier.
     * See <a href="{@docRoot}guide/practices/screens_support.html">Supporting
     * Multiple Screens</a> for more information.
     */
    public static final int SCREENLAYOUT_SIZE_XLARGE = 0x04;

    /**
     * Constant for {@link #screenLayout}: bits that encode the aspect ratio.
     */
    public static final int SCREENLAYOUT_LONG_MASK = 0x30;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_LONG_MASK}
     * value indicating that no size has been set.
     */
    public static final int SCREENLAYOUT_LONG_UNDEFINED = 0x00;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_LONG_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenAspectQualifier">notlong</a>
     * resource qualifier.
     */
    public static final int SCREENLAYOUT_LONG_NO = 0x10;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_LONG_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenAspectQualifier">long</a>
     * resource qualifier.
     */
    public static final int SCREENLAYOUT_LONG_YES = 0x20;

    /**
     * Constant for {@link #screenLayout}: bits that encode the layout direction.
     */
    public static final int SCREENLAYOUT_LAYOUTDIR_MASK = 0xC0;
    /**
     * Constant for {@link #screenLayout}: bits shift to get the layout direction.
     */
    public static final int SCREENLAYOUT_LAYOUTDIR_SHIFT = 6;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_LAYOUTDIR_MASK}
     * value indicating that no layout dir has been set.
     */
    public static final int SCREENLAYOUT_LAYOUTDIR_UNDEFINED = 0x00;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_LAYOUTDIR_MASK}
     * value indicating that a layout dir has been set to LTR.
     */
    public static final int SCREENLAYOUT_LAYOUTDIR_LTR = 0x01 << SCREENLAYOUT_LAYOUTDIR_SHIFT;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_LAYOUTDIR_MASK}
     * value indicating that a layout dir has been set to RTL.
     */
    public static final int SCREENLAYOUT_LAYOUTDIR_RTL = 0x02 << SCREENLAYOUT_LAYOUTDIR_SHIFT;

    /**
     * Constant for {@link #screenLayout}: bits that encode roundness of the screen.
     */
    public static final int SCREENLAYOUT_ROUND_MASK = 0x300;
    /**
     * Constant for {@link #screenLayout}: bit shift to get to screen roundness bits
     */
    public static final int SCREENLAYOUT_ROUND_SHIFT = 8;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_ROUND_MASK} value indicating
     * that it is unknown whether or not the screen has a round shape.
     */
    public static final int SCREENLAYOUT_ROUND_UNDEFINED = 0x00;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_ROUND_MASK} value indicating
     * that the screen does not have a rounded shape.
     */
    public static final int SCREENLAYOUT_ROUND_NO = 0x1 << SCREENLAYOUT_ROUND_SHIFT;
    /**
     * Constant for {@link #screenLayout}: a {@link #SCREENLAYOUT_ROUND_MASK} value indicating
     * that the screen has a rounded shape. Corners may not be visible to the user;
     * developers should pay special attention to the {@link android.view.WindowInsets} delivered
     * to views for more information about ensuring content is not obscured.
     * <p/>
     * <p>Corresponds to the <code>-round</code> resource qualifier.</p>
     */
    public static final int SCREENLAYOUT_ROUND_YES = 0x2 << SCREENLAYOUT_ROUND_SHIFT;

    /**
     * Constant for {@link #screenLayout}: a value indicating that screenLayout is undefined
     */
    public static final int SCREENLAYOUT_UNDEFINED = SCREENLAYOUT_SIZE_UNDEFINED |
            SCREENLAYOUT_LONG_UNDEFINED | SCREENLAYOUT_LAYOUTDIR_UNDEFINED |
            SCREENLAYOUT_ROUND_UNDEFINED;

    /**
     * Special flag we generate to indicate that the screen layout requires
     * us to use a compatibility mode for apps that are not modern layout
     * aware.
     */
    public static final int SCREENLAYOUT_COMPAT_NEEDED = 0x10000000;

    /**
     * Bit mask of overall layout of the screen.  Currently there are two
     * fields:
     * <p>The {@link #SCREENLAYOUT_SIZE_MASK} bits define the overall size
     * of the screen.  They may be one of
     * {@link #SCREENLAYOUT_SIZE_SMALL}, {@link #SCREENLAYOUT_SIZE_NORMAL},
     * {@link #SCREENLAYOUT_SIZE_LARGE}, or {@link #SCREENLAYOUT_SIZE_XLARGE}.</p>
     * <p/>
     * <p>The {@link #SCREENLAYOUT_LONG_MASK} defines whether the screen
     * is wider/taller than normal.  They may be one of
     * {@link #SCREENLAYOUT_LONG_NO} or {@link #SCREENLAYOUT_LONG_YES}.</p>
     * <p/>
     * <p>The {@link #SCREENLAYOUT_LAYOUTDIR_MASK} defines whether the screen layout
     * is either LTR or RTL.  They may be one of
     * {@link #SCREENLAYOUT_LAYOUTDIR_LTR} or {@link #SCREENLAYOUT_LAYOUTDIR_RTL}.</p>
     * <p/>
     * <p>The {@link #SCREENLAYOUT_ROUND_MASK} defines whether the screen has a rounded
     * shape. They may be one of {@link #SCREENLAYOUT_ROUND_NO} or {@link #SCREENLAYOUT_ROUND_YES}.
     * </p>
     * <p/>
     * <p>See <a href="{@docRoot}guide/practices/screens_support.html">Supporting
     * Multiple Screens</a> for more information.</p>
     */
    public int screenLayout;

    /**
     * Constant for {@link #touchscreen}: a value indicating that no value has been set.
     */
    public static final int TOUCHSCREEN_UNDEFINED = 0;
    /**
     * Constant for {@link #touchscreen}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#TouchscreenQualifier">notouch</a>
     * resource qualifier.
     */
    public static final int TOUCHSCREEN_NOTOUCH = 1;
    /**
     * @deprecated Not currently supported or used.
     */
    @Deprecated
    public static final int TOUCHSCREEN_STYLUS = 2;
    /**
     * Constant for {@link #touchscreen}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#TouchscreenQualifier">finger</a>
     * resource qualifier.
     */
    public static final int TOUCHSCREEN_FINGER = 3;

    /**
     * The kind of touch screen attached to the device.
     * One of: {@link #TOUCHSCREEN_NOTOUCH}, {@link #TOUCHSCREEN_FINGER}.
     */
    public int touchscreen;

    /**
     * Constant for {@link #keyboard}: a value indicating that no value has been set.
     */
    public static final int KEYBOARD_UNDEFINED = 0;
    /**
     * Constant for {@link #keyboard}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ImeQualifier">nokeys</a>
     * resource qualifier.
     */
    public static final int KEYBOARD_NOKEYS = 1;
    /**
     * Constant for {@link #keyboard}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ImeQualifier">qwerty</a>
     * resource qualifier.
     */
    public static final int KEYBOARD_QWERTY = 2;
    /**
     * Constant for {@link #keyboard}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ImeQualifier">12key</a>
     * resource qualifier.
     */
    public static final int KEYBOARD_12KEY = 3;

    /**
     * The kind of keyboard attached to the device.
     * One of: {@link #KEYBOARD_NOKEYS}, {@link #KEYBOARD_QWERTY},
     * {@link #KEYBOARD_12KEY}.
     */
    public int keyboard;

    /**
     * Constant for {@link #keyboardHidden}: a value indicating that no value has been set.
     */
    public static final int KEYBOARDHIDDEN_UNDEFINED = 0;
    /**
     * Constant for {@link #keyboardHidden}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#KeyboardAvailQualifier">keysexposed</a>
     * resource qualifier.
     */
    public static final int KEYBOARDHIDDEN_NO = 1;
    /**
     * Constant for {@link #keyboardHidden}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#KeyboardAvailQualifier">keyshidden</a>
     * resource qualifier.
     */
    public static final int KEYBOARDHIDDEN_YES = 2;
    /**
     * Constant matching actual resource implementation.
     */
    public static final int KEYBOARDHIDDEN_SOFT = 3;

    /**
     * A flag indicating whether any keyboard is available.  Unlike
     * {@link #hardKeyboardHidden}, this also takes into account a soft
     * keyboard, so if the hard keyboard is hidden but there is soft
     * keyboard available, it will be set to NO.  Value is one of:
     * {@link #KEYBOARDHIDDEN_NO}, {@link #KEYBOARDHIDDEN_YES}.
     */
    public int keyboardHidden;

    /**
     * Constant for {@link #hardKeyboardHidden}: a value indicating that no value has been set.
     */
    public static final int HARDKEYBOARDHIDDEN_UNDEFINED = 0;
    /**
     * Constant for {@link #hardKeyboardHidden}, value corresponding to the
     * physical keyboard being exposed.
     */
    public static final int HARDKEYBOARDHIDDEN_NO = 1;
    /**
     * Constant for {@link #hardKeyboardHidden}, value corresponding to the
     * physical keyboard being hidden.
     */
    public static final int HARDKEYBOARDHIDDEN_YES = 2;

    /**
     * A flag indicating whether the hard keyboard has been hidden.  This will
     * be set on a device with a mechanism to hide the keyboard from the
     * user, when that mechanism is closed.  One of:
     * {@link #HARDKEYBOARDHIDDEN_NO}, {@link #HARDKEYBOARDHIDDEN_YES}.
     */
    public int hardKeyboardHidden;

    /**
     * Constant for {@link #navigation}: a value indicating that no value has been set.
     */
    public static final int NAVIGATION_UNDEFINED = 0;
    /**
     * Constant for {@link #navigation}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NavigationQualifier">nonav</a>
     * resource qualifier.
     */
    public static final int NAVIGATION_NONAV = 1;
    /**
     * Constant for {@link #navigation}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NavigationQualifier">dpad</a>
     * resource qualifier.
     */
    public static final int NAVIGATION_DPAD = 2;
    /**
     * Constant for {@link #navigation}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NavigationQualifier">trackball</a>
     * resource qualifier.
     */
    public static final int NAVIGATION_TRACKBALL = 3;
    /**
     * Constant for {@link #navigation}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NavigationQualifier">wheel</a>
     * resource qualifier.
     */
    public static final int NAVIGATION_WHEEL = 4;

    /**
     * The kind of navigation method available on the device.
     * One of: {@link #NAVIGATION_NONAV}, {@link #NAVIGATION_DPAD},
     * {@link #NAVIGATION_TRACKBALL}, {@link #NAVIGATION_WHEEL}.
     */
    public int navigation;

    /**
     * Constant for {@link #navigationHidden}: a value indicating that no value has been set.
     */
    public static final int NAVIGATIONHIDDEN_UNDEFINED = 0;
    /**
     * Constant for {@link #navigationHidden}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NavAvailQualifier">navexposed</a>
     * resource qualifier.
     */
    public static final int NAVIGATIONHIDDEN_NO = 1;
    /**
     * Constant for {@link #navigationHidden}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NavAvailQualifier">navhidden</a>
     * resource qualifier.
     */
    public static final int NAVIGATIONHIDDEN_YES = 2;

    /**
     * A flag indicating whether any 5-way or DPAD navigation available.
     * This will be set on a device with a mechanism to hide the navigation
     * controls from the user, when that mechanism is closed.  One of:
     * {@link #NAVIGATIONHIDDEN_NO}, {@link #NAVIGATIONHIDDEN_YES}.
     */
    public int navigationHidden;

    /**
     * Constant for {@link #orientation}: a value indicating that no value has been set.
     */
    public static final int ORIENTATION_UNDEFINED = 0;
    /**
     * Constant for {@link #orientation}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#OrientationQualifier">port</a>
     * resource qualifier.
     */
    public static final int ORIENTATION_PORTRAIT = 1;
    /**
     * Constant for {@link #orientation}, value corresponding to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#OrientationQualifier">land</a>
     * resource qualifier.
     */
    public static final int ORIENTATION_LANDSCAPE = 2;
    /**
     * @deprecated Not currently supported or used.
     */
    @Deprecated
    public static final int ORIENTATION_SQUARE = 3;

    /**
     * Overall orientation of the screen.  May be one of
     * {@link #ORIENTATION_LANDSCAPE}, {@link #ORIENTATION_PORTRAIT}.
     */
    public int orientation;

    /**
     * Constant for {@link #uiMode}: bits that encode the mode type.
     */
    public static final int UI_MODE_TYPE_MASK = 0x0f;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value indicating that no mode type has been set.
     */
    public static final int UI_MODE_TYPE_UNDEFINED = 0x00;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value that corresponds to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#UiModeQualifier">no
     * UI mode</a> resource qualifier specified.
     */
    public static final int UI_MODE_TYPE_NORMAL = 0x01;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#UiModeQualifier">desk</a>
     * resource qualifier.
     */
    public static final int UI_MODE_TYPE_DESK = 0x02;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#UiModeQualifier">car</a>
     * resource qualifier.
     */
    public static final int UI_MODE_TYPE_CAR = 0x03;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#UiModeQualifier">television</a>
     * resource qualifier.
     */
    public static final int UI_MODE_TYPE_TELEVISION = 0x04;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#UiModeQualifier">appliance</a>
     * resource qualifier.
     */
    public static final int UI_MODE_TYPE_APPLIANCE = 0x05;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_TYPE_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#UiModeQualifier">watch</a>
     * resource qualifier.
     */
    public static final int UI_MODE_TYPE_WATCH = 0x06;

    /**
     * Constant for {@link #uiMode}: bits that encode the night mode.
     */
    public static final int UI_MODE_NIGHT_MASK = 0x30;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_NIGHT_MASK}
     * value indicating that no mode type has been set.
     */
    public static final int UI_MODE_NIGHT_UNDEFINED = 0x00;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_NIGHT_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NightQualifier">notnight</a>
     * resource qualifier.
     */
    public static final int UI_MODE_NIGHT_NO = 0x10;
    /**
     * Constant for {@link #uiMode}: a {@link #UI_MODE_NIGHT_MASK}
     * value that corresponds to the
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#NightQualifier">night</a>
     * resource qualifier.
     */
    public static final int UI_MODE_NIGHT_YES = 0x20;

    /**
     * Bit mask of the ui mode.  Currently there are two fields:
     * <p>The {@link #UI_MODE_TYPE_MASK} bits define the overall ui mode of the
     * device. They may be one of {@link #UI_MODE_TYPE_UNDEFINED},
     * {@link #UI_MODE_TYPE_NORMAL}, {@link #UI_MODE_TYPE_DESK},
     * {@link #UI_MODE_TYPE_CAR}, {@link #UI_MODE_TYPE_TELEVISION},
     * {@link #UI_MODE_TYPE_APPLIANCE}, or {@link #UI_MODE_TYPE_WATCH}.
     * <p/>
     * <p>The {@link #UI_MODE_NIGHT_MASK} defines whether the screen
     * is in a special mode. They may be one of {@link #UI_MODE_NIGHT_UNDEFINED},
     * {@link #UI_MODE_NIGHT_NO} or {@link #UI_MODE_NIGHT_YES}.
     */
    public int uiMode;

    /**
     * Default value for {@link #screenWidthDp} indicating that no width
     * has been specified.
     */
    public static final int SCREEN_WIDTH_DP_UNDEFINED = 0;

    /**
     * The current width of the available screen space, in dp units,
     * corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenWidthQualifier">screen
     * width</a> resource qualifier.  Set to
     * {@link #SCREEN_WIDTH_DP_UNDEFINED} if no width is specified.
     */
    public int screenWidthDp;

    /**
     * Default value for {@link #screenHeightDp} indicating that no width
     * has been specified.
     */
    public static final int SCREEN_HEIGHT_DP_UNDEFINED = 0;

    /**
     * The current height of the available screen space, in dp units,
     * corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#ScreenHeightQualifier">screen
     * height</a> resource qualifier.  Set to
     * {@link #SCREEN_HEIGHT_DP_UNDEFINED} if no height is specified.
     */
    public int screenHeightDp;

    /**
     * Default value for {@link #smallestScreenWidthDp} indicating that no width
     * has been specified.
     */
    public static final int SMALLEST_SCREEN_WIDTH_DP_UNDEFINED = 0;

    /**
     * The smallest screen size an application will see in normal operation,
     * corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#SmallestScreenWidthQualifier">smallest
     * screen width</a> resource qualifier.
     * This is the smallest value of both screenWidthDp and screenHeightDp
     * in both portrait and landscape.  Set to
     * {@link #SMALLEST_SCREEN_WIDTH_DP_UNDEFINED} if no width is specified.
     */
    public int smallestScreenWidthDp;

    /**
     * Default value for {@link #densityDpi} indicating that no width
     * has been specified.
     */
    public static final int DENSITY_DPI_UNDEFINED = 0;

    /**
     * Value for {@link #densityDpi} for resources that scale to any density (vector drawables).
     */
    public static final int DENSITY_DPI_ANY = 0xfffe;

    /**
     * Value for {@link #densityDpi} for resources that are not meant to be scaled.
     */
    public static final int DENSITY_DPI_NONE = 0xffff;

    public static final int DENSITY_DPI_LOW = 120;
    public static final int DENSITY_DPI_MEDIUM = 160;
    public static final int DENSITY_DPI_TV = 213;
    public static final int DENSITY_DPI_HIGH = 240;
    public static final int DENSITY_DPI_XHIGH = 320;
    public static final int DENSITY_DPI_XXHIGH = 480;
    public static final int DENSITY_DPI_XXXHIGH = 640;

    /**
     * The target screen density being rendered to,
     * corresponding to
     * <a href="{@docRoot}guide/topics/resources/providing-resources.html#DensityQualifier">density</a>
     * resource qualifier.  Set to
     * {@link #DENSITY_DPI_UNDEFINED} if no density is specified.
     */
    public int densityDpi;

    public static final int SDK_VERSION_UNDEFINED = 0;

    public int sdkVersion;

    /**
     * Construct an invalid Configuration.  You must call {@link #setToDefaults}
     * for this object to be valid.  {@more}
     */
    public CodeColorsConfiguration() {
        setToDefaults();
    }

    /**
     * Makes a deep copy suitable for modification.
     */
    public CodeColorsConfiguration(CodeColorsConfiguration o) {
        setTo(o);
    }

    public CodeColorsConfiguration(int sdkVersion, float fontScale, int mcc, int mnc, String localeLanguage,
                                   String localeCountry, String localeVariant, boolean userSetLocale, int touchscreen,
                                   int keyboard, int keyboardHidden, int hardKeyboardHidden, int navigation,
                                   int navigationHidden, int orientation, int screenLayout, int uiMode,
                                   int screenWidthDp, int screenHeightDp, int smallestScreenWidthDp, int densityDpi) {
        this.sdkVersion = sdkVersion;
        this.fontScale = fontScale;
        this.mcc = mcc;
        this.mnc = mnc;
        if (localeLanguage != null) {
            if (localeCountry != null) {
                if (localeVariant != null) {
                    this.locale = new Locale(localeLanguage, localeCountry, localeVariant);
                } else {
                    this.locale = new Locale(localeLanguage, localeCountry);
                }
            } else {
                this.locale = new Locale(localeLanguage);
            }
        }
        this.userSetLocale = userSetLocale;
        this.touchscreen = touchscreen;
        this.keyboard = keyboard;
        this.keyboardHidden = keyboardHidden;
        this.hardKeyboardHidden = hardKeyboardHidden;
        this.navigation = navigation;
        this.navigationHidden = navigationHidden;
        this.orientation = orientation;
        this.screenLayout = screenLayout;
        this.uiMode = uiMode;
        this.screenWidthDp = screenWidthDp;
        this.screenHeightDp = screenHeightDp;
        this.smallestScreenWidthDp = smallestScreenWidthDp;
        this.densityDpi = densityDpi;
    }

    public void setTo(CodeColorsConfiguration o) {
        sdkVersion = o.sdkVersion;
        fontScale = o.fontScale;
        mcc = o.mcc;
        mnc = o.mnc;
        if (o.locale != null) {
            locale = (Locale) o.locale.clone();
        }
        userSetLocale = o.userSetLocale;
        touchscreen = o.touchscreen;
        keyboard = o.keyboard;
        keyboardHidden = o.keyboardHidden;
        hardKeyboardHidden = o.hardKeyboardHidden;
        navigation = o.navigation;
        navigationHidden = o.navigationHidden;
        orientation = o.orientation;
        screenLayout = o.screenLayout;
        uiMode = o.uiMode;
        screenWidthDp = o.screenWidthDp;
        screenHeightDp = o.screenHeightDp;
        smallestScreenWidthDp = o.smallestScreenWidthDp;
        densityDpi = o.densityDpi;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        buildString(sb);
        sb.append('}');
        return sb.toString();
    }

    protected void buildString(StringBuilder sb) {
        sb.append(fontScale);
        sb.append(" ");
        if (mcc != 0) {
            sb.append(mcc);
            sb.append("mcc");
        } else {
            sb.append("?mcc");
        }
        if (mnc != 0) {
            sb.append(mnc);
            sb.append("mnc");
        } else {
            sb.append("?mnc");
        }
        if (locale != null) {
            sb.append(" ");
            sb.append(locale);
        } else {
            sb.append(" ?locale");
        }
        int layoutDir = (screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK);
        switch (layoutDir) {
            case SCREENLAYOUT_LAYOUTDIR_UNDEFINED:
                sb.append(" ?layoutDir");
                break;
            case SCREENLAYOUT_LAYOUTDIR_LTR:
                sb.append(" ldltr");
                break;
            case SCREENLAYOUT_LAYOUTDIR_RTL:
                sb.append(" ldrtl");
                break;
            default:
                sb.append(" layoutDir=");
                sb.append(layoutDir >> SCREENLAYOUT_LAYOUTDIR_SHIFT);
                break;
        }
        if (smallestScreenWidthDp != SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            sb.append(" sw");
            sb.append(smallestScreenWidthDp);
            sb.append("dp");
        } else {
            sb.append(" ?swdp");
        }
        if (screenWidthDp != SCREEN_WIDTH_DP_UNDEFINED) {
            sb.append(" w");
            sb.append(screenWidthDp);
            sb.append("dp");
        } else {
            sb.append(" ?wdp");
        }
        if (screenHeightDp != SCREEN_HEIGHT_DP_UNDEFINED) {
            sb.append(" h");
            sb.append(screenHeightDp);
            sb.append("dp");
        } else {
            sb.append(" ?hdp");
        }
        if (densityDpi != DENSITY_DPI_UNDEFINED) {
            sb.append(" ");
            sb.append(densityDpi);
            sb.append("dpi");
        } else {
            sb.append(" ?density");
        }
        switch ((screenLayout & SCREENLAYOUT_SIZE_MASK)) {
            case SCREENLAYOUT_SIZE_UNDEFINED:
                sb.append(" ?lsize");
                break;
            case SCREENLAYOUT_SIZE_SMALL:
                sb.append(" smll");
                break;
            case SCREENLAYOUT_SIZE_NORMAL:
                sb.append(" nrml");
                break;
            case SCREENLAYOUT_SIZE_LARGE:
                sb.append(" lrg");
                break;
            case SCREENLAYOUT_SIZE_XLARGE:
                sb.append(" xlrg");
                break;
            default:
                sb.append(" layoutSize=");
                sb.append(screenLayout & SCREENLAYOUT_SIZE_MASK);
                break;
        }
        switch ((screenLayout & SCREENLAYOUT_LONG_MASK)) {
            case SCREENLAYOUT_LONG_UNDEFINED:
                sb.append(" ?long");
                break;
            case SCREENLAYOUT_LONG_NO: /* not-long is not interesting to print */
                break;
            case SCREENLAYOUT_LONG_YES:
                sb.append(" long");
                break;
            default:
                sb.append(" layoutLong=");
                sb.append(screenLayout & SCREENLAYOUT_LONG_MASK);
                break;
        }
        switch (orientation) {
            case ORIENTATION_UNDEFINED:
                sb.append(" ?orien");
                break;
            case ORIENTATION_LANDSCAPE:
                sb.append(" land");
                break;
            case ORIENTATION_PORTRAIT:
                sb.append(" port");
                break;
            default:
                sb.append(" orien=");
                sb.append(orientation);
                break;
        }
        switch ((uiMode & UI_MODE_TYPE_MASK)) {
            case UI_MODE_TYPE_UNDEFINED:
                sb.append(" ?uimode");
                break;
            case UI_MODE_TYPE_NORMAL: /* normal is not interesting to print */
                break;
            case UI_MODE_TYPE_DESK:
                sb.append(" desk");
                break;
            case UI_MODE_TYPE_CAR:
                sb.append(" car");
                break;
            case UI_MODE_TYPE_TELEVISION:
                sb.append(" television");
                break;
            case UI_MODE_TYPE_APPLIANCE:
                sb.append(" appliance");
                break;
            case UI_MODE_TYPE_WATCH:
                sb.append(" watch");
                break;
            default:
                sb.append(" uimode=");
                sb.append(uiMode & UI_MODE_TYPE_MASK);
                break;
        }
        switch ((uiMode & UI_MODE_NIGHT_MASK)) {
            case UI_MODE_NIGHT_UNDEFINED:
                sb.append(" ?night");
                break;
            case UI_MODE_NIGHT_NO: /* not-night is not interesting to print */
                break;
            case UI_MODE_NIGHT_YES:
                sb.append(" night");
                break;
            default:
                sb.append(" night=");
                sb.append(uiMode & UI_MODE_NIGHT_MASK);
                break;
        }
        switch (touchscreen) {
            case TOUCHSCREEN_UNDEFINED:
                sb.append(" ?touch");
                break;
            case TOUCHSCREEN_NOTOUCH:
                sb.append(" -touch");
                break;
            case TOUCHSCREEN_STYLUS:
                sb.append(" stylus");
                break;
            case TOUCHSCREEN_FINGER:
                sb.append(" finger");
                break;
            default:
                sb.append(" touch=");
                sb.append(touchscreen);
                break;
        }
        switch (keyboard) {
            case KEYBOARD_UNDEFINED:
                sb.append(" ?keyb");
                break;
            case KEYBOARD_NOKEYS:
                sb.append(" -keyb");
                break;
            case KEYBOARD_QWERTY:
                sb.append(" qwerty");
                break;
            case KEYBOARD_12KEY:
                sb.append(" 12key");
                break;
            default:
                sb.append(" keys=");
                sb.append(keyboard);
                break;
        }
        switch (keyboardHidden) {
            case KEYBOARDHIDDEN_UNDEFINED:
                sb.append("/?");
                break;
            case KEYBOARDHIDDEN_NO:
                sb.append("/v");
                break;
            case KEYBOARDHIDDEN_YES:
                sb.append("/h");
                break;
            case KEYBOARDHIDDEN_SOFT:
                sb.append("/s");
                break;
            default:
                sb.append("/");
                sb.append(keyboardHidden);
                break;
        }
        switch (hardKeyboardHidden) {
            case HARDKEYBOARDHIDDEN_UNDEFINED:
                sb.append("/?");
                break;
            case HARDKEYBOARDHIDDEN_NO:
                sb.append("/v");
                break;
            case HARDKEYBOARDHIDDEN_YES:
                sb.append("/h");
                break;
            default:
                sb.append("/");
                sb.append(hardKeyboardHidden);
                break;
        }
        switch (navigation) {
            case NAVIGATION_UNDEFINED:
                sb.append(" ?nav");
                break;
            case NAVIGATION_NONAV:
                sb.append(" -nav");
                break;
            case NAVIGATION_DPAD:
                sb.append(" dpad");
                break;
            case NAVIGATION_TRACKBALL:
                sb.append(" tball");
                break;
            case NAVIGATION_WHEEL:
                sb.append(" wheel");
                break;
            default:
                sb.append(" nav=");
                sb.append(navigation);
                break;
        }
        switch (navigationHidden) {
            case NAVIGATIONHIDDEN_UNDEFINED:
                sb.append("/?");
                break;
            case NAVIGATIONHIDDEN_NO:
                sb.append("/v");
                break;
            case NAVIGATIONHIDDEN_YES:
                sb.append("/h");
                break;
            default:
                sb.append("/");
                sb.append(navigationHidden);
                break;
        }
    }

    /**
     * Set this object to the system defaults.
     */
    public void setToDefaults() {
        sdkVersion = SDK_VERSION_UNDEFINED;
        fontScale = 1;
        mcc = mnc = 0;
        locale = null;
        userSetLocale = false;
        touchscreen = TOUCHSCREEN_UNDEFINED;
        keyboard = KEYBOARD_UNDEFINED;
        keyboardHidden = KEYBOARDHIDDEN_UNDEFINED;
        hardKeyboardHidden = HARDKEYBOARDHIDDEN_UNDEFINED;
        navigation = NAVIGATION_UNDEFINED;
        navigationHidden = NAVIGATIONHIDDEN_UNDEFINED;
        orientation = ORIENTATION_UNDEFINED;
        screenLayout = SCREENLAYOUT_UNDEFINED;
        uiMode = UI_MODE_TYPE_UNDEFINED;
        screenWidthDp = SCREEN_WIDTH_DP_UNDEFINED;
        screenHeightDp = SCREEN_HEIGHT_DP_UNDEFINED;
        smallestScreenWidthDp = SMALLEST_SCREEN_WIDTH_DP_UNDEFINED;
        densityDpi = DENSITY_DPI_UNDEFINED;
    }

    public int compareTo(CodeColorsConfiguration that) {
        int n;
        n = that.sdkVersion - this.sdkVersion;
        if (n != 0) return n;
        float a = this.fontScale;
        float b = that.fontScale;
        if (a < b) return -1;
        if (a > b) return 1;
        n = this.mcc - that.mcc;
        if (n != 0) return n;
        n = this.mnc - that.mnc;
        if (n != 0) return n;
        if (this.locale == null) {
            if (that.locale != null) return 1;
        } else if (that.locale == null) {
            return -1;
        } else {
            n = this.locale.getLanguage().compareTo(that.locale.getLanguage());
            if (n != 0) return n;
            n = this.locale.getCountry().compareTo(that.locale.getCountry());
            if (n != 0) return n;
            n = this.locale.getVariant().compareTo(that.locale.getVariant());
            if (n != 0) return n;
        }
        n = this.touchscreen - that.touchscreen;
        if (n != 0) return n;
        n = this.keyboard - that.keyboard;
        if (n != 0) return n;
        n = this.keyboardHidden - that.keyboardHidden;
        if (n != 0) return n;
        n = this.hardKeyboardHidden - that.hardKeyboardHidden;
        if (n != 0) return n;
        n = this.navigation - that.navigation;
        if (n != 0) return n;
        n = this.navigationHidden - that.navigationHidden;
        if (n != 0) return n;
        n = this.orientation - that.orientation;
        if (n != 0) return n;
        n = this.screenLayout - that.screenLayout;
        if (n != 0) return n;
        n = this.uiMode - that.uiMode;
        if (n != 0) return n;
        n = this.screenWidthDp - that.screenWidthDp;
        if (n != 0) return n;
        n = this.screenHeightDp - that.screenHeightDp;
        if (n != 0) return n;
        n = this.smallestScreenWidthDp - that.smallestScreenWidthDp;
        if (n != 0) return n;
        n = this.densityDpi - that.densityDpi;
        if (n != 0) return n;

        return n;
    }

    public boolean equals(CodeColorsConfiguration that) {
        if (that == null) return false;
        if (that == this) return true;
        return this.compareTo(that) == 0;
    }

    public boolean equals(Object that) {
        try {
            return equals((CodeColorsConfiguration) that);
        } catch (ClassCastException e) {
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + sdkVersion;
        result = 31 * result + Float.floatToIntBits(fontScale);
        result = 31 * result + mcc;
        result = 31 * result + mnc;
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + touchscreen;
        result = 31 * result + keyboard;
        result = 31 * result + keyboardHidden;
        result = 31 * result + hardKeyboardHidden;
        result = 31 * result + navigation;
        result = 31 * result + navigationHidden;
        result = 31 * result + orientation;
        result = 31 * result + screenLayout;
        result = 31 * result + uiMode;
        result = 31 * result + screenWidthDp;
        result = 31 * result + screenHeightDp;
        result = 31 * result + smallestScreenWidthDp;
        result = 31 * result + densityDpi;
        return result;
    }
}
