package io.leao.codecolors.plugin.res;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.leao.codecolors.plugin.aapt.AaptConfig;

public class ResourceDependencyHandler {
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
    private static final String PREFIX_COLOR = "@color";
    private static final String PREFIX_ATTR = "?attr";
    private static final String PREFIX_ANDROID_ATTR = "?android:attr";

    private static final String[] RESOURCES_PREFIXES =
            {PREFIX_DRAWABLE, PREFIX_COLOR, PREFIX_ATTR, PREFIX_ANDROID_ATTR};

    private static final String sDependencyRegex = createDependencyRegex();

    private final Set<CodeColorsConfiguration> mConfigurations = new TreeSet<>();

    private final Map<Resource, Map<CodeColorsConfiguration, Set<Resource>>> mResourceConfigurationDependencies =
            new HashMap<>();

    public void processDependencies(File resourcesDir) {
        processDependencies(CodeColorsConfiguration.EMPTY, resourcesDir);
    }

    public Set<CodeColorsConfiguration> getConfigurations() {
        return mConfigurations;
    }

    public Map<Resource, Map<CodeColorsConfiguration, Set<Resource>>> getDependencies() {
        return mResourceConfigurationDependencies;
    }

    private void processDependencies(CodeColorsConfiguration configuration, File resourcesDir) {
        File[] files = resourcesDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    processFile(configuration, file);
                } else {
                    String name = file.getName();
                    String[] parts = name.split("\\-", 2);
                    String qualifier = parts.length > 1 ? parts[1] : "";
                    processDependencies(AaptConfig.parse(qualifier), file);
                }
            }
        }
    }

    private void processFile(CodeColorsConfiguration configuration, File file) {
        try {
            String type = Files.probeContentType(file.toPath());
            if ("text/xml".equals(type)) {
                String parentFolder = file.getParentFile().getName().toLowerCase();
                if (parentFolder.startsWith(RESOURCE_DRAWABLE)) {
                    String fileName = file.getName();
                    parseXmlFile(
                            configuration,
                            file,
                            new Resource(fileName.substring(0, fileName.indexOf('.')), Resource.Type.DRAWABLE));
                } else if (parentFolder.startsWith(RESOURCE_COLOR)) {
                    String fileName = file.getName();
                    parseXmlFile(
                            configuration,
                            file,
                            new Resource(fileName.substring(0, fileName.indexOf('.')), Resource.Type.COLOR));
                } else if (parentFolder.startsWith(RESOURCE_VALUES)) {
                    parseXmlFile(configuration, file, null);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Error processing file " + file.getName() + ": " + e.toString());
        }
    }

    private void parseXmlFile(CodeColorsConfiguration configuration, File file, Resource resource)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(file);

        parseNode(configuration, document.getDocumentElement(), resource);
    }

    public void parseNode(CodeColorsConfiguration configuration, Node node, Resource resource) {
        NodeList childNodes = node.getChildNodes();
        int childCount = childNodes.getLength();

        // Extract dependencies from attributes and text content.
        if (resource != null) {
            NamedNodeMap attributes = node.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Node attribute = attributes.item(j);

                if (NAMESPACE_ANDROID.equals(attribute.getNamespaceURI())) {
                    switch (attribute.getLocalName()) {
                        case ATTRIBUTE_DRAWABLE:
                        case ATTRIBUTE_TINT:
                        case ATTRIBUTE_COLOR:
                            addDependencyIfValid(configuration, resource, attribute.getNodeValue());
                            break;
                        default:
                            break;
                    }
                }
            }

            if (childCount == 1) {
                Node childNode = node.getFirstChild();
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    // If the last node is a text node, try to add it as dependency and end parsing.
                    addDependencyIfValid(configuration, resource, childNode.getTextContent());
                    return;
                }
            }
        }

        // Parse the remaining nodes.
        for (int i = 0; i < childCount; i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                parseNode(configuration, childNode, createResourceFromNode(resource, childNode));
            }
        }
    }

    private static Resource createResourceFromNode(Resource resource, Node node) {
        if (resource != null) {
            return resource;
        }

        String name = null;
        Resource.Type type = null;
        String currentNodeName = node.getNodeName();
        if (currentNodeName.startsWith(RESOURCE_DRAWABLE)) {
            name = getNodeNameAttr(node);
            type = Resource.Type.DRAWABLE;
        } else if (currentNodeName.startsWith(RESOURCE_COLOR)) {
            name = getNodeNameAttr(node);
            type = Resource.Type.COLOR;
        } else if (currentNodeName.startsWith(RESOURCE_ITEM)) {
            Node typeAttr = node.getAttributes().getNamedItem(ATTRIBUTE_TYPE);
            if (typeAttr != null) {
                if (RESOURCE_DRAWABLE.equals(typeAttr.getNodeValue())) {
                    name = getNodeNameAttr(node);
                    type = Resource.Type.DRAWABLE;
                } else if (RESOURCE_COLOR.equals(typeAttr.getNodeValue())) {
                    name = getNodeNameAttr(node);
                    type = Resource.Type.COLOR;
                }
            }
        }
        return name != null ? new Resource(name, type) : null;
    }

    private static String getNodeNameAttr(Node node) {
        Node nameAttr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME);
        if (nameAttr != null) {
            return nameAttr.getNodeValue();
        } else {
            return null;
        }
    }

    private void addDependencyIfValid(CodeColorsConfiguration configuration, Resource resource, String dependency) {
        if (isValidDependency(dependency)) {
            // Make sure to add the configuration to the list of configurations.
            mConfigurations.add(configuration);
            // Add dependency to resource's dependencies set.
            Set<Resource> dependencies = getDependencies(resource, configuration);
            dependencies.add(createResourceFromDependency(dependency));
        }
    }

    private static boolean isValidDependency(String dependency) {
        return dependency != null && dependency.matches(sDependencyRegex);
    }

    private Set<Resource> getDependencies(Resource resource, CodeColorsConfiguration configuration) {
        Map<CodeColorsConfiguration, Set<Resource>> configurationDependencies =
                mResourceConfigurationDependencies.get(resource);
        if (configurationDependencies == null) {
            configurationDependencies = new TreeMap<>();
            mResourceConfigurationDependencies.put(resource, configurationDependencies);
        }

        Set<Resource> dependencies = configurationDependencies.get(configuration);
        if (dependencies == null) {
            dependencies = new HashSet<>();
            configurationDependencies.put(configuration, dependencies);
        }

        return dependencies;
    }

    private static Resource createResourceFromDependency(String dependency) {
        String[] dependencyParts = dependency.split("/");
        if (dependencyParts.length == 2 && dependencyParts[0] != null && dependencyParts[1] != null) {
            switch (dependencyParts[0]) {
                case PREFIX_DRAWABLE:
                    return new Resource(dependencyParts[1], Resource.Type.DRAWABLE);
                case PREFIX_COLOR:
                    return new Resource(dependencyParts[1], Resource.Type.COLOR);
                case PREFIX_ATTR:
                    return new Resource(dependencyParts[1], Resource.Type.ATTR);
                case PREFIX_ANDROID_ATTR:
                    return new Resource(dependencyParts[1], Resource.Type.ANDROID_ATTR);
                default:
                    // Throw.
            }
        }
        throw new IllegalStateException("Error: impossible to map resource name " + dependency + " to any R variable.");
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
}
