package io.leao.codecolors.core;

import io.leao.codecolors.core.manager.CcColorsManager;
import io.leao.codecolors.core.manager.CcConfigurationManager;
import io.leao.codecolors.core.manager.CcDependenciesManager;
import io.leao.codecolors.core.manager.CcSetupManager;
import io.leao.codecolors.core.manager.adapter.CcAdapterManager;
import io.leao.codecolors.core.manager.callback.CcCallbackManager;
import io.leao.codecolors.core.manager.editor.CcEditorManager;

/**
 * {@link CcCore} is a hub class that has two different implementations, depending on the library version: 'codecolors'
 * or 'codecolors-appcompat'.
 */
public class CcCore {
    private final static CcSetupManager sSetupManager;
    private final static CcDependenciesManager sDependenciesManager;
    private final static CcColorsManager sColorsManager;
    private final static CcEditorManager sEditorManager;
    private final static CcAdapterManager sAdapterManager;
    private final static CcCallbackManager sCallbackManager;
    private final static CcConfigurationManager sConfigurationManager;

    static {
        sSetupManager = new CcSetupManager();
        sDependenciesManager = new CcDependenciesManager();
        sColorsManager = new CcColorsManager();
        sEditorManager = new CcEditorManager();
        sAdapterManager = new CcAdapterManager();
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
