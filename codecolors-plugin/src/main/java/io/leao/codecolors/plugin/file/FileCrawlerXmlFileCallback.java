package io.leao.codecolors.plugin.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class FileCrawlerXmlFileCallback<T> implements FileCrawler.Callback<T> {

    @Override
    public boolean isFileValid(File file, T trail) {
        try {
            String type = Files.probeContentType(file.toPath());
            return "text/xml".equals(type);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isFolderValid(File folder, T trail) {
        return true;
    }
}
