package io.leao.codecolors.plugin.res;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.leao.codecolors.plugin.file.FileUtils;

public class Resource implements Serializable {
    private static final long serialVersionUID = -397054342376654475L;

    private String mName;
    private Type mType;
    private boolean mIsPublic;

    private Map<CcConfiguration, Set<Resource>> mConfigurationDependencies;
    private Map<CcConfiguration, Set<Resource>> mConfigurationDependents;

    private Resource(String name, Type type, boolean isPublic) {
        mName = name;
        mType = type;
        mIsPublic = isPublic;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public void setIsPublic(boolean isPublic) {
        mIsPublic = isPublic;
    }

    public boolean isPublic() {
        return mIsPublic;
    }

    public Map<CcConfiguration, Set<Resource>> getConfigurationDependencies() {
        return mConfigurationDependencies;
    }

    public boolean hasDependencies() {
        if (mConfigurationDependencies != null) {
            for (Set<Resource> dependencies : mConfigurationDependencies.values()) {
                if (dependencies.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addDependency(CcConfiguration configuration, Resource dependency) {
        if (dependency.equals(this)) {
            return; // Circular dependency.
        }

        // Add dependency-dependent relation.
        if (mConfigurationDependencies == null) {
            mConfigurationDependencies = new TreeMap<>();
        }
        Set<Resource> dependencies = mConfigurationDependencies.get(configuration);
        if (dependencies == null) {
            dependencies = new HashSet<>();
            mConfigurationDependencies.put(configuration, dependencies);
        }
        dependencies.add(dependency);
        dependency.addDependent(configuration, this);

        // Propagate dependency.
        if (mConfigurationDependents != null) {
            Set<Resource> dependents = mConfigurationDependents.get(configuration);
            if (dependents != null) {
                for (Resource dependent : dependents) {
                    dependent.addDependency(configuration, dependency);
                }
            }
        }
    }

    public boolean hasDependents() {
        if (mConfigurationDependents != null) {
            for (Set<Resource> dependents : mConfigurationDependents.values()) {
                if (dependents.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addDependent(CcConfiguration configuration, Resource dependent) {
        if (mConfigurationDependents == null) {
            mConfigurationDependents = new TreeMap<>();
        }
        Set<Resource> dependents = mConfigurationDependents.get(configuration);
        if (dependents == null) {
            dependents = new HashSet<>();
            mConfigurationDependents.put(configuration, dependents);
        }
        dependents.add(dependent);
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
        DRAWABLE("drawable"),
        ANDROID_DRAWABLE("drawable"),
        COLOR("color"),
        ANDROID_COLOR("color"),
        ATTR("attr"),
        ANDROID_ATTR("attr");

        private String mName;

        Type(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }

    public static class Pool {
        private HashMap<Resource, Resource> mResources;

        public Pool() {
            mResources = new HashMap<>();
        }

        @SuppressWarnings("unchecked")
        public Pool(File input) {
            mResources = (HashMap<Resource, Resource>) FileUtils.readFrom(input);
            if (mResources == null) {
                mResources = new HashMap<>();
            }
        }

        public Set<Resource> getResources() {
            return mResources.keySet();
        }

        public Resource getOrCreateResource(String name, Resource.Type type) {
            Resource key = new Resource(name, type, isPublic(name, type));
            Resource value = mResources.get(key);
            if (value == null) {
                value = key;
                mResources.put(key, value);
            }
            return value;
        }

        public boolean isPublic(String name, Resource.Type type) {
            return true;
        }

        public void writeTo(File output) {
            FileUtils.writeTo(mResources, output);
        }
    }
}
