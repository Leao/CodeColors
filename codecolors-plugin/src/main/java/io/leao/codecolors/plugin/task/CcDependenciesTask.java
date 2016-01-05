package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;

import java.io.File;

import io.leao.codecolors.plugin.res.Resource;
import io.leao.codecolors.plugin.res.DependenciesParser;
import io.leao.codecolors.plugin.source.CcDependenciesGenerator;

public class CcDependenciesTask extends JavaGeneratingTask {
    private static final String NAME_BASE = "generateCcDependencies";

    private File mSdkResourcesFile;

    public CcDependenciesTask(Project project, BaseVariant variant, SdkDependenciesTask sdkDependenciesTask) {
        super(project, variant, NAME_BASE);

        // Depends on sdk resources dependencies.
        addDependsOn(sdkDependenciesTask);
        mSdkResourcesFile = sdkDependenciesTask.getOutputFile();
        addInputFile(mSdkResourcesFile);
    }

    @Override
    public void generateJava() {
        Resource.Pool resourcesPool = new Resource.Pool(mSdkResourcesFile);

        // Parse and get configuration resource dependencies.
        DependenciesParser dependenciesParser = new DependenciesParser(resourcesPool);
        dependenciesParser.parseDependencies(getMergeResourcesDir());

        CcDependenciesGenerator.generateDependencies(
                dependenciesParser.getResources(),
                getVariant().getGenerateBuildConfig().getBuildConfigPackageName(),
                getVariant().getApplicationId(),
                getOutputDir());
    }
}
