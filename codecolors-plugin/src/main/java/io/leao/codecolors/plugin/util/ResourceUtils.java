package io.leao.codecolors.plugin.util;

import com.google.common.io.ByteStreams;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    public static String readResource(String resourceName) {
        try {
            InputStream inputStream = ResourceUtils.class.getResourceAsStream("/" + resourceName);
            String content = new String(ByteStreams.toByteArray(inputStream));
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new GradleException(String.format("Cannot read resource %s: %s", resourceName, e.toString()));
        }
    }

    public static File getResourceAsFile(String resourceName) {
        try {
            InputStream inputStream = ResourceUtils.class.getResourceAsStream("/" + resourceName);

            File tempFile = File.createTempFile(resourceName, ".tmp");
            tempFile.deleteOnExit();

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            //copy stream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return tempFile;
        } catch (IOException e) {
            throw new GradleException(String.format("Cannot read resource %s: %s", resourceName, e.toString()));
        }
    }
}
