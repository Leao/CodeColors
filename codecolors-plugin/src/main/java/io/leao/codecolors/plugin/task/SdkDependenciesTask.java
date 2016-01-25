package io.leao.codecolors.plugin.task;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

import io.leao.codecolors.plugin.aapt.AaptUtil;
import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.res.PublicResourcesParser;
import io.leao.codecolors.plugin.res.Resource;
import io.leao.codecolors.plugin.res.DependenciesParser;

public class SdkDependenciesTask extends DefaultTask {
    private static final String NAME_BASE = "generateSdkDependencies%s";
    private static final String INPUT_DIR_BASE = "%s\\platforms\\%s\\data\\res";
    private static final String PUBLIC_FILE_BASE = "%s\\values\\public.xml";
    private static final String SDK_RESOURCES_FILE_NAME = "SdkResources.ser";

    private File mInputDir;
    private File mOutputFile;

    public static SdkDependenciesTask create(Project project, BaseExtension extension, BaseVariant variant) {
        String name = String.format(NAME_BASE, AaptUtil.capitalize(variant.getName()));
        SdkDependenciesTask task = project.getTasks().create(name, SdkDependenciesTask.class);
        task.initialize(project, extension, variant);

        return task;
    }

    protected void initialize(Project project, BaseExtension extension, BaseVariant variant) {
        mInputDir = project.file(
                String.format(INPUT_DIR_BASE, extension.getSdkDirectory(), extension.getCompileSdkVersion()));

        // Output directory.
        mOutputFile = new File(FileUtils.obtainIntermediatesDirFile(project, variant), SDK_RESOURCES_FILE_NAME);
    }

    @InputDirectory
    public File getInputDir() {
        return mInputDir;
    }

    @OutputFile
    public File getOutputFile() {
        return mOutputFile;
    }

    @TaskAction
    public void generateResources() {
        SdkResourcesPool sdkResourcesPool = new SdkResourcesPool();

        // Parse and get configuration resource dependencies.
        DependenciesParser dependenciesParser = new DependenciesParser(sdkResourcesPool);
        dependenciesParser.parseDependencies(mInputDir);

        // Parse public.xml to convert resources to public.
        PublicResourcesParser publicParser = new PublicResourcesParser(sdkResourcesPool);
        publicParser.parsePublicResources(new File(String.format(PUBLIC_FILE_BASE, mInputDir.getPath())));

        sdkResourcesPool.writeTo(mOutputFile);
    }

    private static class SdkResourcesPool extends Resource.Pool {
        /**
         * <p>Convert default drawable, color, and attr resources, to Android resources.
         * <p>Their default public state is {@code false}, as they have to be included in
         * the {@code public.xml} file to be publicly visible.
         */
        @Override
        public Resource getOrCreateResource(String name, Resource.Type type) {
            switch (type) {
                case DRAWABLE:
                    type = Resource.Type.ANDROID_DRAWABLE;
                    break;
                case COLOR:
                    type = Resource.Type.ANDROID_COLOR;
                    break;
                case ATTR:
                    type = Resource.Type.ANDROID_ATTR;
                    break;
            }
            // Name, type.
            return super.getOrCreateResource(name, type);
        }

        /**
         * By default, we consider that SDK Resources are not public.
         * <p/>
         * We will parse the public resources later and set the public ones.
         */
        @Override
        protected boolean isPublic(String name, Resource.Type type) {
            return false;
        }
    }
}