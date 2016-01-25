package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;

import io.leao.codecolors.plugin.file.FileUtils;

public abstract class ResGeneratingTask extends GeneratingTask {

    public static void register(ResGeneratingTask task) {
        // Adds task to variant as a resources generator.
        task.getVariant().registerResGeneratingTask(task.getGradleTask(), task.getOutputDir());
    }

    public ResGeneratingTask(Project project, BaseVariant variant, String baseName) {
        super(project, variant, baseName, FileUtils.obtainResDirPath(project, variant), true);
    }
}