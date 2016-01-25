package io.leao.codecolors.plugin.source;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.res.CcConfiguration;

public class ColorConfigurationsGenerator {
    private static final String COLOR_CONFIGURATIONS_FILE_NAME = "ColorConfigurations.ser";

    private File mColorConfigurationsOutputFile;
    private HashMap<String, Set<CcConfiguration>> mColorConfigurations = new HashMap<>();

    public ColorConfigurationsGenerator(Project project, BaseVariant variant) {
        mColorConfigurationsOutputFile = createColorConfigurationsOutputFile(project, variant);
    }

    public void addColor(String color, CcConfiguration configuration) {
        Set<CcConfiguration> configurations = mColorConfigurations.get(color);
        if (configurations == null) {
            configurations = new TreeSet<>();
            mColorConfigurations.put(color, configurations);
        }
        configurations.add(configuration);
    }

    public void generate() {
        FileUtils.writeTo(mColorConfigurations, mColorConfigurationsOutputFile);
    }

    private static File createColorConfigurationsOutputFile(Project project, BaseVariant variant) {
        return FileUtils.ensureFile(
                new File(FileUtils.obtainIntermediatesDirFile(project, variant), COLOR_CONFIGURATIONS_FILE_NAME));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Set<CcConfiguration>> getColorConfigurations(Project project, BaseVariant variant) {
        return (Map<String, Set<CcConfiguration>>)
                FileUtils.readFrom(createColorConfigurationsOutputFile(project, variant));
    }
}
