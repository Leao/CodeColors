package io.leao.codecolors.plugin.res;

import java.io.File;

public class ResourceFileUtils {
    public static String getQualifier(File folder) {
        String name = folder.getName();
        String[] parts = name.split("\\-", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
