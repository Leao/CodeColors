package io.leao.codecolors.plugin.res;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PublicResourcesParser {
    private static final String RESOURCE_PUBLIC = "public";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TYPE = "type";

    private static final String TYPE_DRAWABLE = "drawable";
    private static final String TYPE_COLOR = "color";
    private static final String TYPE_ATTR = "attr";

    private final Resource.Pool mResourcesPool;

    public PublicResourcesParser(Resource.Pool resourcesPool) {
        mResourcesPool = resourcesPool;
    }

    public void parsePublicResources(File file) {
        try {
            parseXmlFile(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("Error processing file " + file.getName() + ": " + e.toString());
        }
    }

    private void parseXmlFile(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(file);

        parseNode(document.getDocumentElement());
    }

    public void parseNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        int childCount = childNodes.getLength();

        String currentNodeName = node.getNodeName();
        if (RESOURCE_PUBLIC.equals(currentNodeName)) {
            String name;
            Resource.Type type;
            if ((name = getNodeNameAttribute(node)) != null &&
                    (type = getNodeTypeAttribute(node)) != null) {
                Resource resource = mResourcesPool.getOrCreateResource(name, type);
                resource.setIsPublic(true);
            }
        }

        // Parse the remaining nodes.
        for (int i = 0; i < childCount; i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                parseNode(childNode);
            }
        }
    }

    private static String getNodeNameAttribute(Node node) {
        return getNodeAttribute(node, ATTRIBUTE_NAME);
    }

    private static Resource.Type getNodeTypeAttribute(Node node) {
        String typeName = getNodeAttribute(node, ATTRIBUTE_TYPE);
        if (typeName != null) {
            switch (typeName) {
                case TYPE_DRAWABLE:
                    return Resource.Type.DRAWABLE;
                case TYPE_COLOR:
                    return Resource.Type.COLOR;
                case TYPE_ATTR:
                    return Resource.Type.ATTR;
            }
        }
        return null;
    }

    private static String getNodeAttribute(Node node, String attribute) {
        Node nameAttr = node.getAttributes().getNamedItem(attribute);
        if (nameAttr != null) {
            return nameAttr.getNodeValue();
        }
        return null;
    }
}
