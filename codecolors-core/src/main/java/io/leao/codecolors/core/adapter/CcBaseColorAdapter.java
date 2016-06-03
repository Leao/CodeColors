package io.leao.codecolors.core.adapter;

import android.content.res.ColorStateList;
import android.content.res.Configuration;

/**
 * Adapter to get the default color value of code-colors.
 * <p>
 * Called when the color is created and on every configuration change.
 */
public interface CcBaseColorAdapter {
    /**
     * @return the default color value (a {@link ColorStateList}) for the code-color with the the id {@code colorResId},
     * or {@code null} to use the default base color value, instead.
     */
    ColorStateList getBaseColor(int colorResId, Configuration config, ColorStateList defaultBaseColor);
}
