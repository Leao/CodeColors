package io.leao.codecolors.plugin.file;

import java.io.File;

public class FileCrawler {
    public static <T> void crawl(File root, T trail, Callback<T> callback) {
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (callback.isFileValid(file)) {
                        callback.parseFile(file, trail);
                    }
                } else {
                    if (callback.isFolderValid(file)) {
                        crawl(file, callback.createTrail(file, trail), callback);
                    }
                }
            }
        }
    }

    public interface Callback<T> {
        void parseFile(File file, T trail);

        T createTrail(File folder, T trail);

        boolean isFileValid(File file);

        boolean isFolderValid(File folder);
    }
}
