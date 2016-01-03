package io.leao.codecolors.plugin.res;

import com.android.build.gradle.api.BaseVariant;
import com.google.common.io.ByteStreams;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.xml.XmlCrawler;
import io.leao.codecolors.plugin.xml.XmlUtils;

/**
 * After parsing the list of CodeColors colors, replaces its entries defined in {@code values.xml} files, by a
 * ColorStateList file with the same name.
 */
public class ColorsMerger implements XmlCrawler.Callback<ColorsMerger.XmlCrawlerTrail> {
    private static final String RES_FILE_NAME = "cc_res_base.xml";
    private static final String RES_FILE_PLACE_HOLDER = "%s";

    private static final String XML_FILE_NAME_BASE = "%s.xml";

    private static final String FOLDER_PATH_BASE = "%s\\%s";
    private static final String FOLDER_PATH_QUALIFIER_BASE = "%s\\%s-%s";

    private static final String RESOURCE_COLOR = "color";
    private static final String RESOURCE_VALUES = "values";

    private static final String ATTRIBUTE_NAME = "name";

    private File mMergeResourcesOutputDir;

    public ColorsMerger(BaseVariant variant) {
        mMergeResourcesOutputDir = variant.getMergeResources().getOutputDir();
    }

    public void mergeColors(Map<String, Set<String>> folderColors) {
        for (String folderName : folderColors.keySet()) {
            File outputDir = new File(getFolderPath(folderName, RESOURCE_VALUES));
            File outputFile = new File(outputDir, String.format(XML_FILE_NAME_BASE, outputDir.getName()));

            XmlCrawlerTrail trail = new XmlCrawlerTrail(folderColors.get(folderName));
            Document document = XmlCrawler.crawl(outputFile, trail, this);

            if (document != null && trail.nodesChanged) {
                XmlUtils.writeTo(document, outputFile);
            }
        }
    }

    @Override
    public boolean parseNode(Node node, NodeList children, int childCount, XmlCrawlerTrail trail) {
        if (childCount == 1) {
            Node childNode = node.getFirstChild();
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                String color = XmlUtils.getResourceAttributeValue(node, RESOURCE_COLOR, ATTRIBUTE_NAME);

                if (trail.colors.contains(color)) {
                    // Change node name to reference it as the default value.
                    String colorDefaultValue = ColorUtils.getDefaultValue(color);
                    XmlUtils.setResourceAttributeValue(node, RESOURCE_COLOR, ATTRIBUTE_NAME, colorDefaultValue);

                    trail.nodesChanged = true; // Makes sure to write the document in the end.

                    // Replace the removed node by a new file ColorStateList file.
                    String colorDefaultValueReference = ColorUtils.getDefaultValueReference(color);
                    createColor(color, colorDefaultValueReference);
                }
                return true; // Stop node crawl.
            }
        }
        return false; // Continue node crawl.
    }

    @Override
    public XmlCrawlerTrail createTrail(Node node, XmlCrawlerTrail trail) {
        return trail;
    }

    private void createColor(String color, String value) {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/" + RES_FILE_NAME);
            String baseFileContent = new String(ByteStreams.toByteArray(inputStream));
            inputStream.close();

            String outputFileContent = baseFileContent.replaceAll(RES_FILE_PLACE_HOLDER, value);

            File outputDir = new File(getFolderPath(RESOURCE_COLOR));
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, String.format(XML_FILE_NAME_BASE, color));
            if (!outputFile.exists()) {
                outputFile.createNewFile();

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                outputStream.write(outputFileContent.getBytes());
                outputStream.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to create color " + color + ": " + e.toString());
        }
    }

    private String getFolderPath(String resFolderName) {
        return String.format(FOLDER_PATH_BASE, mMergeResourcesOutputDir.getPath(), resFolderName);
    }

    private String getFolderPath(String folderName, String resFolderName) {
        String qualifier = FileUtils.getQualifier(folderName);
        return qualifier.length() == 0 ?
                getFolderPath(resFolderName) :
                String.format(FOLDER_PATH_QUALIFIER_BASE, mMergeResourcesOutputDir.getPath(), resFolderName, qualifier);
    }

    protected static class XmlCrawlerTrail {
        public Set<String> colors;
        public boolean nodesChanged;

        public XmlCrawlerTrail(Set<String> colors) {
            this.colors = colors;
            nodesChanged = false;
        }
    }
}