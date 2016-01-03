package io.leao.codecolors.plugin.res;

public class ColorUtils {
    private static final String COLOR_DEFAULT_VALUE_NAME_BASE = "%s__default";
    private static final String COLOR_DEFAULT_VALUE_REFERENCE_NAME_BASE = "@color/%s__default";

    public static String getDefaultValue(String color) {
        return String.format(COLOR_DEFAULT_VALUE_NAME_BASE, color);
    }

    public static String getDefaultValueReference(String color) {
        return String.format(COLOR_DEFAULT_VALUE_REFERENCE_NAME_BASE, color);
    }
}
