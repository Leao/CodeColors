package io.leao.codecolors.plugin.file;

import org.gradle.api.Project;

import java.io.File;

import io.leao.codecolors.plugin.extension.CodeColorsExtension;

public class ResFileIdentifier {
    private static final String RESOURCE_VALUES = "values";

    private final String mResFileName;

    public ResFileIdentifier(Project project) {
        mResFileName = CodeColorsExtension.getResFileName(project);
    }

    public boolean isResFile(File file, boolean checkFolder) {
        return mResFileName.equals(file.getName()) && (!checkFolder || isValuesFolder(file));
    }

    public boolean isValuesFolder(File file) {
        File folder;
        if (file.isDirectory()) {
            folder = file;
        } else {
            folder = file.getParentFile();
        }
        return folder.getName().startsWith(RESOURCE_VALUES);
    }
}
