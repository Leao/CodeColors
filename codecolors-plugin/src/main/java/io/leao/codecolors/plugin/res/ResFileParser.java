package io.leao.codecolors.plugin.res;

import org.gradle.api.GradleException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.plugin.aapt.AaptConfig;
import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.xml.XmlCrawler;
import io.leao.codecolors.plugin.xml.XmlUtils;

/**
 * Parses project colors.
 * <p>
 * Returns the list of colors by folder name, and exports the list of {@link CcConfiguration}s by color.
 */
public class ResFileParser implements XmlCrawler.Callback<ResFileParser.XmlCrawlerTrail> {
    private static final String RESOURCES = "resources";
    private static final String RESOURCE_COLOR = "color";
    private static final String RESOURCE_ITEM = "item";

    private static final Set<String> VALID_NODES = new HashSet<String>() {{
        add(RESOURCES);
        add(RESOURCE_COLOR);
        add(RESOURCE_ITEM);
    }};

    private static final String ATTRIBUTE_NAME = "name";

    private static final String COLOR_NAME_REGEX = "[a-z][a-z0-9_]+";

    public void parseFiles(Collection<File> files, Callback callback) {
        for (File file : files) {
            parseFile(file, callback);
        }
    }

    public void parseFile(File file, Callback callback) {
        String folderName = file.getParentFile().getName();
        XmlCrawler.crawl(
                file,
                new XmlCrawlerTrail(folderName, AaptConfig.parse(FileUtils.getQualifier(folderName)), callback),
                this);
    }

    @Override
    public boolean parseNode(Node node, NodeList children, int childCount, XmlCrawlerTrail trail) {
        // Skip uninteresting nodes.
        if (!VALID_NODES.contains(node.getLocalName())) {
            return true;
        }

        if (childCount == 1) {
            Node childNode = node.getFirstChild();
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                String color = XmlUtils.getResourceAttributeValue(node, RESOURCE_COLOR, ATTRIBUTE_NAME);
                if (color != null) {
                    if (!color.matches(COLOR_NAME_REGEX)) {
                        throw new GradleException(
                                "'" + color + "' is not a valid file-based resource name. " +
                                "Code colors need to be defined as file-based resources. " +
                                "Names must contain only lowercase a-z, numbers or underscore, " +
                                "and they must start with a letter.");
                    }
                    trail.callback.parseColor(trail.folderName, color, childNode.getNodeValue(), trail.configuration);
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

    protected static class XmlCrawlerTrail {
        public String folderName;
        public CcConfiguration configuration;
        public Callback callback;

        public XmlCrawlerTrail(String folderName, CcConfiguration configuration, Callback callback) {
            this.folderName = folderName;
            this.configuration = configuration;
            this.callback = callback;
        }
    }

    public interface Callback {
        void parseColor(String folderName, String color, String value, CcConfiguration configuration);
    }
}
