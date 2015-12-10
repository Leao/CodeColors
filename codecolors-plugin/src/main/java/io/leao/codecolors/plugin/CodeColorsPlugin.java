package io.leao.codecolors.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.MergeResources;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.plugin.aapt.AaptUtil;
import io.leao.codecolors.plugin.res.ResourceDependencyHandler;
import io.leao.codecolors.plugin.source.SourceGeneratorHandler;

/**
 * Plugin to preprocess resources and generate Java sources.
 */
public class CodeColorsPlugin implements Plugin<Project> {

    public void apply(final Project project) {
        Object androidExtension = project.getProperties().get("android");
        if (androidExtension instanceof AppExtension) {
            ((AppExtension) androidExtension).getApplicationVariants().all(createPreprocessTaskCreator(project));
        } else if (androidExtension instanceof LibraryExtension) {
            ((LibraryExtension) androidExtension).getLibraryVariants().all(createPreprocessTaskCreator(project));
        }
    }

    private static Action<BaseVariant> createPreprocessTaskCreator(final Project project) {
        return new Action<BaseVariant>() {
            @Override
            public void execute(final BaseVariant applicationVariant) {
                PreprocessTask.create(project, applicationVariant);
            }
        };
    }

    public static class PreprocessTask extends DefaultTask {
        private static final String NAME_BASE = "generateCodeColorsSources";
        private static final String OUTPUT_DIR_BASE = addFileSeparators("generated", "source", "codecolors");

        private Set<File> mInputDirs = new HashSet<>();
        private File mOutputDir;

        private String mPackageName; // Same package name as used by R.
        private String mApplicationId;

        public static PreprocessTask create(final Project project, final BaseVariant variant) {
            return project
                    .getTasks()
                    .create(createTaskName(variant), PreprocessTask.class, new Action<PreprocessTask>() {
                        @Override
                        public void execute(PreprocessTask preprocessTask) {
                            // Package name and application id.
                            preprocessTask.setPackageName(variant.getGenerateBuildConfig().getBuildConfigPackageName());
                            preprocessTask.setApplicationId(variant.getApplicationId());
                            // Merge resource (for input dir and task dependency).
                            MergeResources mergeResourcesTask = variant.getMergeResources();
                            preprocessTask.addInputDir(mergeResourcesTask.getOutputDir());
                            // Output dir.
                            File outputDir = createOutputDir(project, variant.getName());
                            preprocessTask.setOutputDir(outputDir);

                            // Adds task to variant as a Java source code generator.
                            variant.registerJavaGeneratingTask(preprocessTask, outputDir);

                            // Make sure the resources were already merged, before executing the task.
                            Set<Object> newDependencies = new HashSet<>();
                            Collection<?> dependencies = preprocessTask.getDependsOn();
                            if (dependencies != null) {
                                for (Object dependency : dependencies) {
                                    newDependencies.add(dependency);
                                }
                            }
                            newDependencies.add(mergeResourcesTask);
                            preprocessTask.setDependsOn(newDependencies);
                        }
                    });
        }

        private static String createTaskName(BaseVariant variant) {
            return NAME_BASE + AaptUtil.capitalize(variant.getName());
        }

        private static File createOutputDir(Project project, String variantName) {
            return project.file(new File(project.getBuildDir() + OUTPUT_DIR_BASE + addFileSeparators(variantName)));
        }

        private static String addFileSeparators(String... folders) {
            String path = "";
            for (String folder : folders) {
                path += File.separator + folder;
            }
            return path;
        }

        private void setPackageName(String packageName) {
            mPackageName = packageName;
        }

        private void setApplicationId(String applicationId) {
            mApplicationId = applicationId;
        }

        private void addInputDir(File inputDir) {
            mInputDirs.add(inputDir);
        }

        @InputFiles
        private Set<File> getInputDirs() {
            return mInputDirs;
        }

        private void setOutputDir(File outputDir) {
            mOutputDir = outputDir;
        }

        @OutputDirectory
        public File getOutputDir() {
            return mOutputDir;
        }

        @TaskAction
        public void generateSource() {
            // Process and get configuration resource dependencies.
            ResourceDependencyHandler dependencyHandler = new ResourceDependencyHandler();
            for (File dir : mInputDirs) {
                dependencyHandler.processDependencies(dir);
            }

            SourceGeneratorHandler.generateSource(
                    dependencyHandler.getConfigurations(),
                    dependencyHandler.getResources(),
                    mPackageName,
                    mApplicationId,
                    mOutputDir);
        }
    }
}
