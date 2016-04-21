package io.leao.codecolors.plugin.source;

import com.google.common.io.ByteStreams;

import org.gradle.api.GradleException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.res.ColorUtils;
import io.leao.codecolors.plugin.xml.XmlUtils;

/**
 * After parsing the list of CodeColors colors, replaces its entries defined in {@code values.xml} files, by a
 * ColorStateList file with the same name.
 */
public class ResourcesGenerator {
    private static final String RESOURCE_NAME_COLOR_BASE = "cc_color_base.xml";
    private static final String RESOURCE_PLACE_HOLDER_COLOR_BASE = "%s";

    private static final String FILE_NAME_XML_BASE = "%s.xml";
    private static final String FILE_NAME_VALUES = "values.xml";

    private static final String FOLDER_COLOR = "color";
    private static final String ATTRIBUTE_NAME = "name";

    private static final String NODE_RESOURCES = "resources";
    private static final String NODE_COLOR = "color";

    private File mResDir;
    private String mColorBaseFileContent;
    private Map<String, Map<String, String>> mFolderColorValue = new HashMap<>();

    public ResourcesGenerator(File resDir) {
        mResDir = resDir;
        mColorBaseFileContent = readResource(RESOURCE_NAME_COLOR_BASE);
    }

    private String readResource(String resourceName) {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/" + resourceName);
            String content = new String(ByteStreams.toByteArray(inputStream));
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new GradleException(String.format("Cannot read resource %s: %s", resourceName, e.toString()));
        }
    }

    public void addColor(String folderName, String color, String value) {
        Map<String, String> colorValue = mFolderColorValue.get(folderName);
        if (colorValue == null) {
            colorValue = new HashMap<>();
            mFolderColorValue.put(folderName, colorValue);
        }
        colorValue.put(color, value);
    }

    public void generate() {
        if (mFolderColorValue.size() > 0) {
            File colorDir = FileUtils.ensureDir(new File(mResDir, FOLDER_COLOR));

            for (String folderName : mFolderColorValue.keySet()) {
                Map<String, String> colorValue = mFolderColorValue.get(folderName);

                generateColors(colorDir, colorValue);
                generateColorsDefaultValues(folderName, colorValue);
            }
        }
    }

    private void generateColors(File colorDir, Map<String, String> colorValue) {
        for (String color : colorValue.keySet()) {
            String outputFileContent = mColorBaseFileContent.replaceAll(
                    RESOURCE_PLACE_HOLDER_COLOR_BASE, ColorUtils.getDefaultValueReference(color));

            File outputFile = new File(colorDir, String.format(FILE_NAME_XML_BASE, color));
            if (!outputFile.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    outputFile.createNewFile();

                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    outputStream.write(outputFileContent.getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    System.out.println("Failed to create color " + color + ": " + e.toString());
                }
            }
        }
    }

    private void generateColorsDefaultValues(String folderName, Map<String, String> colorValue) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            // Resources root node.
            Document document = documentBuilder.newDocument();
            Element resourcesElement = document.createElement(NODE_RESOURCES);
            document.appendChild(resourcesElement);

            // Color nodes.
            for (String color : colorValue.keySet()) {
                Element colorElement = document.createElement(NODE_COLOR);
                resourcesElement.appendChild(colorElement);

                // Set name attribute to color element.
                Attr nameAttr = document.createAttribute(ATTRIBUTE_NAME);
                nameAttr.setValue(ColorUtils.getDefaultValue(color));
                colorElement.setAttributeNode(nameAttr);

                // Color value.
                colorElement.appendChild(document.createTextNode(colorValue.get(color)));
            }

            File valuesDir = FileUtils.ensureDir(new File(mResDir, folderName));
            File valuesFile = FileUtils.ensureFile(new File(valuesDir, FILE_NAME_VALUES));
            XmlUtils.writeTo(document, valuesFile);
        } catch (ParserConfigurationException e) {
            System.out.println("Failed to create values file in " + folderName + " folder: " + e.toString());
        }
    }
}