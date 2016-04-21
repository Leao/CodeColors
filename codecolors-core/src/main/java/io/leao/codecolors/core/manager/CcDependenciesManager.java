package io.leao.codecolors.core.manager;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.core.res.CcConfigurationUtils;
import io.leao.codecolors.core.util.TempUtils;
import io.leao.codecolors.plugin.CcConst;
import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcDependenciesManager {
    private static final String CLASS_NAME_BASE = "%s.%s";
    private static final String TYPE_ATTR = "attr";

    private Map<CcConfiguration, Map<Object, Set<Object>>> mConfigurationDependencies;

    private CcConfiguration mCcConfiguration;
    private Map<Object, Set<Object>> mDependencies;

    // Keys are either the id or the name of the resource.
    private Map<Integer, Object> mIdKey = new HashMap<>();
    private Map<Object, Integer> mKeyId = new HashMap<>();

    private Map<Resources.Theme, Map<Integer, Integer>> mThemeResolvedAttrs = new HashMap<>();

    @SuppressWarnings("unchecked")
    public synchronized void init(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // Init dependencies from generated classes by the CodeColors plugin.
        Class<?> dependenciesClass =
                Class.forName(String.format(CLASS_NAME_BASE, packageName, CcConst.DEPENDENCIES_CLASS_NAME));
        Field dependenciesField = dependenciesClass.getDeclaredField(CcConst.DEPENDENCIES_FIELD_NAME);
        dependenciesField.setAccessible(true);
        mConfigurationDependencies = (Map<CcConfiguration, Map<Object, Set<Object>>>) dependenciesField.get(null);
    }

    public synchronized void onConfigurationChanged(Configuration configuration) {
        CcConfiguration ccConfiguration =
                CcConfigurationUtils.getBestConfiguration(configuration, mConfigurationDependencies.keySet());
        if (!ccConfiguration.equals(mCcConfiguration)) {
            if (mCcConfiguration == null) {
                mCcConfiguration = new CcConfiguration(ccConfiguration);
            } else {
                mCcConfiguration.setTo(ccConfiguration);
            }

            mCcConfiguration = ccConfiguration;
            mDependencies = mConfigurationDependencies.get(ccConfiguration);
        }
    }

    public synchronized boolean hasDependencies(Resources resources, int resourceId) {
        return getKey(resources, resourceId) != null;
    }

    public synchronized void getDependencies(Resources resources, int id, Set<Integer> outResolvedIds,
                                             Set<Integer> outUnresolvedAttrs) {
        Object key = getKey(resources, id);
        if (key != null) {
            Set<Object> dependencies = mDependencies.get(key);
            for (Object dependency : dependencies) {
                int dependencyId = getId(resources, dependency);
                if (TYPE_ATTR.equals(resources.getResourceTypeName(dependencyId))) {
                    outUnresolvedAttrs.add(dependencyId);
                } else {
                    outResolvedIds.add(dependencyId);
                }
            }
        }
    }

    public synchronized void resolveDependencies(Resources.Theme theme, Resources resources, int id,
                                                 Set<Integer> outResolvedIds) {
        Set<Integer> unresolvedAttrs = TempUtils.getIntegerSet();
        getDependencies(resources, id, outResolvedIds, unresolvedAttrs);

        resolveDependencies(theme, resources, unresolvedAttrs, outResolvedIds);

        // Recycle set for reuse.
        TempUtils.recycleIntegerSet(unresolvedAttrs);
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

        Set<Integer> unresolvedAttrs = TempUtils.getIntegerSet();
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

        int[] unresolvedAttrsArray = TempUtils.toIntArray(unresolvedAttrs);
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
        TempUtils.recycleIntArray(unresolvedAttrsArray);
        TempUtils.recycleIntegerSet(unresolvedAttrs);
    }

    private synchronized Object getKey(Resources resources, int id) {
        if (mIdKey.containsKey(id)) {
            return mIdKey.get(id);
        }

        Object key;
        if (mDependencies.containsKey(id)) {
            key = id;
        } else {
            String name = resources.getResourceName(id);
            if (mDependencies.containsKey(name)) {
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
}
