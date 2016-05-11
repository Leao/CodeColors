package io.leao.codecolors.plugin.android;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import io.leao.codecolors.plugin.res.Resource;
import io.leao.codecolors.plugin.xml.XmlCrawler;
import io.leao.codecolors.plugin.xml.XmlUtils;

public class AndroidSdkPublicResourcesParser implements XmlCrawler.Callback<Void> {
    private static final String RESOURCE_PUBLIC = "public";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TYPE = "type";

    private static final String TYPE_DRAWABLE = "drawable";
    private static final String TYPE_COLOR = "color";
    private static final String TYPE_ATTR = "attr";

    private Resource.Pool mResourcesPool;
    private int mAndroidSdkVersion;

    public AndroidSdkPublicResourcesParser(Resource.Pool resourcesPool, int androidSdkVersion) {
        mResourcesPool = resourcesPool;
        mAndroidSdkVersion = androidSdkVersion;
    }

    public void parsePublicResources(File file) {
        XmlCrawler.crawl(file, null, this);
    }

    @Override
    public boolean parseNode(Node node, NodeList children, int childCount, Void trail) {
        String currentNodeName = node.getNodeName();
        if (RESOURCE_PUBLIC.equals(currentNodeName)) {
            String name;
            Resource.Type type;
            if ((name = XmlUtils.getAttributeValue(node, ATTRIBUTE_NAME)) != null &&
                    (type = getNodeTypeAttribute(node)) != null) {
                Resource resource = mResourcesPool.getOrCreateResource(name, type);
                resource.setIsPublic(mAndroidSdkVersion, true);
            }
        }
        return false;
    }

    @Override
    public Void createTrail(Node node, Void trail) {
        return null;
    }

    private static Resource.Type getNodeTypeAttribute(Node node) {
        String typeName = XmlUtils.getAttributeValue(node, ATTRIBUTE_TYPE);
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
}
