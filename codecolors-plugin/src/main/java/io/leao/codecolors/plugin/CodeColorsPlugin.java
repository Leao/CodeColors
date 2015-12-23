package io.leao.codecolors.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.leao.codecolors.plugin.task.CcDependenciesTask;
import io.leao.codecolors.plugin.task.SdkDependenciesTask;

/**
 * Plugin to preprocess resources and generate Java sources.
 */
public class CodeColorsPlugin implements Plugin<Project> {

    public void apply(final Project project) {
        Object extension = project.getProperties().get("android");
        if (extension instanceof AppExtension) {
            AppExtension appExtension = (AppExtension) extension;
            appExtension.getApplicationVariants().all(createResourcesTaskCreator(project, appExtension));
        } else if (extension instanceof LibraryExtension) {
            LibraryExtension libraryExtension = (LibraryExtension) extension;
            libraryExtension.getLibraryVariants().all(createResourcesTaskCreator(project, libraryExtension));
        }
    }

    private static Action<BaseVariant> createResourcesTaskCreator(final Project project,
                                                                  final BaseExtension extension) {
        return new Action<BaseVariant>() {
            @Override
            public void execute(final BaseVariant variant) {
                CcDependenciesTask.create(
                        project,
                        variant,
                        SdkDependenciesTask.create(project, extension, variant));
            }
        };
    }
}
