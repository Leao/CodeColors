package io.leao.codecolors.manager;

import android.content.res.Configuration;
import android.content.res.Resources;

public class CcConfigurationManager {
    private static CcConfigurationManager sInstance;

    private Configuration mConfiguration;

    public static CcConfigurationManager getInstance() {
        if (sInstance == null) {
            sInstance = new CcConfigurationManager();
        }
        return sInstance;
    }

    public synchronized void onConfigurationChanged(Resources resources) {
        Configuration configuration = resources.getConfiguration();
        if (!configuration.equals(mConfiguration)) {
            if (mConfiguration == null) {
                mConfiguration = new Configuration(configuration);
            } else {
                mConfiguration.setTo(configuration);
            }

            CcColorsManager.getInstance().onConfigurationChanged(resources);
            CcDependenciesManager.getInstance().onConfigurationChanged(configuration);
        }
    }
}
