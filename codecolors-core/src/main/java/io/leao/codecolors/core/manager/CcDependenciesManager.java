package io.leao.codecolors.core.manager;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.core.res.CcConfigurationUtils;
import io.leao.codecolors.core.util.CcTempUtils;
import io.leao.codecolors.plugin.CcConst;
import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcDependenciesManager {
    private static final String CLASS_NAME_BASE = "%s.%s";
    private static final String TYPE_ATTR = "attr";

    private CcConfiguration[] mConfigurations;
    private Map<Object, Object[][]> mResourceConfigurationDependencies;

    private Configuration mConfiguration;
    private Map<Object, Object[]> mResourceDependenciesCache = new HashMap<>();

    // Keys are either the id or the name of the resource.
    private Map<Integer, Object> mIdKey = new HashMap<>();
    private Map<Object, Integer> mKeyId = new HashMap<>();
    // If the ids are of attr type or not.
    private Map<Integer, Boolean> mIdIsAttr = new HashMap<>();

    private Map<Resources.Theme, Map<Integer, Integer>> mThemeResolvedAttrs = new HashMap<>();

    @SuppressWarnings("unchecked")
    public synchronized void init(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        /*
         * Initialize configurations and dependencies from the classes generated by the CodeColors plugin.
         */

        // CcDependencies.java
        Class<?> dependenciesClass =
                Class.forName(String.format(CLASS_NAME_BASE, packageName, CcConst.DEPENDENCIES_CLASS_NAME));

        // Configurations.
        Field configurationsField = dependenciesClass.getDeclaredField(CcConst.CONFIGURATIONS_FIELD_NAME);
        configurationsField.setAccessible(true);
        mConfigurations = (CcConfiguration[]) configurationsField.get(null);

        // Dependencies.
        Field dependenciesField = dependenciesClass.getDeclaredField(CcConst.DEPENDENCIES_FIELD_NAME);
        dependenciesField.setAccessible(true);
        mResourceConfigurationDependencies = (Map<Object, Object[][]>) dependenciesField.get(null);
    }

    public synchronized void onConfigurationChanged(Configuration configuration) {
        // Update configuration.
        mConfiguration = configuration;
        // Clear dependencies cache.
        mResourceDependenciesCache.clear();
    }

    public synchronized boolean hasDependencies(Resources resources, int resourceId) {
        Object[] dependencies = getDependencies(resources, resourceId);
        return dependencies != null && dependencies.length > 0;
    }

    public synchronized void getDependencies(Resources resources, int id, Set<Integer> outResolvedIds,
                                             Set<Integer> outUnresolvedAttrs) {
        Object[] dependencies = getDependencies(resources, id);
        if (dependencies != null) {
            for (Object dependency : dependencies) {
                int dependencyId = getId(resources, dependency);
                if (dependencyId != 0) {
                    Boolean isAttr = isAttr(resources, dependencyId);
                    if (isAttr != null) {
                        if (isAttr) {
                            outUnresolvedAttrs.add(dependencyId);
                        } else {
                            outResolvedIds.add(dependencyId);
                        }
                    }
                }
            }
        }
    }

    private synchronized Object[] getDependencies(Resources resources, int id) {
        Object key = getKey(resources, id);

        if (key != null) {
            if (mResourceDependenciesCache.containsKey(key)) {
                return mResourceDependenciesCache.get(key);
            } else {
                // First row is the list of configuration indexes, while the remaining rows are the dependencies.
                Object[][] configurationDependencies = mResourceConfigurationDependencies.get(key);
                // May be null.
                Object[] dependencies = getDependenciesForConfiguration(configurationDependencies);
                // Cache dependencies for current configuration and return.
                mResourceDependenciesCache.put(key, dependencies);
                return dependencies;
            }
        }

        return null;
    }

    private synchronized Object[] getDependenciesForConfiguration(Object[][] configurationDependencies) {
        Object[] configurationIndexes = configurationDependencies[0];
        for (int i = 0; i < configurationIndexes.length; i++) {
            CcConfiguration configuration = mConfigurations[(int) configurationIndexes[i]];
            if (CcConfigurationUtils.areCompatible(configuration, mConfiguration)) {
                return configurationDependencies[i + 1];
            }
        }
        return null;
    }

    public synchronized void resolveDependencies(Resources.Theme theme, Resources resources, int id,
                                                 Set<Integer> outResolvedIds) {
        Set<Integer> unresolvedAttrs = CcTempUtils.getIntegerSet();
        getDependencies(resources, id, outResolvedIds, unresolvedAttrs);

        resolveDependencies(theme, resources, unresolvedAttrs, outResolvedIds);

        // Recycle set for reuse.
        CcTempUtils.recycleIntegerSet(unresolvedAttrs);
    }

    public synchronized void resolveDependencies(Resources.Theme theme, Resources resources, Set<Integer> attrs,
                                                 Set<Integer> outResolvedAttrs) {
        if (attrs.size() == 0) {
            // Skip if there are not attributes to process.
            return;
        }

        Map<Integer, Integer> resolvedAttrs = mThemeResolvedAttrs.get(theme);
        if (resolvedAttrs == null) {
            resolvedAttrs = new HashMap<>();
            mThemeResolvedAttrs.put(theme, resolvedAttrs);
        }

        Set<Integer> unresolvedAttrs = CcTempUtils.getIntegerSet();
        for (int attr : attrs) {
            Integer id = resolvedAttrs.get(attr);
            if (id == null) {
                unresolvedAttrs.add(attr);
            } else {
                // Add resolved id.
                outResolvedAttrs.add(id);
                // Also resolve its dependencies.
                resolveDependencies(theme, resources, id, outResolvedAttrs);
            }
        }

        int[] unresolvedAttrsArray = CcTempUtils.toIntArray(unresolvedAttrs);
        TypedArray ta = theme.obtainStyledAttributes(unresolvedAttrsArray);
        try {
            int N = ta.length();
            for (int i = 0; i < N; i++) {
                int index = ta.getIndex(i);
                int id = ta.getResourceId(index, 0);
                if (id != 0) {
                    // Cache resolved id.
                    resolvedAttrs.put(unresolvedAttrsArray[index], id);
                    // Add resolved id.
                    outResolvedAttrs.add(id);
                    // Also resolve its dependencies.
                    resolveDependencies(theme, resources, id, outResolvedAttrs);
                }
            }
        } finally {
            ta.recycle();
        }

        // Recycle set and array for reuse.
        CcTempUtils.recycleIntArray(unresolvedAttrsArray);
        CcTempUtils.recycleIntegerSet(unresolvedAttrs);
    }

    private synchronized Object getKey(Resources resources, int id) {
        if (mIdKey.containsKey(id)) {
            return mIdKey.get(id);
        }

        Object key;
        if (mResourceConfigurationDependencies.containsKey(id)) {
            key = id;
        } else {
            String name = resources.getResourceName(id);
            if (mResourceConfigurationDependencies.containsKey(name)) {
                key = name;
            } else {
                key = null;
            }
        }

        // Cache id-key pair.
        mIdKey.put(id, key);
        return key;
    }

    private synchronized Integer getId(Resources resources, Object key) {
        if (key instanceof Integer) {
            return (Integer) key;
        } else {
            if (mKeyId.containsKey(key)) {
                return mKeyId.get(key);
            }

            String name = (String) key;
            int id = resources.getIdentifier(name, "string", null);

            // Cache key-id pair.
            mKeyId.put(key, id);
            return id;
        }
    }

    /**
     * @return true, if id is of attr type; false, if id is not of attr type; null, if couldn't determine id's type.
     */
    private synchronized Boolean isAttr(Resources resources, int id) {
        if (mIdIsAttr.containsKey(id)) {
            return mIdIsAttr.get(id);
        }

        String type;
        try {
            type = resources.getResourceTypeName(id);
        } catch (Resources.NotFoundException e) {
            type = null;
        }

        Boolean isAttr = type != null ? TYPE_ATTR.equals(type) : null;

        // Cache id-isAttr pair.
        mIdIsAttr.put(id, isAttr);
        return isAttr;
    }
}
