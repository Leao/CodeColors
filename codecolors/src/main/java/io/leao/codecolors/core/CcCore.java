package io.leao.codecolors.core;

import io.leao.codecolors.core.manager.CcColorManager;
import io.leao.codecolors.core.manager.CcConfigurationManager;
import io.leao.codecolors.core.manager.CcDependencyManager;
import io.leao.codecolors.core.manager.CcEditorManager;
import io.leao.codecolors.core.manager.CcSetupManager;
import io.leao.codecolors.core.manager.adapter.CcAdapterManager;
import io.leao.codecolors.core.manager.callback.CcCallbackManager;

/**
 * {@link CcCore} is a hub class that has two different implementations, depending on the library version: 'codecolors'
 * or 'codecolors-appcompat'.
 */
public class CcCore {
    private final static CcSetupManager sSetupManager;
    private final static CcDependencyManager sDependencyManager;
    private final static CcColorManager sColorManager;
    private final static CcEditorManager sEditorManager;
    private final static CcAdapterManager sAdapterManager;
    private final static CcCallbackManager sCallbackManager;
    private final static CcConfigurationManager sConfigurationManager;

    static {
        sSetupManager = new CcSetupManager();
        sDependencyManager = new CcDependencyManager();
        sColorManager = new CcColorManager();
        sEditorManager = new CcEditorManager();
        sAdapterManager = new CcAdapterManager();
        sCallbackManager = new CcCallbackManager();
        sConfigurationManager = new CcConfigurationManager();
    }

    public static CcSetupManager getSetupManager() {
        return sSetupManager;
    }

    public static CcDependencyManager getDependenciesManager() {
        return sDependencyManager;
    }

    public static CcColorManager getColorsManager() {
        return sColorManager;
    }

    public static CcEditorManager getEditorManager() {
        return sEditorManager;
    }

    public static CcAdapterManager getAdapterManager() {
        return sAdapterManager;
    }

    public static CcCallbackManager getCallbackManager() {
        return sCallbackManager;
    }

    public static CcConfigurationManager getConfigurationManager() {
        return sConfigurationManager;
    }
}
