package io.leao.codecolors.plugin.extension;

import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CodeColorsExtension {
    public static final String NAME = "codecolors";
    public static final String RES_FILE_NAME_DEFAULT = "codecolors.xml";
    private static final String RES_FILE_NAME_XML_BASE = "%s.xml";

    private String mResFileName = RES_FILE_NAME_DEFAULT;

    public void resFileName(String resFileName) {
        if (resFileName != null && resFileName.length() > 0) {
            try {
                String type = Files.probeContentType(Paths.get(resFileName));
                if ("text/xml".equals(type)) {
                    mResFileName = resFileName;
                } else {
                    mResFileName = String.format(RES_FILE_NAME_XML_BASE, resFileName);
                }
            } catch (IOException e) {
                // Do nothing.
            }
        }
    }

    public String getResFileName() {
        return mResFileName;
    }

    public static String getResFileName(Project project) {
        CodeColorsExtension extension = (CodeColorsExtension) project.getExtensions().getByName(CodeColorsExtension.NAME);
        return extension != null ? extension.getResFileName() : null;
    }
}