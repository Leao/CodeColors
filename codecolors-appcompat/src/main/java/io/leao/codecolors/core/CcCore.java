package io.leao.codecolors.core;

import io.leao.codecolors.appcompat.manager.CcAppCompatSetupManager;
import io.leao.codecolors.core.color.CcColorManager;
import io.leao.codecolors.core.editor.CcEditorManager;
import io.leao.codecolors.core.inflate.CcAppCompatInflateManager;
import io.leao.codecolors.core.inflate.CcInflateManager;
import io.leao.codecolors.core.manager.CcConfigurationManager;
import io.leao.codecolors.core.manager.CcDependencyManager;
import io.leao.codecolors.core.manager.CcSetupManager;

/**
 * {@link CcCore} is a hub class that has two different implementations, depending on the library version: 'codecolors'
 * or 'codecolors-appcompat'.
 */
public class CcCore {
    private final static CcSetupManager sSetupManager;
    private final static CcDependencyManager sDependencyManager;
    private final static CcColorManager sColorManager;
    private final static CcEditorManager sEditorManager;
    private final static CcInflateManager sInflateManager;
    private final static CcConfigurationManager sConfigurationManager;

    static {
        sSetupManager = new CcAppCompatSetupManager();
        sDependencyManager = new CcDependencyManager();
        sColorManager = new CcColorManager();
        sEditorManager = new CcEditorManager();
        sInflateManager = new CcAppCompatInflateManager();
        sConfigurationManager = new CcConfigurationManager();
    }

    public static CcSetupManager getSetupManager() {
        return sSetupManager;
    }

    public static CcDependencyManager getDependencyManager() {
        return sDependencyManager;
    }

    public static CcColorManager getColorManager() {
        return sColorManager;
    }

    public static CcEditorManager getEditorManager() {
        return sEditorManager;
    }

    public static CcInflateManager getInflateManager() {
        return sInflateManager;
    }

    public static CcConfigurationManager getConfigurationManager() {
        return sConfigurationManager;
    }
}