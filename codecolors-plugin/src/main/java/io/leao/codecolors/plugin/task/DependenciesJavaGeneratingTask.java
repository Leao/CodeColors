package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.MergeResources;

import org.gradle.api.Project;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import java.io.File;
import java.util.Set;

import io.leao.codecolors.plugin.res.DependenciesParser;
import io.leao.codecolors.plugin.res.Resource;
import io.leao.codecolors.plugin.source.ColorConfigurationsGenerator;
import io.leao.codecolors.plugin.source.DependenciesGenerator;
import io.leao.codecolors.plugin.util.ResourceUtils;

public class DependenciesJavaGeneratingTask extends JavaGeneratingTask {
    private static final String NAME_BASE = "generateCcDependencies";

    private File mMergeResourcesDir;

    public DependenciesJavaGeneratingTask(Project project, BaseVariant variant) {
        super(project, variant, NAME_BASE);

        // Depends on merged resources.
        MergeResources mergeResourcesTask = variant.getMergeResources();
        addDependsOn(mergeResourcesTask);
        mMergeResourcesDir = mergeResourcesTask.getOutputDir();
        addInputFile(mMergeResourcesDir);
    }

    @Override
    public void generate(IncrementalTaskInputs inputs) {
        File androidSdkResourcesFile =
                ResourceUtils.getResourceAsFile(AndroidSdkDependenciesTask.RESOURCES_OUTPUT_FILE_NAME);
        Resource.Pool resourcesPool = new CcResourcePool(getProject(), getVariant(), androidSdkResourcesFile);

        // Parse and get configuration resource dependencies.
        DependenciesParser dependenciesParser = new DependenciesParser(resourcesPool);
        dependenciesParser.parseDependencies(mMergeResourcesDir);

        DependenciesGenerator dependenciesGenerator = new DependenciesGenerator();
        dependenciesGenerator.generateDependencies(
                resourcesPool.getResources(),
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
        protected boolean getDefaultIsCodeColor(String name, Resource.Type type) {
            return super.getDefaultIsCodeColor(name, type) || mColors.contains(name);
        }
    }
}
