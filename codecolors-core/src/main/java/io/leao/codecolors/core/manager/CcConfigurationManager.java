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

        if (mConfiguration == null) {
            mConfiguration = new Configuration(configuration);
            onConfigurationCreated(resources, configuration);
        } else if (!configuration.equals(mConfiguration)) {
            mConfiguration.setTo(configuration);
            onConfigurationChanged(resources, configuration);
        }
    }

    protected synchronized void onConfigurationCreated(Resources resources, Configuration configuration) {
        CcCore.getColorsManager().onConfigurationCreated(resources, configuration);
        CcCore.getDependenciesManager().onConfigurationCreated(configuration);
    }

    protected synchronized void onConfigurationChanged(Resources resources, Configuration configuration) {
        CcCore.getColorsManager().onConfigurationChanged(resources, configuration);
        CcCore.getDependenciesManager().onConfigurationChanged(configuration);
    }
}
