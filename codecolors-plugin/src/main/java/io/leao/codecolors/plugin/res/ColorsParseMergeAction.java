package io.leao.codecolors.plugin.res;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Map;
import java.util.Set;

/**
 * Parses list of CodeColors colors, stores their {@link CcConfiguration}s, and replaces its names in
 * {@code values.xml} file, and creates separate ColorStateList files with the original names.
 */
public class ColorsParseMergeAction implements Action<Task> {

    private ColorsParser mColorsParser;
    private ColorsMerger mColorsMerger;

    public static ColorsParseMergeAction create(Project project, BaseVariant variant) {
        ColorsParseMergeAction action = new ColorsParseMergeAction(project, variant);
        variant.getMergeResources().doLast(action);
        return action;
    }

    protected ColorsParseMergeAction(Project project, BaseVariant variant) {
        mColorsParser = new ColorsParser(project, variant);
        mColorsMerger = new ColorsMerger(variant);
    }

    @Override
    public void execute(Task task) {
        Map<String, Set<String>> folderColors = mColorsParser.parseColors();

        mColorsMerger.mergeColors(folderColors);
    }
}
