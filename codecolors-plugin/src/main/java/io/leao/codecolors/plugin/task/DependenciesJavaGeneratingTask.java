package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.MergeResources;

import org.gradle.api.Project;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import java.io.File;
import java.util.Set;

import io.leao.codecolors.plugin.res.DependenciesParser;
import io.leao.codecolors.plugin.res.Resource;
import io.leao.codecolors.plugin.source.DependenciesGenerator;
import io.leao.codecolors.plugin.source.ColorConfigurationsGenerator;

public class DependenciesJavaGeneratingTask extends JavaGeneratingTask {
    private static final String NAME_BASE = "generateCcDependencies";

    private File mMergeResourcesDir;
    private File mSdkResourcesFile;

    public DependenciesJavaGeneratingTask(Project project, BaseVariant variant,
                                          SdkDependenciesTask sdkDependenciesTask) {
        super(project, variant, NAME_BASE);

        // Depends on merged resources.
        MergeResources mergeResourcesTask = variant.getMergeResources();
        addDependsOn(mergeResourcesTask);
        mMergeResourcesDir = mergeResourcesTask.getOutputDir();
        addInputFile(mMergeResourcesDir);

        // Depends on sdk resources dependencies.
        addDependsOn(sdkDependenciesTask);
        mSdkResourcesFile = sdkDependenciesTask.getOutputFile();
        addInputFile(mSdkResourcesFile);
    }

    @Override
    public void generate(IncrementalTaskInputs inputs) {
        Resource.Pool resourcesPool = new CcResourcePool(getProject(), getVariant(), mSdkResourcesFile);

        // Parse and get configuration resource dependencies.
        DependenciesParser dependenciesParser = new DependenciesParser(resourcesPool);
        dependenciesParser.parseDependencies(mMergeResourcesDir);

        DependenciesGenerator.generateDependencies(
                dependenciesParser.getResources(),
                getVariant().getGenerateBuildConfig().getBuildConfigPackageName(),
                getVariant().getApplicationId(),
                getOutputDir());
    }

    private static class CcResourcePool extends Resource.Pool {
        private Set<String> mColors;

        public CcResourcePool(Project project, BaseVariant variant, File input) {
            super(input);
            mColors = ColorConfigurationsGenerator.getColorConfigurations(project, variant).keySet();
        }

        @Override
        protected boolean isCodeColor(String name, Resource.Type type) {
            return super.isCodeColor(name, type) || mColors.contains(name);
        }
    }
}
