package io.leao.codecolors.core.color;

import android.content.res.Configuration;

import io.leao.codecolors.CodeColors;

/**
 * Adapter to manipulate code-colors when the configuration changes.
 * <p>
 * {@link #onConfigurationCreated(Configuration, CcColorStateList, int)} is called when the library is first set.
 * <p>
 * Tip: initialize in {@link CodeColors.Callback#onCodeColorsStarted()} to make sure it is called when the colors are
 * created.
 */
public interface CcColorAdapter {
    void onConfigurationCreated(Configuration config, CcColorStateList color, int colorResId);

    void onConfigurationChanged(Configuration config, CcColorStateList color, int colorResId);
}
