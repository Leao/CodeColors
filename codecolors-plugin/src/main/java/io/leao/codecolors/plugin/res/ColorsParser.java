package io.leao.codecolors.plugin.res;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.SourceProvider;

import org.gradle.api.Project;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.leao.codecolors.plugin.aapt.AaptConfig;
import io.leao.codecolors.plugin.file.FileCrawler;
import io.leao.codecolors.plugin.file.FileCrawlerResFileCallback;
import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.xml.XmlCrawler;
import io.leao.codecolors.plugin.xml.XmlUtils;

/**
 * Parses project colors.
 * <p/>
 * Returns the list of colors by folder name, and exports the list of {@link CcConfiguration}s by color.
 */
public class ColorsParser
        extends FileCrawlerResFileCallback<String>
        implements XmlCrawler.Callback<ColorsParser.XmlCrawlerTrail> {
    private static final String OUTPUT_DIR_BASE = "%s\\intermediates\\codecolors\\";
    private static final String COLOR_CONFIGURATIONS_FILE_NAME = "ColorConfigurations.ser";

    private static final String RESOURCE_COLOR = "color";
    private static final String ATTRIBUTE_NAME = "name";

    private Project mProject;
    private BaseVariant mVariant;
    private Map<String, Set<String>> mFolderColors = new HashMap<>();
    private HashMap<String, Set<CcConfiguration>> mColorConfigurations = new HashMap<>();

    protected ColorsParser(Project project, BaseVariant variant) {
        super(project);
        mProject = project;
        mVariant = variant;
    }

    public Map<String, Set<String>> parseColors() {
        // Fill mFolderColors, and mColorConfigurations.
        for (SourceProvider sourceProvider : mVariant.getSourceSets()) {
            for (File file : sourceProvider.getResDirectories()) {
                FileCrawler.crawl(file, file.getName(), this);
            }
        }

        File outputDir = createOutputDirFile(mProject);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Write color configurations.
        try {
            File colorConfigurationsFile = createColorConfigurationsOutputFile(outputDir);
            if (!colorConfigurationsFile.exists()) {
                colorConfigurationsFile.createNewFile();
            }
            FileUtils.writeTo(mColorConfigurations, colorConfigurationsFile);
        } catch (IOException e) {
            System.out.println("Failed to create color configurations file: " + e.toString());
        }

        return mFolderColors;
    }

    /**
     * @param trail the color folder name where the generated resources should be placed.
     */
    @Override
    public void parseFile(File file, String trail) {
        XmlCrawler.crawl(file, new XmlCrawlerTrail(trail, AaptConfig.parse(FileUtils.getQualifier(trail))), this);
    }

    @Override
    public String createTrail(File folder, String trail) {
        return folder.getName();
    }

    @Override
    public boolean parseNode(Node node, NodeList children, int childCount, XmlCrawlerTrail trail) {
        if (childCount == 1) {
            Node childNode = node.getFirstChild();
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                String color = XmlUtils.getResourceAttributeValue(node, RESOURCE_COLOR, ATTRIBUTE_NAME);
                addColor(trail.folderName, color, trail.configuration);
                return true; // Stop node crawl.
            }
        }
        return false; // Continue node crawl.
    }

    @Override
    public XmlCrawlerTrail createTrail(Node node, XmlCrawlerTrail trail) {
        return trail;
    }

    private void addColor(String folderName, String color, CcConfiguration configuration) {
        Set<CcConfiguration> configurations = mColorConfigurations.get(color);
        if (configurations == null) {
            configurations = new TreeSet<>();
            mColorConfigurations.put(color, configurations);
        }
        configurations.add(configuration);

        Set<String> colors = mFolderColors.get(folderName);
        if (colors == null) {
            colors = new HashSet<>();
            mFolderColors.put(folderName, colors);
        }
        colors.add(color);
    }

    private static File createOutputDirFile(Project project) {
        return project.file(String.format(OUTPUT_DIR_BASE, project.getBuildDir()));
    }

    private static File createColorConfigurationsOutputFile(File outputDir) {
        return new File(outputDir, COLOR_CONFIGURATIONS_FILE_NAME);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Set<CcConfiguration>> getColorConfigurations(Project project) {
        return (Map<String, Set<CcConfiguration>>)
                FileUtils.readFrom(createColorConfigurationsOutputFile(createOutputDirFile(project)));
    }

    protected static class XmlCrawlerTrail {
        public String folderName;
        public CcConfiguration configuration;

        public XmlCrawlerTrail(String folderName, CcConfiguration configuration) {
            this.folderName = folderName;
            this.configuration = configuration;
        }
    }
}
