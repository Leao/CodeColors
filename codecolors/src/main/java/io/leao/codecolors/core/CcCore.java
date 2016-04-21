package io.leao.codecolors.core;

import io.leao.codecolors.core.manager.CcCallbackManager;
import io.leao.codecolors.core.manager.CcColorsManager;
import io.leao.codecolors.core.manager.CcConfigurationManager;
import io.leao.codecolors.core.manager.CcDependenciesManager;
import io.leao.codecolors.core.manager.CcSetupManager;

public class CcCore {
    private final static CcSetupManager sSetupManager;
    private final static CcDependenciesManager sDependenciesManager;
    private final static CcColorsManager sColorsManager;
    private final static CcCallbackManager sCallbackManager;
    private final static CcConfigurationManager sConfigurationManager;

    static {
        sSetupManager = new CcSetupManager();
        sDependenciesManager = new CcDependenciesManager();
        sColorsManager = new CcColorsManager();
        sCallbackManager = new CcCallbackManager();
        sConfigurationManager = new CcConfigurationManager();
    }

    public static CcSetupManager getSetupManager() {
        return sSetupManager;
    }

    public static CcDependenciesManager getDependenciesManager() {
        return sDependenciesManager;
    }

    public static CcColorsManager getColorsManager() {
        return sColorsManager;
    }

    public static CcCallbackManager getCallbackManager() {
        return sCallbackManager;
    }

    public static CcConfigurationManager getConfigurationManager() {
        return sConfigurationManager;
    }
}
