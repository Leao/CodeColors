package io.leao.codecolors.core.color;

import android.content.res.Configuration;

/**
 * Adapter to manipulate code-colors when the configuration changes.
 * <p>
 * {@link #onConfigurationCreated(Configuration, CcColorStateList, int)} is called when the library is first set.
 */
public interface CcColorAdapter {
    void onConfigurationCreated(Configuration config, CcColorStateList color, int colorResId);

    void onConfigurationChanged(Configuration config, CcColorStateList color, int colorResId);
}
