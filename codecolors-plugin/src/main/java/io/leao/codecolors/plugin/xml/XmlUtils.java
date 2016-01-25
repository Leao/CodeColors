package io.leao.codecolors.plugin.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlUtils {
    private static final String RESOURCE_ITEM = "item";
    private static final String ATTRIBUTE_TYPE = "type";

    public static String getAttributeValue(Node node, String attribute) {
        Node nameAttr = node.getAttributes().getNamedItem(attribute);
        if (nameAttr != null) {
            return nameAttr.getNodeValue();
        } else {
            return null;
        }
    }

    public static void setAttributeValue(Node node, String attribute, String value) {
        Node nameAttr = node.getAttributes().getNamedItem(attribute);
        if (nameAttr != null) {
            nameAttr.setNodeValue(value);
        }
    }

    public static String getResourceAttributeValue(Node node, String resourceType, String attribute) {
        String currentNodeName = node.getNodeName();
        if (currentNodeName.startsWith(resourceType)) {
            return getAttributeValue(node, attribute);
        } else if (currentNodeName.startsWith(RESOURCE_ITEM)) {
            String typeValue = getAttributeValue(node, ATTRIBUTE_TYPE);
            if (resourceType.equals(typeValue)) {
                return getAttributeValue(node, attribute);
            }
        }
        return null;
    }

    public static void setResourceAttributeValue(Node node, String resourceType, String attribute, String value) {
        String currentNodeName = node.getNodeName();
        if (currentNodeName.startsWith(resourceType)) {
            setAttributeValue(node, attribute, value);
        } else if (currentNodeName.startsWith(RESOURCE_ITEM)) {
            String typeValue = getAttributeValue(node, ATTRIBUTE_TYPE);
            if (resourceType.equals(typeValue)) {
                setAttributeValue(node, attribute, value);
            }
        }
    }

    public static void writeTo(Document document, File output) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            FileOutputStream outputstream = new FileOutputStream(output);
            StreamResult result = new StreamResult(outputstream);

            // Manually add xml declaration, to force a newline after it.
            String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
            outputstream.write(xmlDeclaration.getBytes());

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);

            outputstream.close();
        } catch (TransformerException | IOException e) {
            System.out.println("Failed to write document file" + output.getPath() + ": " + e.toString());
        }
    }
}
