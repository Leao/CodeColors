package io.leao.codecolors.plugin.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileUtils {
    public static Object readFrom(File input) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(input));
            Object object = inputStream.readObject();
            inputStream.close();
            return object;
        } catch (Exception e) {
            System.out.println("Failed to read object from " + input.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    public static void writeTo(File output, Serializable object) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(output));
            outputStream.writeObject(object);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Failed to write object to " + output.getPath() + ": " + e.getMessage());
        }
    }

    public static String getQualifier(File folder) {
        String name = folder.getName();
        String[] parts = name.split("\\-", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
