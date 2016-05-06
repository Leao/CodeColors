package io.leao.codecolors.core.manager;

import android.content.res.Configuration;
import android.content.res.Resources;

import io.leao.codecolors.core.CcCore;

/**
 * Called by the initial setup and by activities when the configuration possibly changed,
 * and propagates the call if it truly changed.
 */
public class CcConfigurationManager {
    private Configuration mConfiguration;

    public synchronized void onConfigurationChanged(Resources resources) {
        Configuration configuration = resources.getConfiguration();
        if (!configuration.equals(mConfiguration)) {
            if (mConfiguration == null) {
                mConfiguration = new Configuration(configuration);
            } else {
                mConfiguration.setTo(configuration);
            }

            CcCore.getColorsManager().onConfigurationChanged(resources);
            CcCore.getDependenciesManager().onConfigurationChanged(configuration);
        }
    }
}
