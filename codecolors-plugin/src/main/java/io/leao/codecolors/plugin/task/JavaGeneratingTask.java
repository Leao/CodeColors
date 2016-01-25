package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.plugin.file.FileUtils;

public abstract class JavaGeneratingTask extends GeneratingTask {

    public static void register(JavaGeneratingTask task) {
        // Adds task to variant as a Java source code generator.
        task.getVariant().registerJavaGeneratingTask(task.getGradleTask(), task.getOutputDir());
    }

    public JavaGeneratingTask(Project project, BaseVariant variant, String baseName) {
        super(project, variant, baseName, FileUtils.obtainJavaDirPath(project, variant), false);
    }

    public void addDependsOn(Object dependency) {
        Task runnableTask = getGradleTask();
        Set<Object> dependencies = runnableTask.getDependsOn();
        if (dependencies == null) {
            dependencies = new HashSet<>();
            runnableTask.setDependsOn(dependencies);
        }
        dependencies.add(dependency);
    }
}