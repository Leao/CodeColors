package io.leao.codecolors.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.leao.codecolors.plugin.extension.CcPluginExtension;
import io.leao.codecolors.plugin.res.ColorsParseMergeAction;
import io.leao.codecolors.plugin.task.CcColorsTask;
import io.leao.codecolors.plugin.task.CcDependenciesTask;
import io.leao.codecolors.plugin.task.JavaGeneratingTask;
import io.leao.codecolors.plugin.task.SdkDependenciesTask;

/**
 * Plugin to preprocess resources and generate Java sources.
 */
public class CodeColorsPlugin implements Plugin<Project> {

    public void apply(final Project project) {
        // Create plugin extension.
        project.getExtensions().create(CcPluginExtension.NAME, CcPluginExtension.class);

        // Hook tasks and actions to project's android extension.
        // Make sure android extension was already evaluated.
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                Object extension = project.getProperties().get("android");
                if (extension instanceof AppExtension) {
                    AppExtension appExtension = (AppExtension) extension;
                    appExtension.getApplicationVariants().all(createResourcesTaskCreator(project, appExtension));
                } else if (extension instanceof LibraryExtension) {
                    LibraryExtension libraryExtension = (LibraryExtension) extension;
                    libraryExtension.getLibraryVariants().all(createResourcesTaskCreator(project, libraryExtension));
                }
            }
        });
    }

    private static Action<BaseVariant> createResourcesTaskCreator(final Project project,
                                                                  final BaseExtension extension) {
        return new Action<BaseVariant>() {
            @Override
            public void execute(final BaseVariant variant) {
                ColorsParseMergeAction.create(project, variant);

                SdkDependenciesTask sdkDependenciesTask = SdkDependenciesTask.create(project, extension, variant);

                CcDependenciesTask ccDependenciesTask = new CcDependenciesTask(project, variant, sdkDependenciesTask);
                JavaGeneratingTask.register(ccDependenciesTask);

                CcColorsTask ccColorsTask = new CcColorsTask(project, variant);
                JavaGeneratingTask.register(ccColorsTask);
            }
        };
    }
}
