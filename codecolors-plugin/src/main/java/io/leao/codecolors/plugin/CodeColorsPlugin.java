package io.leao.codecolors.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.SourceProvider;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
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
                PreprocessTask task = PreprocessTask.create(project, applicationVariant);
                applicationVariant.registerJavaGeneratingTask(task, task.getOutputDir());
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
            String name = generateTaskName(variant);
            project.getTasks().create(name, PreprocessTask.class, new Action<PreprocessTask>() {
                @Override
                public void execute(PreprocessTask preprocessTask) {
                    preprocessTask.setPackageName(variant.getGenerateBuildConfig().getBuildConfigPackageName());
                    preprocessTask.setApplicationId(variant.getApplicationId());
                    preprocessTask.setOutputDir(generateOutputDir(project, variant.getName()));

                    for (SourceProvider sourceSet : variant.getSourceSets()) {
                        preprocessTask.getInputDirs().addAll(sourceSet.getResDirectories());
                    }
                }
            });
            return (PreprocessTask) project.getTasks().getByName(name);
        }

        private static String generateTaskName(BaseVariant variant) {
            return NAME_BASE + AaptUtil.capitalize(variant.getName());
        }

        private static File generateOutputDir(Project project, String variantName) {
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
                    dependencyHandler.getDependencies(),
                    mPackageName,
                    mApplicationId,
                    mOutputDir);
        }
    }
}
