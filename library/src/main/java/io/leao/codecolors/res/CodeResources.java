package io.leao.codecolors.res;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.TypedValue;

/**
 * Util class to retrieve drawables without tampering with {@link Resources} caches.
 */
public class CodeResources {
    private static TypedValue sTempValue;

    public static long createKey(Resources resources, int resId) {
        return createKey(getValue(resources, resId, true));
    }

    protected static long createKey(TypedValue value) {
        return (((long) value.assetCookie) << 32) | value.data;
    }

    public static TypedValue getValue(Resources resources, int id, boolean resolveRefs) {
        TypedValue value = sTempValue;
        if (value == null) {
            sTempValue = value = new TypedValue();
        }
        resources.getValue(id, value, resolveRefs);
        return value;
    }

    @Nullable
    public static ColorStateList loadColorStateList(Resources resources, int id, Resources.Theme theme)
            throws Resources.NotFoundException {
        TypedValue value = getValue(resources, id, true);

        // Handle inline color definitions.
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return ColorStateList.valueOf(value.data);
        } else {
            return loadColorStateListForCookie(resources, value, id, theme);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected static ColorStateList loadColorStateListForCookie(Resources resources, TypedValue value, int id,
                                                                Resources.Theme theme) {
        if (value.string == null) {
            throw new UnsupportedOperationException("Can't convert to color state list: type=0x" + value.type);
        }

        final String file = value.string.toString();

        final ColorStateList csl;

        if (file.endsWith(".xml")) {
            try {
                final XmlResourceParser rp = resources.getXml(id);
                csl = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        ColorStateList.createFromXml(resources, rp, theme) :
                        ColorStateList.createFromXml(resources, rp);
                rp.close();
            } catch (Exception e) {
                final Resources.NotFoundException rnf = new Resources.NotFoundException(
                        "File " + file + " from color state list resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        } else {
            throw new Resources.NotFoundException(
                    "File " + file + " from drawable resource ID #0x"
                            + Integer.toHexString(id) + ": .xml extension required");
        }

        return csl;
    }
}
