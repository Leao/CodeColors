package io.leao.codecolors.plugin.aapt;

import java.util.regex.Pattern;

public class AaptUtil {
    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static String[] splitAndLowerCase(String str, String separator) {
        return str.toLowerCase().split(Pattern.quote(separator));
    }
}