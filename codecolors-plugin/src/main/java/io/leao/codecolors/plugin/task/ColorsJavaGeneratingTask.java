package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import java.util.Map;
import java.util.Set;

import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.plugin.source.ColorsGenerator;
import io.leao.codecolors.plugin.source.ColorConfigurationsGenerator;

public class ColorsJavaGeneratingTask extends JavaGeneratingTask {
    private static final String NAME_BASE = "generateCcColors";

    public ColorsJavaGeneratingTask(Project project, BaseVariant variant,
                                    ResourcesResGeneratingTask resourcesResGeneratingTask) {
        super(project, variant, NAME_BASE);

        // Depends on resources res generating task.
        addDependsOn(resourcesResGeneratingTask.getGradleTask());
        addInputFile(resourcesResGeneratingTask.getOutputDir());
    }

    @Override
    public void generate(IncrementalTaskInputs inputs) {
        Map<String, Set<CcConfiguration>> colorConfigurations =
                ColorConfigurationsGenerator.getColorConfigurations(getProject(), getVariant());

        ColorsGenerator.generateColors(
                colorConfigurations,
                getVariant().getGenerateBuildConfig().getBuildConfigPackageName(),
                getVariant().getApplicationId(),
                getOutputDir());
    }
}