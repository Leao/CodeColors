package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.MergeResources;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.plugin.aapt.AaptUtil;
import io.leao.codecolors.plugin.res.Resource;
import io.leao.codecolors.plugin.res.ResourcesDependenciesParser;
import io.leao.codecolors.plugin.source.CodeColorsDependenciesGenerator;

public class CodeColorsDependenciesTask extends DefaultTask {
    private static final String NAME_BASE = "generateCodeColorsDependencies%s";
    private static final String OUTPUT_DIR_BASE = "%s\\generated\\source\\codecolors\\%s";

    private Set<File> mInputFiles = new HashSet<>();
    private File mOutputDir;

    private File mSdkResourcesFile;
    private File mMergeResourcesDir;

    private BaseVariant mVariant;

    public static CodeColorsDependenciesTask create(Project project, BaseVariant variant,
                                                    SdkDependenciesTask sdkDependenciesTask) {
        String name = String.format(NAME_BASE, AaptUtil.capitalize(variant.getName()));
        CodeColorsDependenciesTask task = project.getTasks().create(name, CodeColorsDependenciesTask.class);
        task.initialize(project, variant, sdkDependenciesTask);
        return task;
    }

    protected void initialize(Project project, BaseVariant variant, SdkDependenciesTask sdkDependenciesTask) {
        mVariant = variant;

        // Depends on sdk resources dependencies.
        mSdkResourcesFile = sdkDependenciesTask.getOutputFile();
        mInputFiles.add(mSdkResourcesFile);
        addDependsOn(sdkDependenciesTask);

        // Depends on resources merge.
        MergeResources mergeResourcesTask = variant.getMergeResources();
        mMergeResourcesDir = mergeResourcesTask.getOutputDir();
        mInputFiles.add(mMergeResourcesDir);
        addDependsOn(mergeResourcesTask);

        // Output directory, the source package directory.
        String outputDirPath = String.format(OUTPUT_DIR_BASE, project.getBuildDir(), variant.getName());
        mOutputDir = project.file(new File(outputDirPath));

        // Adds task to variant as a Java source code generator.
        mVariant.registerJavaGeneratingTask(this, mOutputDir);
    }

    @InputFiles
    public Set<File> getInputFiles() {
        return mInputFiles;
    }

    @OutputDirectory
    public File getOutputDir() {
        return mOutputDir;
    }

    @TaskAction
    public void generateSource() {
        Resource.Pool resourcesPool = new Resource.Pool(mSdkResourcesFile);

        // Parse and get configuration resource dependencies.
        ResourcesDependenciesParser dependenciesParser = new ResourcesDependenciesParser(resourcesPool);
        dependenciesParser.parseDependencies(mMergeResourcesDir);

        CodeColorsDependenciesGenerator.generateDependencies(
                dependenciesParser.getResources(),
                mVariant.getGenerateBuildConfig().getBuildConfigPackageName(),
                mVariant.getApplicationId(),
                mOutputDir);
    }

    public void addDependsOn(Object dependency) {
        Set<Object> dependencies = getDependsOn();
        if (dependencies == null) {
            dependencies = new HashSet<>();
            setDependsOn(dependencies);
        }
        dependencies.add(dependency);
    }
}