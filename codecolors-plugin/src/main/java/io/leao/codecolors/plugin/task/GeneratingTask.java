package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.plugin.aapt.AaptUtil;

public abstract class GeneratingTask {

    private Project mProject;
    private BaseVariant mVariant;
    private AbsGradleTask mGradleTask;

    public GeneratingTask(Project project, BaseVariant variant, String baseName, String outputDirPath,
                          boolean incremental) {
        mProject = project;
        mVariant = variant;

        if (incremental) {
            mGradleTask = project.getTasks().create(generateName(variant, baseName), GradleIncrementalTask.class);
        } else {
            mGradleTask = project.getTasks().create(generateName(variant, baseName), GradleTask.class);
        }

        // Output directory.
        mGradleTask.setOutputDir(project.file(outputDirPath));

        // Set GeneratingTask to be called by task action.
        mGradleTask.setGeneratingTask(this);
    }

    public abstract void generate(IncrementalTaskInputs inputs);

    public Project getProject() {
        return mProject;
    }

    public BaseVariant getVariant() {
        return mVariant;
    }

    public void addInputFile(File file) {
        mGradleTask.addInputFile(file);
    }

    public Set<File> getInputFiles() {
        return mGradleTask.getInputFiles();
    }

    public File getOutputDir() {
        return mGradleTask.getOutputDir();
    }

    protected Task getGradleTask() {
        return mGradleTask;
    }

    public abstract static class AbsGradleTask extends DefaultTask {
        private Set<File> mInputFiles = new HashSet<>();
        private File mOutputDir;
        protected GeneratingTask mGeneratingTask;

        @InputFiles
        public Set<File> getInputFiles() {
            return mInputFiles;
        }

        public void addInputFile(File file) {
            mInputFiles.add(file);
        }

        public void setOutputDir(File outputDir) {
            mOutputDir = outputDir;
        }

        @OutputDirectory
        public File getOutputDir() {
            return mOutputDir;
        }

        public void setGeneratingTask(GeneratingTask generatingTask) {
            mGeneratingTask = generatingTask;
        }
    }

    public static class GradleTask extends AbsGradleTask {
        @TaskAction
        public void generate() {
            mGeneratingTask.generate(null);
        }
    }

    public static class GradleIncrementalTask extends AbsGradleTask {
        @TaskAction
        public void generate(IncrementalTaskInputs inputs) {
            mGeneratingTask.generate(inputs);
        }
    }

    protected static String generateName(BaseVariant variant, String baseName) {
        return baseName + AaptUtil.capitalize(variant.getName());
    }
}
