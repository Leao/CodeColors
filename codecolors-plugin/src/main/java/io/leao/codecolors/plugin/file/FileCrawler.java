package io.leao.codecolors.plugin.file;

import java.io.File;

public class FileCrawler {
    public static <T> void crawl(File root, T trail, Callback<T> callback) {
        if (root.exists()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (callback.isFileValid(file, trail)) {
                            callback.parseFile(file, trail);
                        }
                    } else {
                        if (callback.isFolderValid(file, trail)) {
                            crawl(file, callback.createTrail(file, trail), callback);
                        }
                    }
                }
            }
        } else {
            System.out.println("Warning crawling folder: " + root + " doesn't exist");
        }
    }

    public interface Callback<T> {
        void parseFile(File file, T trail);

        T createTrail(File folder, T trail);

        boolean isFileValid(File file, T trail);

        boolean isFolderValid(File folder, T trail);
    }
}
