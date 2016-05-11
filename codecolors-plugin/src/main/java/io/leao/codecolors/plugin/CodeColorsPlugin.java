package io.leao.codecolors.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.leao.codecolors.plugin.extension.CodeColorsExtension;
import io.leao.codecolors.plugin.task.ColorsJavaGeneratingTask;
import io.leao.codecolors.plugin.task.DependenciesJavaGeneratingTask;
import io.leao.codecolors.plugin.task.JavaGeneratingTask;
import io.leao.codecolors.plugin.task.MergeResourcesConfigurationTask;
import io.leao.codecolors.plugin.task.ResGeneratingTask;
import io.leao.codecolors.plugin.task.ResourcesResGeneratingTask;

/**
 * Plugin to preprocess resources and generate Java sources.
 */
public class CodeColorsPlugin implements Plugin<Project> {

    public void apply(final Project project) {
        // Create plugin extension.
        project.getExtensions().create(CodeColorsExtension.NAME, CodeColorsExtension.class);

        // Hook tasks and actions to project's android extension.
        // Make sure android extension was already evaluated.
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                Object extension = project.getProperties().get("android");
                if (extension instanceof AppExtension) {
                    AppExtension appExtension = (AppExtension) extension;
                    appExtension.getApplicationVariants().all(createResourcesTaskCreator(project));
                } else if (extension instanceof LibraryExtension) {
                    LibraryExtension libraryExtension = (LibraryExtension) extension;
                    libraryExtension.getLibraryVariants().all(createResourcesTaskCreator(project));
                }
            }
        });
    }

    private static Action<BaseVariant> createResourcesTaskCreator(final Project project) {
        return new Action<BaseVariant>() {
            @Override
            public void execute(final BaseVariant variant) {
                ResourcesResGeneratingTask resourcesResGeneratingTask =
                        new ResourcesResGeneratingTask(project, variant);
                ResGeneratingTask.register(resourcesResGeneratingTask);

                MergeResourcesConfigurationTask.create(project, variant, resourcesResGeneratingTask);

                DependenciesJavaGeneratingTask dependenciesJavaGeneratingTask =
                        new DependenciesJavaGeneratingTask(project, variant);
                JavaGeneratingTask.register(dependenciesJavaGeneratingTask);

                ColorsJavaGeneratingTask colorsJavaGeneratingTask =
                        new ColorsJavaGeneratingTask(project, variant, resourcesResGeneratingTask);
                JavaGeneratingTask.register(colorsJavaGeneratingTask);
            }
        };
    }
}
