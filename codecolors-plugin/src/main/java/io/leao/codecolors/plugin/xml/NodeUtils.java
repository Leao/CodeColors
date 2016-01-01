package io.leao.codecolors.plugin.xml;

import org.w3c.dom.Node;

public class NodeUtils {
    public static String getAttributeValue(Node node, String attribute) {
        Node nameAttr = node.getAttributes().getNamedItem(attribute);
        if (nameAttr != null) {
            return nameAttr.getNodeValue();
        } else {
            return null;
        }
    }
}
