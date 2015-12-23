package io.leao.codecolors.res;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.plugin.res.CodeColorsConfiguration;

public class CodeColorsDependenciesHandler {
    private static final String TYPE_ATTR = "attr";

    private static final Map<String, Map<CodeColorsConfiguration, Map<Object, Set<Object>>>> sDependencies =
            new HashMap<>();
    // Keys are either the id or the name of the resource.
    private static final Map<Integer, Object> sKeys = new HashMap<>();
    private static final Map<Object, Integer> sResolvedKeys = new HashMap<>();

    private Context mContext;
    private Resources mResources;
    private Map<Object, Set<Object>> mDependencies;

    private Map<Object, Integer> mResolvedAttrs = new HashMap<>();
    private Map<Integer, Set<Integer>> mResolvedDependencies = new HashMap<>();

    private int[] mTempArray = new int[1];

    public CodeColorsDependenciesHandler(Context context) {
        mContext = context;
        mResources = context.getResources();
        mDependencies = getDependencies(context);
    }

    public Set<Integer> resolveDependencies(int resourceId) {
        if (mResolvedDependencies.containsKey(resourceId)) {
            return mResolvedDependencies.get(resourceId);
        }

        Set<Integer> resolvedDependencies;
        Object key = getKey(resourceId);
        if (key != null) {
            resolvedDependencies = new HashSet<>();
            resolvedDependencies.add(resourceId);
            Set<Object> dependencies = mDependencies.get(key);
            for (Object dependency : dependencies) {
                resolvedDependencies.add(resolveAttr(resolveKey(dependency)));
            }
        } else {
            resolvedDependencies = null;
        }

        mResolvedDependencies.put(resourceId, resolvedDependencies);
        return resolvedDependencies;
    }

    private Object getKey(int resourceId) {
        if (sKeys.containsKey(resourceId)) {
            return sKeys.get(resourceId);
        }

        Object key;
        if (mDependencies.containsKey(resourceId)) {
            key = resourceId;
        } else {
            String resourceName = mResources.getResourceName(resourceId);
            if (mDependencies.containsKey(resourceName)) {
                key = resourceName;
            } else {
                key = null;
            }
        }

        sKeys.put(resourceId, key);
        return key;
    }

    private Integer resolveKey(Object resourceKey) {
        if (resourceKey instanceof Integer) {
            return (Integer) resourceKey;
        } else {
            if (sResolvedKeys.containsKey(resourceKey)) {
                return sResolvedKeys.get(resourceKey);
            }

            String resourceName = (String) resourceKey;
            int resourceId = mResources.getIdentifier(resourceName, "string", null);

            sResolvedKeys.put(resourceKey, resourceId);
            return resourceId;
        }
    }

    private Integer resolveAttr(Integer resourceId) {
        if (mResolvedAttrs.containsKey(resourceId)) {
            return mResolvedAttrs.get(resourceId);
        }

        int resolvedId = 0;
        if (TYPE_ATTR.equals(mResources.getResourceTypeName(resourceId))) {
            mTempArray[0] = resourceId;
            TypedArray ta = mContext.obtainStyledAttributes(mTempArray);
            try {
                resolvedId = ta.getResourceId(0, resolvedId);
            } finally {
                ta.recycle();
            }
        }

        mResolvedAttrs.put(resourceId, resolvedId);
        return resolvedId;
    }

    @SuppressWarnings("unchecked")
    public static void addPackageDependencies(String packageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> codeColorResourcesClass = Class.forName(packageName + ".CodeColorsDependencies");
        Field dependenciesField = codeColorResourcesClass.getDeclaredField("sDependencies");
        dependenciesField.setAccessible(true);
        sDependencies.put(
                packageName,
                (Map<CodeColorsConfiguration, Map<Object, Set<Object>>>) dependenciesField.get(null));
    }

    public static Map<Object, Set<Object>> getDependencies(Context context) {
        Map<CodeColorsConfiguration, Map<Object, Set<Object>>> configurationDependencies =
                sDependencies.get(context.getPackageName());
        Configuration contextConfiguration = context.getResources().getConfiguration();
        for (CodeColorsConfiguration configuration : configurationDependencies.keySet()) {
            if (CodeColorsConfigurationUtils.areCompatible(configuration, contextConfiguration)) {
                return configurationDependencies.get(configuration);
            }
        }
        return configurationDependencies.values().iterator().next();
    }
}
