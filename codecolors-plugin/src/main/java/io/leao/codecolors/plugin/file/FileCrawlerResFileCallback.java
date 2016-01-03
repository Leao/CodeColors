package io.leao.codecolors.plugin.file;

import org.gradle.api.Project;

import java.io.File;

import io.leao.codecolors.plugin.extension.CcPluginExtension;

public abstract class FileCrawlerResFileCallback<T> implements FileCrawler.Callback<T> {
    private static final String RESOURCE_VALUES = "values";

    private final String mResFileName;

    public FileCrawlerResFileCallback(Project project) {
        CcPluginExtension extension = (CcPluginExtension) project.getExtensions().getByName(CcPluginExtension.NAME);
        mResFileName = extension.getResFileName();
    }

    @Override
    public boolean isFileValid(File file) {
        return mResFileName.equals(file.getName());
    }

    @Override
    public boolean isFolderValid(File folder) {
        String folderName = folder.getName().toLowerCase();
        return folderName.startsWith(RESOURCE_VALUES);
    }
}
