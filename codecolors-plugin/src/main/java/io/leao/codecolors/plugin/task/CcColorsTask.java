package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;

import java.util.Map;
import java.util.Set;

import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.plugin.res.ColorsParser;
import io.leao.codecolors.plugin.source.CcColorsGenerator;

public class CcColorsTask extends JavaGeneratingTask {
    private static final String NAME_BASE = "generateCcColors";

    public CcColorsTask(Project project, BaseVariant variant) {
        super(project, variant, NAME_BASE);
    }

    @Override
    public void generateJava() {
        Map<String, Set<CcConfiguration>> colorConfigurations = ColorsParser.getColorConfigurations(getProject());

        CcColorsGenerator.generateColors(
                colorConfigurations,
                getVariant().getGenerateBuildConfig().getBuildConfigPackageName(),
                getVariant().getApplicationId(),
                getOutputDir());
    }
}