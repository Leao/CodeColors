package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.MergeResources;
import com.android.ide.common.res2.ResourceSet;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.internal.tasks.ContextAwareTaskAction;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.leao.codecolors.plugin.aapt.AaptUtil;
import io.leao.codecolors.plugin.action.IgnoreResFilesTaskActionWrapper;
import io.leao.codecolors.plugin.file.FileUtils;
import io.leao.codecolors.plugin.file.IgnoreResFileFile;

/**
 * Configures MergeResources task to ignore CodeColors res files.
 * <p>
 * Adds a wrapper to all {@link MergeResources} tasks, in order to avoid issues with incremental tasks when the res
 * files are changed.
 * <p>
 * Manipulates all {@link ResourceSet}s in {@link MergeResources} task, in order to skip res files when listing
 * children files.
 */
public class MergeResourcesConfigurationTask extends DefaultTask {
    private static final String NAME_BASE = "ccConfigureMerge%sResources";

    private BaseVariant mVariant;
    private Set<File> mResFiles;

    public static MergeResourcesConfigurationTask create(Project project, BaseVariant variant,
                                                         ResourcesResGeneratingTask resourcesResGeneratingTask) {
        String name = String.format(NAME_BASE, AaptUtil.capitalize(variant.getName()));
        MergeResourcesConfigurationTask task = project.getTasks().create(name, MergeResourcesConfigurationTask.class);
        task.initialize(variant, resourcesResGeneratingTask);

        return task;
    }

    public void initialize(BaseVariant v, ResourcesResGeneratingTask resourcesResGeneratingTask) {
        mVariant = v;
        mVariant.getMergeResources().dependsOn(this);

        dependsOn(resourcesResGeneratingTask.getGradleTask());
        mResFiles = resourcesResGeneratingTask.getResFiles();
    }

    @TaskAction
    public void configure() {
        MergeResources mergeResourcesTask = mVariant.getMergeResources();

        setupTasks(mergeResourcesTask);

        setupResourceSets(mergeResourcesTask);
    }

    private void setupTasks(MergeResources mergeResources) {
        List<ContextAwareTaskAction> tasks = mergeResources.getTaskActions();
        List<ContextAwareTaskAction> newTasks = new ArrayList<>(tasks.size());
        for (ContextAwareTaskAction action : tasks) {
            newTasks.add(new IgnoreResFilesTaskActionWrapper(action, mResFiles));
        }
        mergeResources.deleteAllActions();
        mergeResources.getTaskActions().addAll(newTasks);
    }

    private void setupResourceSets(MergeResources mergeResources) {
        List<ResourceSet> resourceSets = mergeResources.getInputResourceSets();
        List<ResourceSet> newResourceSets = new ArrayList<>(resourceSets.size());
        for (ResourceSet resourceSet : resourceSets) {
            // Change ResourceSets files to ignore res files.
            List<File> newFiles = new ArrayList<>();
            Iterator<File> iterator = resourceSet.getSourceFiles().iterator();
            while (iterator.hasNext()) {
                File file = iterator.next();
                if (FileUtils.inFolder(file, mResFiles)) {
                    iterator.remove();
                    newFiles.add(new IgnoreResFileFile(file.getPath(), mResFiles));
                }
            }
            if (newFiles.size() > 0) {
                resourceSet.getSourceFiles().addAll(newFiles);
            }
            newResourceSets.add(resourceSet);
        }
        mVariant.getMergeResources().setInputResourceSets(newResourceSets);
    }
}
