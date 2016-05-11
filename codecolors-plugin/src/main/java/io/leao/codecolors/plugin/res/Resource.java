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
    private Visibility mVisibility;
    private boolean mIsCodeColor; // True if of attribute type, or if included in the code colors list.

    private Map<Integer, Boolean> mSdkVersionIsPublic;
    private Map<CcConfiguration, Set<Resource>> mConfigurationDependencies;

    private boolean mIsPruned;

    // Dependents do not need to be persisted, since they are only important when creating the dependency graph, and,
    // if persisted, they persist the circular dependency between resources, that creates a hashCode() issue on
    // deserialization.
    private transient Map<CcConfiguration, Set<Resource>> mConfigurationDependents;

    private Resource(String name, Type type, Visibility visibility, boolean isCodeColor) {
        mName = name;
        mType = type;
        mVisibility = visibility;
        mIsCodeColor = isCodeColor;
    }

    @Override
    public String toString() {
        return mName;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public Visibility getVisibility() {
        return mVisibility;
    }

    public void setVisibility(Visibility visibility) {
        mVisibility = visibility;
    }

    public boolean isPublic() {
        return mVisibility == Visibility.PUBLIC;
    }

    public boolean isPublic(int sdkVersion) {
        if (mSdkVersionIsPublic != null) {
            return mSdkVersionIsPublic.getOrDefault(sdkVersion, isPublic());
        } else {
            return isPublic();
        }
    }

    public void setIsPublic(int sdkVersion, boolean isPublic) {
        if (mSdkVersionIsPublic == null) {
            mSdkVersionIsPublic = new HashMap<>();
        }
        mSdkVersionIsPublic.put(sdkVersion, isPublic);
    }

    public boolean isCodeColor() {
        return mIsCodeColor;
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

        // If we don't have the dependency.
        if (dependencies == null || !dependencies.contains(dependency)) {
            addDependencyOrDependent(dependencies, configuration, dependency);

            // Also add all dependency's dependencies.
            Map<CcConfiguration, Set<Resource>> dependencyConfigurationDependencies =
                    dependency.getConfigurationDependencies();
            if (dependencyConfigurationDependencies != null) {
                Set<Resource> dependencyDependencies = dependencyConfigurationDependencies.get(configuration);
                if (dependencyDependencies != null) {
                    for (Resource dependencyDependency : dependencyDependencies) {
                        addDependencyOrDependent(dependencies, configuration, dependencyDependency);
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

    private void addDependencyOrDependent(Set<Resource> dependencies, CcConfiguration configuration,
                                          Resource dependency) {
        // If the dependency is a code-color, add it to the list of dependencies.
        // Otherwise, add this resource as dependent on the dependency resource,
        // to allow the propagation of other sub-dependencies.
        if (dependency.isCodeColor()) {
            if (dependencies == null) {
                dependencies = new HashSet<>();
                mConfigurationDependencies.put(configuration, dependencies);
            }
            dependencies.add(dependency);
        } else {
            dependency.addDependent(configuration, this);
        }
    }

    public void pruneDependencies() {
        // Code color dependencies must be preserved.
        if (mIsCodeColor || mIsPruned) {
            return;
        }

        if (mConfigurationDependencies != null) {
            // Tries to remove duplicate dependencies which only difference is the SDK version.
            Iterator<CcConfiguration> configurationsIterator = mConfigurationDependencies.keySet().iterator();
            Set<CcConfiguration> removableConfigurations = new HashSet<>();

            CcConfiguration previousConfiguration = null;
            while (configurationsIterator.hasNext()) {
                if (previousConfiguration == null) {
                    previousConfiguration = configurationsIterator.next();
                } else {
                    CcConfiguration configuration = configurationsIterator.next();
                    if (matchConfigurationDependencies(previousConfiguration, configuration)) {
                        // If the configuration and dependencies match, and the only difference if the SDK version,
                        // keep the configurationDependencies with the lower SDK version and remove the other.
                        removableConfigurations.add(previousConfiguration);
                    }
                    previousConfiguration = configuration;
                }
            }

            // Remove unnecessary configurations.
            for (CcConfiguration configuration : removableConfigurations) {
                mConfigurationDependencies.remove(configuration);
            }
        }

        // Prune only once.
        mIsPruned = true;
    }

    private boolean matchConfigurationDependencies(CcConfiguration thisConfiguration,
                                                   CcConfiguration thatConfiguration) {
        if (thisConfiguration.compareTo(thatConfiguration, true) == 0) {
            Set<Resource> thisDependencies = mConfigurationDependencies.get(thisConfiguration);
            Set<Resource> thatDependencies = mConfigurationDependencies.get(thatConfiguration);
            if (thisDependencies.size() == thatDependencies.size()) {
                // Check if the list of dependencies are equal.
                for (Resource dependency : thisDependencies) {
                    if (!thatDependencies.contains(dependency) ||
                            (dependency.isPublic(thisConfiguration.sdkVersion) !=
                                    dependency.isPublic(thatConfiguration.sdkVersion))) {
                        // The dependencies are not equal if they do not have the same resources,
                        // or if the visibility of the resources is not the same on both SDK versions.
                        return false;
                    }
                }
            }
        }
        // Success, dependencies are equal.
        return true;
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

    /**
     * {@link Visibility} atomic types are {@link #PUBLIC} and {@link #PRIVATE}.
     * <p>
     * {@link #MIXED} type is composed of {@link #PUBLIC} and {@link #PRIVATE}.
     * <p>
     * Use {@link #getComponents()} to access the basic types.
     */
    public enum Visibility {
        PUBLIC,
        PRIVATE,
        MIXED(PUBLIC, PRIVATE);

        private Visibility[] mComponents;

        Visibility() {
            mComponents = new Visibility[]{this};
        }

        Visibility(Visibility... components) {
            mComponents = components;
        }

        public Visibility[] getComponents() {
            return mComponents;
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
            Resource key = new Resource(name, type, getDefaultVisibility(), getDefaultIsCodeColor(name, type));
            Resource value = mResources.get(key);
            if (value == null) {
                value = key;
                mResources.put(key, value);
            }
            return value;
        }

        protected Visibility getDefaultVisibility() {
            return Visibility.PUBLIC;
        }

        protected boolean getDefaultIsCodeColor(String name, Resource.Type type) {
            return type == Type.ATTR || type == Type.ANDROID_ATTR;
        }

        public void writeTo(File output) {
            FileUtils.writeTo(mResources, output);
        }
    }
}
