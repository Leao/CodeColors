package io.leao.codecolors.plugin.res;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import io.leao.codecolors.plugin.aapt.AaptConfig;
import io.leao.codecolors.plugin.file.FileCrawler;
import io.leao.codecolors.plugin.file.FileCrawlerXmlFileCallback;
import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.xml.XmlUtils;
import io.leao.codecolors.plugin.xml.XmlCrawler;

public class ResourcesDependenciesParser
        extends FileCrawlerXmlFileCallback<CcConfiguration>
        implements XmlCrawler.Callback<ResourcesDependenciesParser.XmlCrawlerTrail> {
    private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";

    private static final String RESOURCE_DRAWABLE = "drawable";
    private static final String RESOURCE_COLOR = "color";
    private static final String RESOURCE_ITEM = "item";
    private static final String RESOURCE_VALUES = "values";

    private static final String ATTRIBUTE_DRAWABLE = "drawable";
    private static final String ATTRIBUTE_COLOR = "color";
    private static final String ATTRIBUTE_TINT = "tint";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TYPE = "type";

    private static final String PREFIX_DRAWABLE = "@drawable";
    private static final String PREFIX_ANDROID_DRAWABLE = "@android:drawable";
    private static final String PREFIX_COLOR = "@color";
    private static final String PREFIX_ANDROID_COLOR = "@android:color";
    private static final String PREFIX_ATTR = "?attr";
    private static final String PREFIX_ANDROID_ATTR = "?android:attr";

    private static final String[] RESOURCES_PREFIXES = {
            PREFIX_DRAWABLE,
            PREFIX_ANDROID_DRAWABLE,
            PREFIX_COLOR,
            PREFIX_ANDROID_COLOR,
            PREFIX_ATTR,
            PREFIX_ANDROID_ATTR
    };

    private static final Set<String> sDependencyAttributes = new HashSet<String>() {{
        add(ATTRIBUTE_DRAWABLE);
        add(ATTRIBUTE_COLOR);
        add(ATTRIBUTE_TINT);
    }};

    private static final String sDependencyRegex = createDependencyRegex();

    private final Resource.Pool mResourcesPool;

    public ResourcesDependenciesParser(Resource.Pool resourcesPool) {
        mResourcesPool = resourcesPool;
    }

    public void parseDependencies(File resourcesDir) {
        FileCrawler.crawl(resourcesDir, CcConfiguration.EMPTY, this);
    }

    public Set<Resource> getResources() {
        return mResourcesPool.getResources();
    }

    /**
     * @param trail the {@link CcConfiguration} for the folder where the file is located.
     */
    @Override
    public void parseFile(File file, CcConfiguration trail) {
        File folder = file.getParentFile();
        String folderName = folder.getName().toLowerCase();
        if (folderName.startsWith(RESOURCE_DRAWABLE)) {
            String fileName = file.getName();
            crawlXmlFile(
                    file,
                    trail,
                    mResourcesPool.getOrCreateResource(
                            fileName.substring(0, fileName.indexOf('.')), Resource.Type.DRAWABLE));

        } else if (folderName.startsWith(RESOURCE_COLOR)) {
            String fileName = file.getName();
            crawlXmlFile(
                    file,
                    trail,
                    mResourcesPool.getOrCreateResource(
                            fileName.substring(0, fileName.indexOf('.')), Resource.Type.COLOR));
        } else if (folderName.startsWith(RESOURCE_VALUES)) {
            crawlXmlFile(file, trail, null);
        }
    }

    @Override
    public CcConfiguration createTrail(File folder, CcConfiguration trail) {
        return AaptConfig.parse(FileUtils.getQualifier(folder));
    }

    private void crawlXmlFile(File file, CcConfiguration configuration, Resource resource) {
        XmlCrawler.crawl(file, new XmlCrawlerTrail(configuration, resource), this);
    }

    @Override
    public boolean parseNode(Node node, NodeList children, int childCount, XmlCrawlerTrail trail) {
        // Extract dependencies from attributes and text content.
        if (trail.resource != null) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (NAMESPACE_ANDROID.equals(attribute.getNamespaceURI()) &&
                        sDependencyAttributes.contains(attribute.getLocalName())) {
                    addDependencyIfValid(trail.configuration, trail.resource, attribute.getNodeValue());
                }
            }

            if (childCount == 1) {
                Node childNode = node.getFirstChild();
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    // If the last node is a text node, try to add it as dependency and end parsing.
                    addDependencyIfValid(trail.configuration, trail.resource, childNode.getTextContent());
                    return true; // Stop node crawl.
                }
            }
        }
        return false; // Continue node crawl.
    }

    @Override
    public XmlCrawlerTrail createTrail(Node node, XmlCrawlerTrail trail) {
        return new XmlCrawlerTrail(trail.configuration, createResourceFromNodeIfNeeded(node, trail.resource));
    }

    private Resource createResourceFromNodeIfNeeded(Node node, Resource resource) {
        if (resource != null) {
            return resource;
        }

        String name = null;
        Resource.Type type = null;
        String currentNodeName = node.getNodeName();
        if (currentNodeName.startsWith(RESOURCE_DRAWABLE)) {
            name = XmlUtils.getAttributeValue(node, ATTRIBUTE_NAME);
            type = Resource.Type.DRAWABLE;
        } else if (currentNodeName.startsWith(RESOURCE_COLOR)) {
            name = XmlUtils.getAttributeValue(node, ATTRIBUTE_NAME);
            type = Resource.Type.COLOR;
        } else if (currentNodeName.startsWith(RESOURCE_ITEM)) {
            String typeValue = XmlUtils.getAttributeValue(node, ATTRIBUTE_TYPE);
            if (RESOURCE_DRAWABLE.equals(typeValue)) {
                name = XmlUtils.getAttributeValue(node, ATTRIBUTE_NAME);
                type = Resource.Type.DRAWABLE;
            } else if (RESOURCE_COLOR.equals(typeValue)) {
                name = XmlUtils.getAttributeValue(node, ATTRIBUTE_NAME);
                type = Resource.Type.COLOR;
            }
        }
        return name != null ? mResourcesPool.getOrCreateResource(name, type) : null;
    }

    private void addDependencyIfValid(CcConfiguration configuration, Resource resource, String dependency) {
        if (isValidDependency(dependency)) {
            resource.addDependency(configuration, getOrCreateResourceFromDependency(dependency));
        }
    }

    private static boolean isValidDependency(String dependency) {
        return dependency != null && dependency.matches(sDependencyRegex);
    }

    private Resource getOrCreateResourceFromDependency(String dependency) {
        String[] dependencyParts = dependency.split("/");
        if (dependencyParts.length == 2 && dependencyParts[0] != null && dependencyParts[1] != null) {
            switch (dependencyParts[0]) {
                case PREFIX_DRAWABLE:
                    return mResourcesPool.getOrCreateResource(dependencyParts[1], Resource.Type.DRAWABLE);
                case PREFIX_ANDROID_DRAWABLE:
                    return mResourcesPool.getOrCreateResource(dependencyParts[1], Resource.Type.ANDROID_DRAWABLE);
                case PREFIX_COLOR:
                    return mResourcesPool.getOrCreateResource(dependencyParts[1], Resource.Type.COLOR);
                case PREFIX_ANDROID_COLOR:
                    return mResourcesPool.getOrCreateResource(dependencyParts[1], Resource.Type.ANDROID_COLOR);
                case PREFIX_ATTR:
                    return mResourcesPool.getOrCreateResource(dependencyParts[1], Resource.Type.ATTR);
                case PREFIX_ANDROID_ATTR:
                    return mResourcesPool.getOrCreateResource(dependencyParts[1], Resource.Type.ANDROID_ATTR);
                default:
                    // Throw.
            }
        }
        throw new IllegalStateException(
                "Error: impossible to map resource with name " + dependency + " to a valid Resource.");
    }

    private static String createDependencyRegex() {
        String dependencyRegex = "(";
        for (int i = 0; i < RESOURCES_PREFIXES.length; i++) {
            if (i > 0) {
                dependencyRegex += "|";
            }
            dependencyRegex += Pattern.quote(RESOURCES_PREFIXES[i]);
        }
        dependencyRegex += ")/\\w+$";
        return dependencyRegex;
    }

    protected static class XmlCrawlerTrail {
        public CcConfiguration configuration;
        public Resource resource;

        public XmlCrawlerTrail(CcConfiguration configuration, Resource resource) {
            this.configuration = configuration;
            this.resource = resource;
        }
    }
}
