package io.leao.codecolors.plugin.task;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.leao.codecolors.plugin.android.AndroidSdkDependenciesParser;
import io.leao.codecolors.plugin.android.AndroidSdkPublicResourcesParser;
import io.leao.codecolors.plugin.android.AndroidSdkResourcesPool;
import io.leao.codecolors.plugin.res.DependenciesParser;
import io.leao.codecolors.plugin.res.Resource;

public class AndroidSdkDependenciesTask {
    private static final Map<Integer, String> ANDROID_SDK_VERSION_FOLDER_NAME = new HashMap<Integer, String>() {{
        put(16, "16");
        put(17, "17");
        put(18, "18");
        put(19, "19");
        put(20, "20");
        put(21, "21");
        put(22, "22");
        put(23, "23");
        put(24, "N");
    }};
    private static final String ANDROID_PLATFORM_DIR_BASE = "%s\\platforms\\android-%s\\data\\res";
    private static final String ANDROID_PUBLIC_FILE_BASE = "%s\\values\\public.xml";

    private static final String RESOURCES_DIR_BASE = "%s\\src\\main\\resources";
    public static final String RESOURCES_OUTPUT_FILE_NAME = "SdkResources.ser";

    public static void main(String[] args) {
        String rootDirPath = args[0];
        String projectDirPath = args[1];

        String androidSdkDirPath = getAndroidSdkDir(rootDirPath);

        AndroidSdkResourcesPool androidSdkResourcesPool = new AndroidSdkResourcesPool();

        for (int androidSdkVersion : ANDROID_SDK_VERSION_FOLDER_NAME.keySet()) {
            String androidSdkFolderName = ANDROID_SDK_VERSION_FOLDER_NAME.get(androidSdkVersion);

            File androidPlatformDir =
                    new File(String.format(ANDROID_PLATFORM_DIR_BASE, androidSdkDirPath, androidSdkFolderName));
            // Parse and get configuration resource dependencies.
            DependenciesParser dependenciesParser =
                    new AndroidSdkDependenciesParser(androidSdkResourcesPool, androidSdkVersion);
            dependenciesParser.parseDependencies(androidPlatformDir);

            File androidPublicFile = new File(String.format(ANDROID_PUBLIC_FILE_BASE, androidPlatformDir.getPath()));
            // Parse public.xml to convert resources to public.
            AndroidSdkPublicResourcesParser publicParser =
                    new AndroidSdkPublicResourcesParser(androidSdkResourcesPool, androidSdkVersion);
            publicParser.parsePublicResources(androidPublicFile);
        }

        for (Resource resource : androidSdkResourcesPool.getResources()) {
            // Prune dependencies.
            resource.pruneDependencies();

            // Update visibility.
            updateResourceVisibility(resource);
        }

        File outputFile = new File(String.format(RESOURCES_DIR_BASE, projectDirPath), RESOURCES_OUTPUT_FILE_NAME);
        androidSdkResourcesPool.writeTo(outputFile);
    }


    /**
     * Gets Android SDK location giving precedence to 'sdk.dir' defined in local.properties,
     * before trying 'ANDROID_HOME' system variable.
     */
    private static String getAndroidSdkDir(String rootDir) {
        String androidSdkDir;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream((new File(rootDir, "local.properties"))));
            androidSdkDir = properties.getProperty("sdk.dir");
        } catch (IOException e) {
            androidSdkDir = null;
        }

        if (androidSdkDir == null) {
            androidSdkDir = System.getenv("ANDROID_HOME");
        }

        if (androidSdkDir == null) {
            throw new GradleException("Android SDK location must be defined in local.properties 'sdk.dir' " +
                    "property, or in 'ANDROID_HOME' system variable.");
        }

        // Success.
        return androidSdkDir;
    }

    /**
     * When parsing Android resources, their visibility is {@link Resource.Visibility#PRIVATE} by default.
     * <p>
     * However, if they are public in all Android SDKs, their visibility should be {@link Resource.Visibility#PUBLIC},
     * and if they are public in some Android SDKs, their visibility should be {@link Resource.Visibility#MIXED}.
     */
    private static void updateResourceVisibility(Resource resource) {
        Resource.Visibility visibility = null;
        for (int androidSdkVersion : ANDROID_SDK_VERSION_FOLDER_NAME.keySet()) {
            boolean isPublic = resource.isPublic(androidSdkVersion);

            if (visibility == null) {
                visibility = isPublic ? Resource.Visibility.PUBLIC : Resource.Visibility.PRIVATE;
            } else {
                if (visibility == Resource.Visibility.PUBLIC) {
                    if (!isPublic) {
                        visibility = Resource.Visibility.MIXED;
                        break;
                    }
                } else {
                    if (isPublic) {
                        visibility = Resource.Visibility.MIXED;
                        break;
                    }
                }
            }
        }
        resource.setVisibility(visibility);
    }
}