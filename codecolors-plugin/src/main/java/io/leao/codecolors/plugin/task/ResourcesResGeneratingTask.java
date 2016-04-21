package io.leao.codecolors.plugin.task;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.SourceProvider;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.plugin.file.FileCrawler;
import io.leao.codecolors.plugin.file.FileCrawlerResFileCallback;
import io.leao.codecolors.plugin.file.ResFileIdentifier;
import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.plugin.res.ResFileParser;
import io.leao.codecolors.plugin.source.ColorConfigurationsGenerator;
import io.leao.codecolors.plugin.source.ResourcesGenerator;

public class ResourcesResGeneratingTask extends ResGeneratingTask {
    private static final String NAME_BASE = "generateCcResources";

    private Project mProject;
    private BaseVariant mVariant;
    private ResFileIdentifier mResFileIdentifier;
    private Set<File> mResFiles;

    private boolean mResFilesChanged;

    public ResourcesResGeneratingTask(Project project, BaseVariant variant) {
        super(project, variant, NAME_BASE);

        mProject = project;
        mVariant = variant;
        mResFileIdentifier = new ResFileIdentifier(project);

        /*
         * Init res files.
         */

        mResFiles = new HashSet<>();

        FileCrawler.Callback resFileCallback = new FileCrawlerResFileCallback<Void>(mProject) {
            @Override
            public void parseFile(File file, Void trail) {
                mResFiles.add(file);
            }

            @Override
            public Void createTrail(File folder, Void trail) {
                return null;
            }
        };

        for (SourceProvider provider : mVariant.getSourceSets()) {
            for (File resDir : provider.getResDirectories()) {
                // Add res dir as task input file.
                addInputFile(resDir);
                // Collect all res files in path.
                FileCrawler.crawl(resDir, null, resFileCallback);
            }
        }
    }

    public Set<File> getResFiles() {
        return mResFiles;
    }

    @Override
    public void generate(IncrementalTaskInputs inputs) {
        mResFilesChanged = !inputs.isIncremental();

        if (!mResFilesChanged) {
            Action<InputFileDetails> detectChangesAction = new Action<InputFileDetails>() {
                @Override
                public void execute(InputFileDetails inputFileDetails) {
                    if (!mResFilesChanged) {
                        mResFilesChanged = mResFileIdentifier.isResFile(inputFileDetails.getFile(), true);
                    }
                }
            };

            inputs.outOfDate(detectChangesAction);
            if (!mResFilesChanged) {
                inputs.removed(detectChangesAction);
            }
        }

        if (mResFilesChanged) {
            // If we detect any changes, will rebuild all CodeColor resources.
            // Incremental build is too complex for its eventual gain.
            // For every build, we would have to parse the existing resources, and update or remove their
            // individual files and default values, for every configuration.
            generateResources();
        }
    }

    private void generateResources() {
        // Remove output folder contents if it exists already.
        if (getOutputDir().exists()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(getOutputDir());
            } catch (IOException e) {
                System.out.println(
                        String.format(
                                "Failed to remove resources directory %s: %s",
                                getOutputDir().getPath(),
                                e.toString()));
            }
        }

        ResFileParser resFileParser = new ResFileParser();
        ResourcesGenerator resourcesGenerator = new ResourcesGenerator(getOutputDir());
        ColorConfigurationsGenerator colorConfigurationGenerator = new ColorConfigurationsGenerator(mProject, mVariant);

        for (File file : mResFiles) {
            resFileParser.parseFile(file, new ResFileParser.Callback() {
                @Override
                public void parseColor(String folderName, String color, String value, CcConfiguration configuration) {
                    // Populate generators with colors.
                    resourcesGenerator.addColor(folderName, color, value);
                    colorConfigurationGenerator.addColor(color, configuration);
                }
            });
        }

        // Generate colors and values resources.
        resourcesGenerator.generate();

        // Generate color configuration map file.
        colorConfigurationGenerator.generate();
    }
}
