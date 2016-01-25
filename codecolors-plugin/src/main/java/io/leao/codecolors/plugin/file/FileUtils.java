package io.leao.codecolors.plugin.file;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

public class FileUtils {
    private static final String INTERMEDIATES_DIR_BASE = "%s\\intermediates\\codecolors\\%s";
    private static final String RES_DIR_BASE = "%s\\res"; // First part is intermediates dir path.
    private static final String JAVA_DIR_BASE = "%s\\generated\\source\\codecolors\\%s";

    public static File ensureDir(File dir) {
        if (dir.exists() || dir.mkdirs()) {
            return dir;
        }
        System.out.println(String.format("Failed to create directory %s.", dir.getPath()));
        return null;
    }

    public static File ensureFile(File file) {
        try {
            if (file.exists() || file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            // Do nothing.
        }
        System.out.println(String.format("Failed to create file %s.", file.getPath()));
        return null;
    }

    /**
     * @return true, if the path of some {@code files} starts with the path in {@code folder}; false, otherwise.
     */
    public static boolean inFolder(File folder, Set<File> files) {
        if (folder.isFile()) {
            return files.contains(folder);
        }

        for (File file : files) {
            if (file.getPath().startsWith(folder.getPath())) {
                return true;
            }
        }
        return false;
    }

    public static Object readFrom(File input) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(input));
            Object object = inputStream.readObject();
            inputStream.close();
            return object;
        } catch (Exception e) {
            System.out.println("Failed to read object from " + input.getPath() + ": " + e.toString());
            return null;
        }
    }

    public static void writeTo(Serializable object, File output) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(output));
            outputStream.writeObject(object);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Failed to write object to " + output.getPath() + ": " + e.toString());
        }
    }

    public static String getQualifier(File folder) {
        return getQualifier(folder.getName());
    }

    public static String getQualifier(String folderName) {
        String[] parts = folderName.split("\\-", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    public static String obtainIntermediatesDirPath(Project project, BaseVariant variant) {
        return String.format(INTERMEDIATES_DIR_BASE, project.getBuildDir(), variant.getName());
    }

    public static File obtainIntermediatesDirFile(Project project, BaseVariant variant) {
        return ensureDir(project.file(obtainIntermediatesDirPath(project, variant)));
    }

    public static String obtainResDirPath(Project project, BaseVariant variant) {
        return String.format(RES_DIR_BASE, obtainIntermediatesDirPath(project, variant));
    }

    public static File obtainResDirFile(Project project, BaseVariant variant) {
        return ensureDir(project.file(obtainResDirPath(project, variant)));
    }

    public static String obtainJavaDirPath(Project project, BaseVariant variant) {
        return String.format(JAVA_DIR_BASE, project.getBuildDir(), variant.getName());
    }

    public static File obtainJavaDirFile(Project project, BaseVariant variant) {
        return ensureDir(project.file(obtainJavaDirPath(project, variant)));
    }
}
