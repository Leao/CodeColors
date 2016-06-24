package io.leao.codecolors.core;

import io.leao.codecolors.core.color.CcColorManager;
import io.leao.codecolors.core.editor.CcEditorManager;
import io.leao.codecolors.core.inflate.CcInflateManager;
import io.leao.codecolors.core.manager.CcActivityManager;
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
    private final static CcActivityManager sActivityManager;

    static {
        sSetupManager = new CcSetupManager();
        sDependencyManager = new CcDependencyManager();
        sColorManager = new CcColorManager();
        sEditorManager = new CcEditorManager();
        sInflateManager = new CcInflateManager();
        sActivityManager = new CcActivityManager();
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

    public static CcActivityManager getActivityManager() {
        return sActivityManager;
    }
}
