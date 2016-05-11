package io.leao.codecolors.plugin.android;

import java.io.File;

import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.plugin.res.DependenciesParser;
import io.leao.codecolors.plugin.res.Resource;

public class AndroidSdkDependenciesParser extends DependenciesParser {
    private int mAndroidSdkVersion;

    public AndroidSdkDependenciesParser(Resource.Pool resourcesPool, int androidSdkVersion) {
        super(resourcesPool);
        mAndroidSdkVersion = androidSdkVersion;
    }

    @Override
    public CcConfiguration createTrail(File folder, CcConfiguration trail) {
        CcConfiguration configuration = super.createTrail(folder, trail);
        if (configuration != null && configuration.sdkVersion == CcConfiguration.SDK_VERSION_UNDEFINED) {
            configuration.sdkVersion = mAndroidSdkVersion;
        }
        return configuration;
    }
}