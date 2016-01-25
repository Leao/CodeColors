package io.leao.codecolors.plugin.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlCrawler {
    public static <T> Document crawl(File file, T trail, Callback<T> callback) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(file);

            crawl(document.getDocumentElement(), trail, callback);

            return document;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Error crawling file " + file.getName() + ": " + e.toString());
            return null;
        }
    }

    private static <T> void crawl(Node node, T trail, Callback<T> callback) {
        NodeList childNodes = node.getChildNodes();
        int childCount = childNodes.getLength();

        if (!callback.parseNode(node, childNodes, childCount, trail)) {
            // Parse the remaining nodes if parseNode does not stop the crawler.
            for (int i = 0; i < childCount; i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeType() == Node.ELEMENT_NODE) {
                    crawl(childNode, callback.createTrail(childNode, trail), callback);
                }
            }
        }
    }

    public interface Callback<T> {
        /**
         * @return true to stop node crawl; false to continue node crawl.
         */
        boolean parseNode(Node node, NodeList children, int childCount, T trail);

        T createTrail(Node node, T trail);
    }
}
