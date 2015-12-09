package io.leao.codecolors.plugin.res;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Resource {
    private String mName;
    private Type mType;

    private Map<CodeColorsConfiguration, Set<Resource>> mConfigurationDependencies;
    private Map<CodeColorsConfiguration, Set<Resource>> mConfigurationDependents;

    private Resource(String name, Type type) {
        mName = name;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public void addDependency(CodeColorsConfiguration configuration, Resource dependency) {
        if (dependency.equals(this)) {
            return; // Circular dependency.
        }

        // Establish dependency-dependent relation.
        Set<Resource> dependencies = getDependencies(configuration);
        dependencies.add(dependency);
        dependency.addDependent(configuration, this);

        // Propagate dependency.
        Set<Resource> dependents = getDependents(configuration);
        for (Resource dependent : dependents) {
            dependent.addDependency(configuration, dependency);
        }
    }

    public boolean hasDependencies() {
        return mConfigurationDependencies != null;
    }

    public Map<CodeColorsConfiguration, Set<Resource>> getConfigurationDependencies() {
        return mConfigurationDependencies;
    }

    private Set<Resource> getDependencies(CodeColorsConfiguration configuration) {
        if (mConfigurationDependencies == null) {
            mConfigurationDependencies = new TreeMap<>();
        }
        Set<Resource> dependencies = mConfigurationDependencies.get(configuration);
        if (dependencies == null) {
            dependencies = new HashSet<>();
            mConfigurationDependencies.put(configuration, dependencies);
        }
        return dependencies;
    }

    private void addDependent(CodeColorsConfiguration configuration, Resource dependent) {
        Set<Resource> dependents = getDependents(configuration);
        dependents.add(dependent);
    }

    private Set<Resource> getDependents(CodeColorsConfiguration configuration) {
        if (mConfigurationDependents == null) {
            mConfigurationDependents = new TreeMap<>();
        }
        Set<Resource> dependents = mConfigurationDependents.get(configuration);
        if (dependents == null) {
            dependents = new HashSet<>();
            mConfigurationDependents.put(configuration, dependents);
        }
        return dependents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource) o;

        if (mName != null ? !mName.equals(resource.mName) : resource.mName != null) return false;
        return mType == resource.mType;
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + (mType != null ? mType.hashCode() : 0);
        return result;
    }

    public enum Type {
        DRAWABLE, COLOR, ATTR, ANDROID_ATTR
    }

    public static class Pool {
        private final Map<Resource, Resource> mResources = new HashMap<>();

        public Set<Resource> getResources() {
            return mResources.keySet();
        }

        public Resource getOrCreateResource(String name, Resource.Type type) {
            Resource key = new Resource(name, type);
            Resource value = mResources.get(key);
            if (value == null) {
                value = key;
                mResources.put(key, value);
            }
            return value;
        }
    }
}
