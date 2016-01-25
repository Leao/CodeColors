package io.leao.codecolors.plugin.file;

import org.gradle.api.Project;

import java.io.File;

public abstract class FileCrawlerResFileCallback<T> implements FileCrawler.Callback<T> {
    private ResFileIdentifier mResFileIdentifier;
    private boolean mInResFolder;

    public FileCrawlerResFileCallback(Project project) {
        this(project, true);
    }

    public FileCrawlerResFileCallback(Project project, boolean inResFolder) {
        mResFileIdentifier = new ResFileIdentifier(project);
        mInResFolder = inResFolder;
    }

    @Override
    public boolean isFileValid(File file, T trail) {
        return mResFileIdentifier.isResFile(file, !mInResFolder);
    }

    @Override
    public boolean isFolderValid(File folder, T trail) {
        // If the crawler does not start in res folder, we check the folder for every file.
        return !mInResFolder || mResFileIdentifier.isValuesFolder(folder);
    }
}
