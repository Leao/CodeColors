package io.leao.codecolors.plugin.res;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.leao.codecolors.plugin.file.FileUtils;

public class Resource implements Serializable {
    private static final long serialVersionUID = -397054342376654475L;

    private String mName;
    private Type mType;
    private boolean mIsPublic;
    // True if of attribute type, or if included in the code colors list.
    private boolean mIsCodeColor;

    private Map<CcConfiguration, Boolean> mConfigurationHasCodeColors;
    private Map<CcConfiguration, Set<Resource>> mConfigurationDependencies;

    // Dependents do not need to be persisted, since they are only important when creating the dependency graph, and,
    // if persisted, they persist the circular dependency between resources, that creates a hashCode() issue on
    // deserialization.
    private transient Map<CcConfiguration, Set<Resource>> mConfigurationDependents;

    private Resource(String name, Type type, boolean isPublic, boolean isCodeColor) {
        mName = name;
        mType = type;
        mIsPublic = isPublic;
        mIsCodeColor = isCodeColor;
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

    public boolean hasCodeColors() {
        if (mIsCodeColor) {
            return true;
        }

        if (mConfigurationHasCodeColors != null) {
            for (Boolean hasCodeColors : mConfigurationHasCodeColors.values()) {
                if (hasCodeColors) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasCodeColors(CcConfiguration configuration) {
        return mIsCodeColor ||
                (mConfigurationHasCodeColors != null && mConfigurationHasCodeColors.getOrDefault(configuration, false));

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

        if (mConfigurationDependencies == null) {
            mConfigurationDependencies = new TreeMap<>();
        }
        Set<Resource> dependencies = mConfigurationDependencies.get(configuration);
        if (dependencies == null) {
            dependencies = new HashSet<>();
            mConfigurationDependencies.put(configuration, dependencies);
        }

        if (!dependencies.contains(dependency)) {
            // Add code color info.
            if (mConfigurationHasCodeColors == null) {
                mConfigurationHasCodeColors = new HashMap<>();
            }
            Boolean hasCodeColors = mConfigurationHasCodeColors.get(configuration);
            // Only overwrite hasCodeColors value if it is not true.
            if (hasCodeColors == null || !hasCodeColors) {
                mConfigurationHasCodeColors.put(configuration, dependency.hasCodeColors());
            }

            // Add dependency-dependent relation.
            dependencies.add(dependency);
            dependency.addDependent(configuration, this);

            // Also add all dependency's dependencies.
            if (mConfigurationDependencies != null) {
                Set<Resource> dependencyDependencies = mConfigurationDependencies.get(configuration);
                if (dependencyDependencies != null) {
                    for (Resource dependencyDependency : dependencyDependencies) {
                        dependencies.add(dependencyDependency);
                        dependencyDependency.addDependent(configuration, this);
                    }
                }
            }

            // Propagate dependency to dependents.
            if (mConfigurationDependents != null) {
                Set<Resource> dependents = mConfigurationDependents.get(configuration);
                if (dependents != null) {
                    for (Resource dependent : dependents) {
                        dependent.addDependency(configuration, dependency);
                    }
                }
            }
        }
    }

    public void pruneDependencies() {
        // Code color dependencies must be preserved.
        if (mIsCodeColor) {
            return;
        }

        if (mConfigurationHasCodeColors != null) {
            for (CcConfiguration configuration : mConfigurationHasCodeColors.keySet()) {
                boolean hasCodeColors = mConfigurationHasCodeColors.get(configuration);
                // If a configuration has code colors, we remove only the dependencies that doesn't have code colors.
                // If a configuration doesn't have code colors, we remove the configuration altogether for the list
                // of dependencies.
                // In both cases, we remove this resource as a dependent.
                if (hasCodeColors) {
                    Iterator<Resource> dependenciesIterator = mConfigurationDependencies.get(configuration).iterator();
                    while (dependenciesIterator.hasNext()) {
                        Resource dependency = dependenciesIterator.next();
                        if (!dependency.hasCodeColors(configuration)) {
                            dependenciesIterator.remove();
                            dependency.removeDependent(configuration, this);
                        }
                    }
                } else {
                    for (Resource dependency : mConfigurationDependencies.get(configuration)) {
                        dependency.removeDependent(configuration, this);
                    }
                    mConfigurationDependencies.remove(configuration);
                }
            }
        }
    }

    protected void addDependent(CcConfiguration configuration, Resource dependent) {
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

    protected void removeDependent(CcConfiguration configuration, Resource dependent) {
        if (mConfigurationDependents != null) {
            Set<Resource> dependents = mConfigurationDependents.get(configuration);
            if (dependents != null) {
                dependents.remove(dependent);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource) o;

        if (!mName.equals(resource.mName)) return false;
        return mType == resource.mType;
    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mType.hashCode();
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
            Resource key = new Resource(name, type, isPublic(name, type), isCodeColor(name, type));
            Resource value = mResources.get(key);
            if (value == null) {
                value = key;
                mResources.put(key, value);
            }
            return value;
        }

        protected boolean isPublic(String name, Resource.Type type) {
            return true;
        }

        protected boolean isCodeColor(String name, Resource.Type type) {
            return type == Type.ATTR || type == Type.ANDROID_ATTR;
        }

        public void writeTo(File output) {
            FileUtils.writeTo(mResources, output);
        }
    }
}
