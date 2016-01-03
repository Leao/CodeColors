package io.leao.codecolors.plugin.res;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Map;
import java.util.Set;

/**
 * Parses list of CodeColors colors, stores it by {@link CcConfiguration}s, and replaces its entries defined in
 * {@code values.xml} files by separate ColorStateList files.
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
