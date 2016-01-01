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

public abstract class JavaGeneratingTask {
    private static final String OUTPUT_DIR_BASE = "%s\\generated\\source\\codecolors\\%s";

    private Project mProject;
    private BaseVariant mVariant;

    private RunnableTask mRunnableTask;
    private File mMergeResourcesDir;

    public static void register(JavaGeneratingTask task) {
        // Adds task to variant as a Java source code generator.
        task.getVariant().registerJavaGeneratingTask(task.getRunnableTask(), task.getOutputDir());
    }

    public JavaGeneratingTask(Project project, BaseVariant variant, String baseName) {
        mProject = project;
        mVariant = variant;

        String name = baseName + AaptUtil.capitalize(variant.getName());
        mRunnableTask = project.getTasks().create(name, RunnableTask.class);

        // Depends on merged resources.
        MergeResources mergeResourcesTask = mVariant.getMergeResources();
        addDependsOn(mergeResourcesTask);
        mMergeResourcesDir = mergeResourcesTask.getOutputDir();
        addInputFile(mMergeResourcesDir);

        // Output directory, the source package directory.
        File outputDir = project.file(String.format(OUTPUT_DIR_BASE, project.getBuildDir(), mVariant.getName()));
        mRunnableTask.setOutputDir(outputDir);

        // Set runnable to generate source.
        mRunnableTask.setRunnable(new Runnable() {
            @Override
            public void run() {
                generateJava();
            }
        });
    }

    public abstract void generateJava();

    public Project getProject() {
        return mProject;
    }

    public BaseVariant getVariant() {
        return mVariant;
    }

    public void addInputFile(File file) {
        mRunnableTask.getInputFiles().add(file);
    }

    public Set<File> getInputFiles() {
        return mRunnableTask.getInputFiles();
    }

    public File getOutputDir() {
        return mRunnableTask.getOutputDir();
    }

    public File getMergeResourcesDir() {
        return mMergeResourcesDir;
    }

    public void addDependsOn(Object dependency) {
        Set<Object> dependencies = mRunnableTask.getDependsOn();
        if (dependencies == null) {
            dependencies = new HashSet<>();
            mRunnableTask.setDependsOn(dependencies);
        }
        dependencies.add(dependency);
    }

    protected RunnableTask getRunnableTask() {
        return mRunnableTask;
    }

    public static class RunnableTask extends DefaultTask {
        private Set<File> mInputFiles = new HashSet<>();
        private File mOutputDir;
        private Runnable mRunnable;

        @InputFiles
        public Set<File> getInputFiles() {
            return mInputFiles;
        }

        public void setOutputDir(File outputDir) {
            mOutputDir = outputDir;
        }

        @OutputDirectory
        public File getOutputDir() {
            return mOutputDir;
        }

        public void setRunnable(Runnable runnable) {
            mRunnable = runnable;
        }

        @TaskAction
        public void generateSource() {
            mRunnable.run();
        }
    }
}